package com.example.data.notification

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * WAQTI Smart Notification Preference per category
 */
@Entity(tableName = "notification_preferences")
data class NotificationPreferenceEntity(
    @PrimaryKey val id: String, // e.g. "PRAYER", "MORNING_AZKAR", "EVENING_AZKAR", "QURAN_ROUTINE", "QURAN_VERSE", "DHIKR", "EXERCISE", "HABITS", "TASKS", "CALENDAR", "FOCUS", "SLEEP", "AI_SUGGESTIONS", "SYSTEM"
    val userId: String = "default",
    val category: String,
    val enabled: Boolean = true,
    val frequency: String = "DAILY", // DAILY, TWICE_DAILY, THRICE_DAILY, LOW, MEDIUM, HIGH, AS_NEEDED
    val preferredTime: String = "07:00",
    val startTime: String = "09:00",
    val endTime: String = "21:00",
    val reminderOffsetMinutes: Int = 15,
    val notifyAtTime: Boolean = true,
    val notifyAfterTime: Boolean = false,
    val maxPerDay: Int = 3,
    val sound: Boolean = true,
    val soundTone: String = "DEFAULT",
    val vibration: Boolean = true,
    val quietHoursBehavior: String = "SUPPRESS", // SUPPRESS, ALLOW, DELAY
    val extraJson: String = "", // For specific configs like workout days, prayer offsets, etc.
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Scheduled notification record in the engine
 */
@Entity(tableName = "notification_schedules")
data class NotificationScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "default",
    val type: String,
    val title: String,
    val body: String,
    val scheduledAt: Long,
    val timezone: String = "UTC",
    val priority: String = "NORMAL", // CRITICAL, HIGH, NORMAL, LOW
    val status: String = "SCHEDULED", // SCHEDULED, DELIVERED, OPENED, DISMISSED, SNOOZED, CANCELLED
    val sourceEntityType: String = "",
    val sourceEntityId: String = "",
    val channel: String = "channel_tasks",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Historical logs for notification analytics and fatigue detection
 */
@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "default",
    val notificationId: Long,
    val category: String = "TASKS",
    val deliveredAt: Long = System.currentTimeMillis(),
    val openedAt: Long? = null,
    val action: String? = null, // OPENED, DISMISSED, SNOOZED, COMPLETED, RESCHEDULED, SKIPPED
    val dismissedAt: Long? = null,
    val snoozedUntil: Long? = null
)

/**
 * Detailed Prayer Notification Configuration
 */
data class SinglePrayerConfig(
    val name: String, // Fajr, Dhuhr, Asr, Maghrib, Isha
    val nameAr: String,
    val enabled: Boolean = true,
    val beforePrayerMinutes: Int = 15, // 0 means disabled, 5, 10, 15, 20, 30
    val atPrayerTime: Boolean = true,
    val afterPrayer: Boolean = false,
    val sound: String = "DEFAULT", // DEFAULT, ADHAN, CALM
    val vibration: Boolean = true
)

/**
 * Prayer calculation & Location Settings
 */
data class PrayerLocationAndCalcSettings(
    val isAutoLocation: Boolean = true,
    val country: String = "مصر (Egypt)",
    val cityName: String = "القاهرة",
    val city: String = "القاهرة (Cairo)",
    val latitude: Double = 30.0444,
    val longitude: Double = 31.2357,
    val calcMethod: String = "EGYPTIAN",
    val calculationMethod: String = "EGYPTIAN",
    val fajrEnabled: Boolean = true,
    val dhuhrEnabled: Boolean = true,
    val asrEnabled: Boolean = true,
    val maghribEnabled: Boolean = true,
    val ishaEnabled: Boolean = true,
    val asrMethod: String = "STANDARD",
    val fajrOffset: Int = 0,
    val dhuhrOffset: Int = 0,
    val asrOffset: Int = 0,
    val maghribOffset: Int = 0,
    val ishaOffset: Int = 0,
    val is24HourFormat: Boolean = false
)

/**
 * Global Advanced Settings
 */
data class AdvancedNotificationSettings(
    val maxDailyCap: Int = 15,
    val maxNotificationsPerDay: Int = 15,
    val collisionMinSpacingMinutes: Int = 20,
    val minGapBetweenNotificationsMinutes: Int = 20,
    val collisionPreventionEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = true,
    val quietHoursStart: String = "23:00",
    val quietHoursEnd: String = "06:00",
    val quietHoursAllowPrayers: Boolean = true,
    val quietHoursAllowCritical: Boolean = true,
    val lockScreenPrivacy: String = "SHOW_FULL",
    val fridaySpecialEnabled: Boolean = true,
    val fridayKahfReminder: Boolean = true,
    val fridayDuroodReminder: Boolean = true,
    val weekendScheduleEnabled: Boolean = true,
    val fatigueProtectionEnabled: Boolean = true,
    val exactAlarmsPreferred: Boolean = true
)

object NotificationCategory {
    const val PRAYER = "PRAYER"
    const val MORNING_AZKAR = "MORNING_AZKAR"
    const val EVENING_AZKAR = "EVENING_AZKAR"
    const val QURAN_ROUTINE = "QURAN_ROUTINE"
    const val QURAN = "QURAN"
    const val QURAN_VERSE = "QURAN_VERSE"
    const val DHIKR = "DHIKR"
    const val EXERCISE = "EXERCISE"
    const val WATER = "WATER"
    const val BREAK = "BREAK"
    const val SLEEP = "SLEEP"
    const val TASKS = "TASKS"
    const val TASK = "TASK"
    const val DEADLINES = "DEADLINES"
    const val DEADLINE = "DEADLINE"
    const val CALENDAR = "CALENDAR"
    const val FOCUS = "FOCUS"
    const val HABITS = "HABITS"
    const val HABIT = "HABIT"
    const val AI_SUGGESTIONS = "AI_SUGGESTIONS"
    const val AI_SUGGESTION = "AI_SUGGESTION"
}

object NotificationSoundTone {
    const val DEFAULT = "DEFAULT"
    const val AZAN_FULL = "AZAN_FULL"
    const val AZAN_TAKBEER = "AZAN_TAKBEER"
    const val QUIET_CHIME = "QUIET_CHIME"
    const val SOFT_CHIME = "SOFT_CHIME"
    const val SILENT = "SILENT"
    const val VIBRATE_ONLY = "VIBRATE_ONLY"
}

val NotificationPreferenceEntity.isEnabled: Boolean
    get() = enabled

val NotificationPreferenceEntity.tone: String
    get() = soundTone

val NotificationPreferenceEntity.timingMinutesBefore: Int
    get() = reminderOffsetMinutes

val NotificationPreferenceEntity.displayNameAr: String
    get() = when (category) {
        "PRAYER" -> "الصلوات الخمس والأذان"
        "MORNING_AZKAR" -> "أذكار الصباح"
        "EVENING_AZKAR" -> "أذكار المساء"
        "QURAN_ROUTINE", "QURAN" -> "الورد القرآني"
        "QURAN_VERSE" -> "آية اليوم وتدبر"
        "DHIKR" -> "تذكير الأذكار والتسبيح"
        "EXERCISE" -> "الرياضة والنشاط البدني"
        "WATER" -> "تذكير شرب الماء"
        "BREAK" -> "استراحات العمل والتنفس"
        "SLEEP" -> "روتين النوم والراحة"
        "TASKS", "TASK" -> "المهام اليومية"
        "DEADLINES", "DEADLINE" -> "المواعيد النهائية"
        "CALENDAR" -> "أحداث التقويم"
        "FOCUS" -> "جلسات التركيز والإنتاجية"
        "HABITS", "HABIT" -> "بناء ومتابعة العادات"
        "AI_SUGGESTIONS", "AI_SUGGESTION" -> "اقتراحات الذكاء الاصطناعي الذكية"
        else -> category
    }

val NotificationPreferenceEntity.displayNameEn: String
    get() = when (category) {
        "PRAYER" -> "Five Daily Prayers & Azan"
        "MORNING_AZKAR" -> "Morning Azkar"
        "EVENING_AZKAR" -> "Evening Azkar"
        "QURAN_ROUTINE", "QURAN" -> "Quran Routine"
        "QURAN_VERSE" -> "Quran Verse Reflection"
        "DHIKR" -> "Dhikr & Tasbeeh"
        "EXERCISE" -> "Workout & Exercise"
        "WATER" -> "Water Hydration"
        "BREAK" -> "Productivity Breaks"
        "SLEEP" -> "Sleep Routine & Rest"
        "TASKS", "TASK" -> "Daily Tasks"
        "DEADLINES", "DEADLINE" -> "Deadlines & Milestones"
        "CALENDAR" -> "Calendar Events"
        "FOCUS" -> "Focus & Deep Work Sessions"
        "HABITS", "HABIT" -> "Habits Tracking"
        "AI_SUGGESTIONS", "AI_SUGGESTION" -> "AI Smart Suggestions"
        else -> category
    }

val NotificationScheduleEntity.scheduledEpochMillis: Long
    get() = scheduledAt

val NotificationScheduleEntity.category: String
    get() = type

val NotificationScheduleEntity.titleAr: String
    get() = title

val NotificationScheduleEntity.titleEn: String
    get() = title

val NotificationScheduleEntity.bodyAr: String
    get() = body

val NotificationScheduleEntity.bodyEn: String
    get() = body
