package com.example.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.WaqtiDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COMPLETE_TASK = "com.example.ACTION_COMPLETE_TASK"
        const val ACTION_SNOOZE = "com.example.ACTION_SNOOZE"
        const val ACTION_RESCHEDULE_EXERCISE = "com.example.ACTION_RESCHEDULE_EXERCISE"
        const val ACTION_SKIP_EXERCISE = "com.example.ACTION_SKIP_EXERCISE"
        const val ACTION_COMPLETE_ROUTINE = "com.example.ACTION_COMPLETE_ROUTINE"
        const val ACTION_COMPLETE_PRAYER = "com.example.ACTION_COMPLETE_PRAYER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val notificationId = intent.getLongExtra("notification_id", -1)
        val sourceId = intent.getStringExtra("source_id") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (notificationId != -1L) {
            notificationManager?.cancel(notificationId.toInt())
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = WaqtiDatabase.getInstance(context)
                val dao = db.waqtiDao()

                // Update notification log
                if (notificationId != -1L) {
                    dao.updateLogAction(notificationId, action, System.currentTimeMillis())
                }

                when (action) {
                    ACTION_COMPLETE_TASK -> {
                        val taskId = sourceId.toLongOrNull()
                        if (taskId != null) {
                            val tasks = dao.getAllTasks().firstOrNull()
                            val task = tasks?.find { it.id == taskId }
                            if (task != null) {
                                dao.updateTask(task.copy(status = "COMPLETED"))
                            }
                        }
                    }

                    ACTION_SNOOZE -> {
                        val snoozeMinutes = intent.getIntExtra("snooze_minutes", 15)
                        val triggerAt = System.currentTimeMillis() + snoozeMinutes * 60 * 1000
                        val snoozedSchedule = com.example.data.notification.NotificationScheduleEntity(
                            title = "تذكير مؤجل",
                            body = "حان وقت المتابعة بعد انتهاء فترة التأجيل ($snoozeMinutes دقيقة)",
                            scheduledAt = triggerAt,
                            type = "TASKS",
                            priority = "NORMAL",
                            channel = WaqtiNotificationChannels.CHANNEL_TASKS
                        )
                        val newId = dao.insertSchedule(snoozedSchedule)
                        NotificationScheduler.scheduleNotification(context, snoozedSchedule.copy(id = newId))
                    }

                    ACTION_RESCHEDULE_EXERCISE -> {
                        val routines = dao.getAllRoutines().firstOrNull()
                        val exercise = routines?.find { it.category == "HEALTH" || it.title.contains("Fitness") || it.titleAr.contains("تمرين") }
                        if (exercise != null) {
                            dao.updateRoutine(exercise.copy(time = "20:00"))
                        }
                    }

                    ACTION_SKIP_EXERCISE -> {
                        Log.d("ActionReceiver", "Skipped workout for today.")
                    }

                    ACTION_COMPLETE_ROUTINE, ACTION_COMPLETE_PRAYER -> {
                        val routineName = intent.getStringExtra("routine_name") ?: intent.getStringExtra("prayer_name") ?: ""
                        val routines = dao.getAllRoutines().firstOrNull()
                        val routine = routines?.find { it.title.contains(routineName, ignoreCase = true) || it.titleAr.contains(routineName) }
                        if (routine != null) {
                            dao.updateRoutine(routine.copy(isCompleted = true, streak = routine.streak + 1))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
