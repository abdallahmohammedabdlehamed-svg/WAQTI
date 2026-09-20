package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.notification.NotificationScheduleEntity

object NotificationScheduler {

    private const val TAG = "NotificationScheduler"

    /**
     * Generates a deterministic, stable request code/ID for each notification type and source.
     * This prevents multiple alarm duplication in AlarmManager and notification shade piling.
     */
    fun getStableNotificationId(type: String, sourceEntityType: String, sourceEntityId: String): Int {
        return when (type) {
            "PRAYER" -> {
                val base = if (sourceEntityType == "PRAYER_BEFORE") 2100 else 2000
                val prayerOffset = when (sourceEntityId.lowercase()) {
                    "fajr" -> 1
                    "sunrise" -> 2
                    "dhuhr" -> 3
                    "asr" -> 4
                    "maghrib" -> 5
                    "isha" -> 6
                    else -> Math.abs(sourceEntityId.hashCode()) % 50
                }
                base + prayerOffset
            }
            "AI_SUGGESTIONS" -> 3001
            "MORNING_AZKAR" -> 3002
            "EVENING_AZKAR" -> 3003
            "QURAN_ROUTINE" -> 3004
            "EXERCISE" -> 3005
            "SLEEP_PREP" -> 3006
            "WATER_HYDRATION" -> 3007
            "SYSTEM" -> if (sourceEntityId == "surah_kahf") 3008 else 3009
            "TASKS" -> {
                val taskId = sourceEntityId.toLongOrNull() ?: Math.abs(sourceEntityId.hashCode().toLong())
                (4000 + (taskId % 3000)).toInt()
            }
            "ROUTINE" -> {
                val routineId = sourceEntityId.toLongOrNull() ?: Math.abs(sourceEntityId.hashCode().toLong())
                (7000 + (routineId % 1000)).toInt()
            }
            else -> {
                val hash = Math.abs("${type}_${sourceEntityType}_${sourceEntityId}".hashCode())
                8000 + (hash % 1000)
            }
        }
    }

    fun getStableNotificationId(schedule: NotificationScheduleEntity): Int {
        return getStableNotificationId(schedule.type, schedule.sourceEntityType, schedule.sourceEntityId)
    }

    fun scheduleNotification(context: Context, schedule: NotificationScheduleEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val triggerAtMillis = schedule.scheduledAt
        val now = System.currentTimeMillis()

        // If the schedule is already in the past by more than 1 minute, don't set an alarm
        if (triggerAtMillis < now - 60_000) {
            Log.d(TAG, "Skipping past schedule #${schedule.id} (${schedule.title})")
            return
        }

        // Cancel any existing alarm with this stable ID before setting a new one
        cancelNotification(context, schedule)

        val stableCode = getStableNotificationId(schedule)

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("schedule_id", schedule.id)
            putExtra("stable_code", stableCode)
            putExtra("type", schedule.type)
            putExtra("title", schedule.title)
            putExtra("body", schedule.body)
            putExtra("channel", schedule.channel)
            putExtra("priority", schedule.priority)
            putExtra("source_type", schedule.sourceEntityType)
            putExtra("source_id", schedule.sourceEntityId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            stableCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val canExact = try { alarmManager.canScheduleExactAlarms() } catch (e: Exception) { false }
                if (canExact && (schedule.priority == "CRITICAL" || schedule.priority == "HIGH")) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm #$stableCode for ${schedule.title} at $triggerAtMillis")
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission missing, falling back to setAndAllowWhileIdle: ${e.message}")
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to schedule fallback alarm: ${ex.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm: ${e.message}", e)
        }
    }

    fun cancelNotification(context: Context, schedule: NotificationScheduleEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NotificationReceiver::class.java)

        // Cancel using stable code
        val stableCode = getStableNotificationId(schedule)
        val pendingIntentStable = PendingIntent.getBroadcast(
            context,
            stableCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntentStable)

        // Also cancel using numeric database id if valid
        if (schedule.id > 0) {
            val pendingIntentId = PendingIntent.getBroadcast(
                context,
                schedule.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntentId)
        }
    }

    fun cancelNotification(context: Context, scheduleId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
