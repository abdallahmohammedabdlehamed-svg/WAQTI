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
        val priorityStr = intent.getStringExtra("priority") ?: "NORMAL"
        val sourceType = intent.getStringExtra("source_type") ?: ""
        val sourceId = intent.getStringExtra("source_id") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        // Open App Intent
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", type)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            scheduleId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priorityCompat = when (priorityStr) {
            "CRITICAL" -> NotificationCompat.PRIORITY_MAX
            "HIGH" -> NotificationCompat.PRIORITY_HIGH
            "LOW" -> NotificationCompat.PRIORITY_LOW
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(priorityCompat)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)

        // Add context-sensitive action buttons based on type
        when (type) {
            "TASKS" -> {
                val completeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_COMPLETE_TASK
                    putExtra("source_id", sourceId)
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

                val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_SKIP_EXERCISE
                    putExtra("notification_id", scheduleId)
                }
                val skipPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (scheduleId * 10 + 2).toInt(),
                    skipIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                builder.addAction(android.R.drawable.ic_media_play, "بدء الآن 💪", openAppPendingIntent)
                builder.addAction(android.R.drawable.ic_menu_recent_history, "نقل لـ 8:00 🕗", reschedulePendingIntent)
                builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "تخطي اليوم", skipPendingIntent)
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

                builder.addAction(android.R.drawable.checkbox_on_background, "صليت بالمسجد/البيت 🕌", donePendingIntent)
                builder.addAction(android.R.drawable.ic_menu_agenda, "عرض الأوقات", openAppPendingIntent)
            }
        }

        try {
            notificationManager.notify(scheduleId.toInt(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Record delivery log and update schedule in database
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
                e.printStackTrace()
            }
        }
    }
}
