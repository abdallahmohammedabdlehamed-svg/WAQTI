package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

/**
 * Versioned notification channels for WAQTI Audio 2.0.
 * Ensures consistent audio attributes, importance, and vibration across modern Android versions.
 */
object WaqtiNotificationChannels {

    // V2 Versioned Channel IDs
    const val CHANNEL_PRAYERS_V2 = "channel_prayers_v2"
    const val CHANNEL_TASKS_V2 = "channel_tasks_v2"
    const val CHANNEL_REMINDERS_V2 = "channel_reminders_v2"
    const val CHANNEL_HABITS_V2 = "channel_habits_v2"
    const val CHANNEL_FOCUS_V2 = "channel_focus_v2"
    const val CHANNEL_QURAN_V2 = "channel_quran_v2"
    const val CHANNEL_AZKAR_V2 = "channel_azkar_v2"
    const val CHANNEL_EXERCISE_V2 = "channel_exercise_v2"
    const val CHANNEL_AI_V2 = "channel_ai_v2"
    const val CHANNEL_SYSTEM_V2 = "channel_system_v2"

    // Backward-compatibility aliases
    const val CHANNEL_PRAYERS = CHANNEL_PRAYERS_V2
    const val CHANNEL_QURAN = CHANNEL_QURAN_V2
    const val CHANNEL_AZKAR = CHANNEL_AZKAR_V2
    const val CHANNEL_EXERCISE = CHANNEL_EXERCISE_V2
    const val CHANNEL_TASKS = CHANNEL_TASKS_V2
    const val CHANNEL_CALENDAR = "channel_calendar_v2"
    const val CHANNEL_FOCUS = CHANNEL_FOCUS_V2
    const val CHANNEL_AI = CHANNEL_AI_V2
    const val CHANNEL_SYSTEM = CHANNEL_SYSTEM_V2

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_PRAYERS_V2,
                    "مواقيت الصلاة والأذان (Prayers & Adhan)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "تنبيهات دخول أوقات الصلوات الخمس والتذكير المسبق والأذان"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 400, 200, 400)
                    setSound(defaultSoundUri, audioAttributes)
                },
                NotificationChannel(
                    CHANNEL_TASKS_V2,
                    "تذكيرات المهام (Task Reminders)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "تذكيرات بدء المهام والمواعيد النهائية الهامة"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 150, 250)
                    setSound(defaultSoundUri, audioAttributes)
                },
                NotificationChannel(
                    CHANNEL_REMINDERS_V2,
                    "التذكيرات العامة (General Reminders)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "تذكيرات المواعيد والجداول الزمنية العامة"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 150, 250)
                    setSound(defaultSoundUri, audioAttributes)
                },
                NotificationChannel(
                    CHANNEL_HABITS_V2,
                    "العادات والروتين (Habits & Routines)",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "تذكيرات بناء العادات والروتين اليومي"
                    enableVibration(true)
                    setSound(defaultSoundUri, audioAttributes)
                },
                NotificationChannel(
                    CHANNEL_FOCUS_V2,
                    "جلسات التركيز (Focus Mode)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "إشعارات فترات التركيز وبدء الاستراحات"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 150, 300)
                    setSound(defaultSoundUri, audioAttributes)
                },
                NotificationChannel(
                    CHANNEL_QURAN_V2,
                    "ورد وآيات القرآن (Quran)",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "تذكير بالورد القرآني اليومي وآيات التدبر"
                    enableVibration(true)
                    setSound(defaultSoundUri, audioAttributes)
                },
                NotificationChannel(
                    CHANNEL_AZKAR_V2,
                    "أذكار اليوم (Daily Azkar)",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "أذكار الصباح والمساء وتذكيرات الذكر المتفرقة"
                    enableVibration(true)
                    setSound(defaultSoundUri, audioAttributes)
                },
                NotificationChannel(
                    CHANNEL_EXERCISE_V2,
                    "الرياضة واللياقة (Exercise)",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "مواعيد التمارين والنشاط البدني"
                    enableVibration(true)
                    setSound(defaultSoundUri, audioAttributes)
                },
                NotificationChannel(
                    CHANNEL_AI_V2,
                    "اقتراحات مساعد وقتي (AI Assistant)",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "توصيات ذكية لإعادة تنظيم اليوم وتحسين الإنتاجية"
                    enableVibration(false)
                    setSound(defaultSoundUri, audioAttributes)
                },
                NotificationChannel(
                    CHANNEL_SYSTEM_V2,
                    "تنبيهات النظام (System)",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "تنبيهات النسخ الاحتياطي وإدارة النظام"
                    enableVibration(false)
                }
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
