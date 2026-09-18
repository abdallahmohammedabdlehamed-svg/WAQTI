package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

object WaqtiNotificationChannels {

    const val CHANNEL_PRAYERS = "channel_prayers"
    const val CHANNEL_QURAN = "channel_quran"
    const val CHANNEL_AZKAR = "channel_azkar"
    const val CHANNEL_EXERCISE = "channel_exercise"
    const val CHANNEL_TASKS = "channel_tasks"
    const val CHANNEL_CALENDAR = "channel_calendar"
    const val CHANNEL_FOCUS = "channel_focus"
    const val CHANNEL_AI = "channel_ai"
    const val CHANNEL_SYSTEM = "channel_system"

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
                    CHANNEL_PRAYERS,
                    "مواقيت الصلاة (Prayers)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "تنبيهات دخول أوقات الصلوات الخمس والتذكير المسبق"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 250, 500)
                    setSound(defaultSoundUri, audioAttributes)
                },
                NotificationChannel(
                    CHANNEL_QURAN,
                    "ورد وآيات القرآن (Quran)",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "تذكير بالورد اليومي وآية اليوم المختارة من المصحف"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_AZKAR,
                    "أذكار اليوم (Daily Azkar & Dhikr)",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "أذكار الصباح والمساء وتذكيرات الذكر المتفرقة"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_EXERCISE,
                    "الرياضة واللياقة (Exercise)",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "مواعيد التمارين والنشاط البدني وإعادة الجدولة الذكية"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_TASKS,
                    "المهام اليومية (Tasks)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "تذكيرات بدء المهام والمواعيد النهائية الهامة"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_CALENDAR,
                    "أحداث التقويم (Calendar)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "تنبيهات المواعيد والاجتماعات المجدولة"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_FOCUS,
                    "جلسات التركيز (Focus)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "إشعارات انتهاء جلسات البومودورو والعمل العميق"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_AI,
                    "اقتراحات وقتي الذكية (AI Suggestions)",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "تحليلات الإنتاجية وتوصيات إعادة تنظيم اليوم"
                    enableVibration(false)
                },
                NotificationChannel(
                    CHANNEL_SYSTEM,
                    "تنبيهات النظام (System)",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "تنبيهات الحساب والنسخ الاحتياطي"
                    enableVibration(false)
                }
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
