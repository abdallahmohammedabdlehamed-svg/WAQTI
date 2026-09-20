package com.example.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.WaqtiDatabase
import com.example.data.notification.NotificationLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WaqtiNotificationPoster {

    private const val TAG = "WaqtiNotificationPoster"

    // Throttle cache to prevent rapid duplicate posts of identical notifications
    private val recentNotifications = java.util.concurrent.ConcurrentHashMap<String, Long>()

    /**
     * Clears all active notifications from the drawer (useful to clear old duplicate spam)
     */
    fun cancelAllActiveNotifications(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancelAll()
            Log.d(TAG, "Cleared all active notifications from system tray")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear active notifications: ${e.message}")
        }
    }

    /**
     * Posts a notification immediately to the Android notification drawer.
     * Ensures channels are created, sets custom icons, actions, sound, and logs to Room database.
     */
    fun showNotification(
        context: Context,
        scheduleId: Long = System.currentTimeMillis() % 100000,
        type: String = "TASKS",
        title: String = "تنبيه من وقتي",
        body: String = "لديك موعد مجدول في تطبيق وقتي",
        channelId: String = WaqtiNotificationChannels.CHANNEL_TASKS,
        priorityStr: String = "HIGH",
        sourceEntityType: String = "",
        sourceEntityId: String = ""
    ) {
        try {
            // Deduplication & Anti-spam Throttling
            val dedupeKey = "${type}::${sourceEntityType}::${sourceEntityId}::${title.trim()}"
            val now = System.currentTimeMillis()
            val lastPosted = recentNotifications[dedupeKey] ?: 0L

            val isDailySummary = type == "AI_SUGGESTIONS" || sourceEntityId == "daily_summary"
            // Daily summary can only post once every 6 hours; other alerts throttle within 60 seconds
            val suppressWindow = if (isDailySummary) 6 * 60 * 60 * 1000L else 60 * 1000L

            if (now - lastPosted < suppressWindow) {
                Log.d(TAG, "Suppressed duplicate notification for key: $dedupeKey (already posted ${(now - lastPosted) / 1000}s ago)")
                return
            }
            recentNotifications[dedupeKey] = now

            // 1. Ensure all notification channels exist
            WaqtiNotificationChannels.createChannels(context)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (notificationManager == null) {
                Log.e(TAG, "NotificationManager service is null!")
                return
            }

            val stableNotifyId = NotificationScheduler.getStableNotificationId(type, sourceEntityType, sourceEntityId)
            val notificationTag = "waqti_${type}_${sourceEntityType}_${sourceEntityId}"

            // 2. Intent to open the main app
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", type)
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                stableNotifyId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val priorityCompat = when (priorityStr) {
                "CRITICAL" -> NotificationCompat.PRIORITY_MAX
                "HIGH" -> NotificationCompat.PRIORITY_HIGH
                "LOW" -> NotificationCompat.PRIORITY_LOW
                else -> NotificationCompat.PRIORITY_DEFAULT
            }

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            // 3. Build notification
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_waqti_clock_symbol)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(priorityCompat)
                .setAutoCancel(true)
                .setContentIntent(openAppPendingIntent)
                .setSound(defaultSoundUri)
                .setColor(0xFF00A3FF.toInt()) // Vibrant Waqti Primary Blue
                .setVibrate(longArrayOf(0, 300, 200, 300))

            // 4. Context-sensitive interactive action buttons
            when (type) {
                "TASKS" -> {
                    val completeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                        action = NotificationActionReceiver.ACTION_COMPLETE_TASK
                        putExtra("source_id", sourceEntityId)
                        putExtra("notification_id", scheduleId)
                    }
                    val completePendingIntent = PendingIntent.getBroadcast(
                        context,
                        (scheduleId * 10 + 1).toInt(),
                        completeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                        action = NotificationActionReceiver.ACTION_SNOOZE
                        putExtra("notification_id", scheduleId)
                        putExtra("snooze_minutes", 15)
                    }
                    val snoozePendingIntent = PendingIntent.getBroadcast(
                        context,
                        (scheduleId * 10 + 2).toInt(),
                        snoozeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    builder.addAction(android.R.drawable.checkbox_on_background, "إتمام ✓", completePendingIntent)
                    builder.addAction(android.R.drawable.ic_popup_sync, "تأجيل 15د ⏳", snoozePendingIntent)
                }

                "EXERCISE" -> {
                    val rescheduleIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                        action = NotificationActionReceiver.ACTION_RESCHEDULE_EXERCISE
                        putExtra("notification_id", scheduleId)
                    }
                    val reschedulePendingIntent = PendingIntent.getBroadcast(
                        context,
                        (scheduleId * 10 + 1).toInt(),
                        rescheduleIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    builder.addAction(android.R.drawable.ic_media_play, "بدء الآن 💪", openAppPendingIntent)
                    builder.addAction(android.R.drawable.ic_menu_recent_history, "نقل لـ 8:00 🕗", reschedulePendingIntent)
                }

                "QURAN_ROUTINE" -> {
                    val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                        action = NotificationActionReceiver.ACTION_COMPLETE_ROUTINE
                        putExtra("routine_name", "ورد القرآن")
                        putExtra("notification_id", scheduleId)
                    }
                    val donePendingIntent = PendingIntent.getBroadcast(
                        context,
                        (scheduleId * 10 + 1).toInt(),
                        doneIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    builder.addAction(android.R.drawable.ic_menu_compass, "فتح المصحف 📖", openAppPendingIntent)
                    builder.addAction(android.R.drawable.checkbox_on_background, "أتممت الورد ✓", donePendingIntent)
                }

                "MORNING_AZKAR", "EVENING_AZKAR" -> {
                    val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                        action = NotificationActionReceiver.ACTION_COMPLETE_ROUTINE
                        putExtra("routine_name", if (type == "MORNING_AZKAR") "أذكار الصباح" else "أذكار المساء")
                        putExtra("notification_id", scheduleId)
                    }
                    val donePendingIntent = PendingIntent.getBroadcast(
                        context,
                        (scheduleId * 10 + 1).toInt(),
                        doneIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    builder.addAction(android.R.drawable.ic_menu_agenda, "قراءة الأذكار 🌿", openAppPendingIntent)
                    builder.addAction(android.R.drawable.checkbox_on_background, "تمت القراءة ✓", donePendingIntent)
                }

                "PRAYER" -> {
                    val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                        action = NotificationActionReceiver.ACTION_COMPLETE_PRAYER
                        putExtra("prayer_name", title)
                        putExtra("notification_id", scheduleId)
                    }
                    val donePendingIntent = PendingIntent.getBroadcast(
                        context,
                        (scheduleId * 10 + 1).toInt(),
                        doneIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    builder.addAction(android.R.drawable.checkbox_on_background, "صليت بالمسجد 🕌", donePendingIntent)
                    builder.addAction(android.R.drawable.ic_menu_agenda, "عرض الأوقات", openAppPendingIntent)
                }
            }

            // 5. Post notification using stable tag and stable ID (updates existing notification instead of stacking)
            val notificationCompatManager = NotificationManagerCompat.from(context)
            notificationCompatManager.notify(notificationTag, stableNotifyId, builder.build())
            Log.d(TAG, "Successfully posted notification [$notificationTag #$stableNotifyId]: $title")

            // 6. Record delivery log in Room DB asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = WaqtiDatabase.getInstance(context)
                    val dao = db.waqtiDao()
                    dao.updateScheduleStatus(scheduleId, "DELIVERED")
                    dao.insertLog(
                        NotificationLogEntity(
                            notificationId = scheduleId,
                            category = type,
                            deliveredAt = System.currentTimeMillis()
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to write notification delivery log: ${e.message}")
                }
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while showing notification (check POST_NOTIFICATIONS): ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error posting notification: ${e.message}", e)
        }
    }
}
