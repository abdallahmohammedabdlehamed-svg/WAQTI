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
    }
}
