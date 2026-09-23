package com.example.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.WaqtiDatabase
import com.example.data.notification.NotificationLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra("schedule_id", System.currentTimeMillis() % 100000)
        val type = intent.getStringExtra("type") ?: "TASKS"
        val title = intent.getStringExtra("title") ?: "تنبيه من وقتي"
        val body = intent.getStringExtra("body") ?: "لديك موعد مجدول في تطبيق وقتي"
        val channelId = intent.getStringExtra("channel") ?: WaqtiNotificationChannels.CHANNEL_TASKS
        val priorityStr = intent.getStringExtra("priority") ?: "HIGH"
        val sourceType = intent.getStringExtra("source_type") ?: ""
        val sourceId = intent.getStringExtra("source_id") ?: ""

        WaqtiNotificationPoster.showNotification(
            context = context,
            scheduleId = scheduleId,
            type = type,
            title = title,
            body = body,
            channelId = channelId,
            priorityStr = priorityStr,
            sourceEntityType = sourceType,
            sourceEntityId = sourceId
        )

        // If this is an exact prayer event, trigger Adhan playback service if enabled and allowed
        if (type == "PRAYER" && (sourceType == "PRAYER_EXACT" || priorityStr == "CRITICAL")) {
            val prayerEnum = com.example.data.prayer.WaqtiPrayer.fromString(sourceId) ?: when {
                title.contains("الفجر") || title.contains("Fajr", ignoreCase = true) -> com.example.data.prayer.WaqtiPrayer.FAJR
                title.contains("الظهر") || title.contains("Dhuhr", ignoreCase = true) -> com.example.data.prayer.WaqtiPrayer.DHUHR
                title.contains("العصر") || title.contains("Asr", ignoreCase = true) -> com.example.data.prayer.WaqtiPrayer.ASR
                title.contains("المغرب") || title.contains("Maghrib", ignoreCase = true) -> com.example.data.prayer.WaqtiPrayer.MAGHRIB
                title.contains("العشاء") || title.contains("Isha", ignoreCase = true) -> com.example.data.prayer.WaqtiPrayer.ISHA
                else -> null
            }

            val prayerName = prayerEnum?.nameEn ?: sourceId.ifBlank { "Prayer" }
            android.util.Log.d("WAQTI_ADHAN_DEBUG", "Prayer alarm received in NotificationReceiver for: $prayerName")

            val audioPrefs = com.example.audio.WaqtiAudioPreferences.getInstance(context)
            val settings = audioPrefs.getSettings()

            if (settings.masterNotificationsEnabled && settings.adhanEnabledGlobal && audioPrefs.isAdhanEnabledForPrayer(prayerName)) {
                val inQuiet = audioPrefs.isInQuietHours()
                val quietAllowed = !inQuiet || settings.quietHoursAllowPrayers
                if (quietAllowed) {
                    if (audioPrefs.canPlayAdhan(prayerName)) {
                        audioPrefs.markAdhanPlayed(prayerName)
                        android.util.Log.d("WAQTI_ADHAN_DEBUG", "Triggering WaqtiAdhanService for scheduled prayer: $prayerName")
                        com.example.audio.WaqtiAdhanService.start(context, prayerName, isTest = false)
                    } else {
                        android.util.Log.d("WAQTI_ADHAN_DEBUG", "Duplicate Adhan suppressed for prayer: $prayerName within 20min window")
                    }
                } else {
                    android.util.Log.d("WAQTI_ADHAN_DEBUG", "Adhan suppressed due to Quiet Hours for: $prayerName")
                }
            } else {
                android.util.Log.d("WAQTI_ADHAN_DEBUG", "Adhan not enabled in settings for prayer: $prayerName")
            }
        }
    }
}
