package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("BootCompletedReceiver", "Received system intent: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_DATE_CHANGED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            WaqtiNotificationChannels.createChannels(context)

            // Trigger Smart Notification Engine to recalculate and reschedule
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d("WAQTI_PRAYER_DEBUG", "System broadcast received ($action). Recalculating and scheduling prayer notifications.")
                    SmartNotificationEngine.recalculateAndScheduleAll(context)
                } catch (e: Exception) {
                    Log.e("BootCompletedReceiver", "Failed to reschedule on boot: ${e.message}")
                }
            }
        }
    }
}
