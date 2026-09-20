package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TaskEntity::class,
        RoutineEntity::class,
        HabitEntity::class,
        FocusSessionEntity::class,
        UserEntity::class,
        com.example.data.notification.NotificationPreferenceEntity::class,
        com.example.data.notification.NotificationScheduleEntity::class,
        com.example.data.notification.NotificationLogEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class WaqtiDatabase : RoomDatabase() {
    abstract fun waqtiDao(): WaqtiDao

    companion object {
        @Volatile
        private var INSTANCE: WaqtiDatabase? = null

        fun getInstance(context: Context): WaqtiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaqtiDatabase::class.java,
                    "waqti_database.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with realistic starter data
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).waqtiDao()
                            populateInitialData(dao)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(dao: WaqtiDao) {
            // Initial Active User
            dao.insertUser(
                UserEntity(
                    id = "user_default_01",
                    name = "عبدالله محمد",
                    email = "abdallahmohammedabdlehamed@gmail.com",
                    passwordHash = "123456",
                    plan = "PRO",
                    isActive = true
                )
            )
            // Initial Planned Tasks
            val initialTasks = listOf(
                TaskEntity(
                    title = "العمل على الـPortfolio",
                    description = "استكمال صفحات المشاريع وتحديث تصاميم الهوية الشخصية",
                    status = "IN_PROGRESS",
                    priority = "HIGH",
                    category = "WORK",
                    projectName = "Personal Portfolio",
                    goalName = "Career Growth",
                    durationMinutes = 45,
                    startTime = "09:00",
                    endTime = "09:45",
                    isProtected = false,
                    subtasksRaw = "تصميم الصفحة الرئيسية\nإضافة صفحة المشاريع\nتحديث السيرة الذاتية"
                ),
                TaskEntity(
                    title = "مراجعة كود JavaScript",
                    description = "حل تحديات البرمجة غير المتزامنة وتطبيقات الـPromises",
                    status = "PLANNED",
                    priority = "MEDIUM",
                    category = "STUDY",
                    projectName = "JS Mastery",
                    goalName = "Become Pro Developer",
                    durationMinutes = 60,
                    startTime = "10:00",
                    endTime = "11:00",
                    isProtected = false,
                    subtasksRaw = "مراجعة Async/Await\nتطبيق كود Fetch API"
                ),
                TaskEntity(
                    title = "اجتماع فريق التصميم الأسبوعي",
                    description = "مناقشة أفكار واجهة المستخدم الجديدة ونظام المكونات",
                    status = "PLANNED",
                    priority = "HIGH",
                    category = "WORK",
                    projectName = "WAQTI Platform",
                    durationMinutes = 45,
                    startTime = "11:30",
                    endTime = "12:15",
                    isProtected = true // Meeting is fixed/protected
                ),
                TaskEntity(
                    title = "تطوير واجهة متجر إلكتروني",
                    description = "بناء نظام سلة المشتريات وإتمام الدفع",
                    status = "PLANNED",
                    priority = "HIGH",
                    category = "WORK",
                    projectName = "E-Commerce App",
                    durationMinutes = 75,
                    startTime = "13:30",
                    endTime = "14:45",
                    isProtected = false
                ),
                TaskEntity(
                    title = "إعداد تقرير الإنتاجية والملخص الأسبوعي",
                    description = "مراجعة ما تم إنجازه وجدولة أولويات الأسبوع القادم",
                    status = "PLANNED",
                    priority = "LOW",
                    category = "PERSONAL",
                    durationMinutes = 30,
                    startTime = "16:15",
                    endTime = "16:45",
                    isProtected = false
                ),
                // Completed earlier today
                TaskEntity(
                    title = "تخطيط أولويات الصباح وتفريغ الأفكار",
                    description = "تحديد المهام الرئيسية وترتيبها في الجدول",
                    status = "COMPLETED",
                    priority = "MEDIUM",
                    category = "PERSONAL",
                    durationMinutes = 20,
                    startTime = "07:30",
                    endTime = "07:50",
                    isProtected = false
                )
            )
            if (dao.getTaskCount() == 0) {
                dao.insertTasks(initialTasks)
            }

            // Initial Daily Program & Routines (Life OS + Worship + Health)
            val initialRoutines = listOf(
                RoutineEntity(
                    title = "Fajr Prayer",
                    titleAr = "صلاة الفجر",
                    category = "WORSHIP",
                    type = "FIXED",
                    time = "05:05",
                    durationMinutes = 20,
                    isProtected = true,
                    isCompleted = true,
                    streak = 14
                ),
                RoutineEntity(
                    title = "Morning Azkar",
                    titleAr = "أذكار الصباح",
                    category = "WORSHIP",
                    type = "FLEXIBLE",
                    time = "06:20",
                    durationMinutes = 15,
                    isProtected = false,
                    isCompleted = true,
                    streak = 18
                ),
                RoutineEntity(
                    title = "Healthy Breakfast & Coffee",
                    titleAr = "إفطار صحي وقهوة الصباح",
                    category = "PERSONAL",
                    type = "FIXED",
                    time = "07:00",
                    durationMinutes = 30,
                    isProtected = false,
                    isCompleted = true,
                    streak = 7
                ),
                RoutineEntity(
                    title = "Dhuhr Prayer",
                    titleAr = "صلاة الظهر",
                    category = "WORSHIP",
                    type = "FIXED",
                    time = "12:15",
                    durationMinutes = 20,
                    isProtected = true,
                    isCompleted = false,
                    streak = 12
                ),
                RoutineEntity(
                    title = "Lunch Break",
                    titleAr = "استراحة الغداء",
                    category = "PERSONAL",
                    type = "FLEXIBLE",
                    time = "12:45",
                    durationMinutes = 35,
                    isProtected = false,
                    isCompleted = false,
                    streak = 10
                ),
                RoutineEntity(
                    title = "Asr Prayer",
                    titleAr = "صلاة العصر",
                    category = "WORSHIP",
                    type = "FIXED",
                    time = "15:40",
                    durationMinutes = 20,
                    isProtected = true,
                    isCompleted = false,
                    streak = 14
                ),
                RoutineEntity(
                    title = "Fitness Workout",
                    titleAr = "تمرين رياضي ولياقة",
                    category = "HEALTH",
                    type = "FLEXIBLE",
                    time = "17:00",
                    durationMinutes = 45,
                    isProtected = false,
                    isCompleted = false,
                    streak = 5
                ),
                RoutineEntity(
                    title = "Maghrib Prayer",
                    titleAr = "صلاة المغرب",
                    category = "WORSHIP",
                    type = "FIXED",
                    time = "18:15",
                    durationMinutes = 20,
                    isProtected = true,
                    isCompleted = false,
                    streak = 14
                ),
                RoutineEntity(
                    title = "Quran Routine (20 min)",
                    titleAr = "ورد القرآن الكريم (20 دقيقة)",
                    category = "WORSHIP",
                    type = "FLEXIBLE",
                    time = "18:40",
                    durationMinutes = 20,
                    isProtected = false,
                    isCompleted = false,
                    streak = 9
                ),
                RoutineEntity(
                    title = "Isha Prayer",
                    titleAr = "صلاة العشاء",
                    category = "WORSHIP",
                    type = "FIXED",
                    time = "19:35",
                    durationMinutes = 20,
                    isProtected = true,
                    isCompleted = false,
                    streak = 14
                ),
                RoutineEntity(
                    title = "Evening Azkar",
                    titleAr = "أذكار المساء",
                    category = "WORSHIP",
                    type = "FLEXIBLE",
                    time = "20:00",
                    durationMinutes = 15,
                    isProtected = false,
                    isCompleted = false,
                    streak = 11
                ),
                RoutineEntity(
                    title = "Reading Book",
                    titleAr = "قراءة كتاب معرفي",
                    category = "PERSONAL",
                    type = "OPTIONAL",
                    time = "21:30",
                    durationMinutes = 30,
                    isProtected = false,
                    isCompleted = false,
                    streak = 6
                )
            )
            if (dao.getRoutineCount() == 0) {
                dao.insertRoutines(initialRoutines)
            }

            // Initial Habits
            val initialHabits = listOf(
                HabitEntity(
                    title = "Read 30 minutes",
                    titleAr = "قراءة 30 دقيقة",
                    target = "30 min",
                    streak = 12,
                    bestStreak = 24,
                    completedToday = false
                ),
                HabitEntity(
                    title = "Drink 2.5L Water",
                    titleAr = "شرب 2.5 لتر ماء",
                    target = "2.5 Liters",
                    streak = 19,
                    bestStreak = 30,
                    completedToday = true
                ),
                HabitEntity(
                    title = "Daily Quran Routine",
                    titleAr = "ورد القرآن اليومي",
                    target = "20 min",
                    streak = 9,
                    bestStreak = 45,
                    completedToday = false
                ),
                HabitEntity(
                    title = "5,000 Walking Steps",
                    titleAr = "المشي 5000 خطوة",
                    target = "5,000 steps",
                    streak = 7,
                    bestStreak = 15,
                    completedToday = false
                )
            )
            dao.insertHabits(initialHabits)

            // Initial Focus Sessions (totaling ~135 minutes = 2h 15m)
            dao.insertFocusSession(FocusSessionEntity(taskTitle = "تخطيط أولويات الصباح", durationMinutes = 20, mode = "POMODORO"))
            dao.insertFocusSession(FocusSessionEntity(taskTitle = "العمل على الـPortfolio", durationMinutes = 45, mode = "DEEP_WORK"))
            dao.insertFocusSession(FocusSessionEntity(taskTitle = "مراجعة كود JavaScript", durationMinutes = 70, mode = "DEEP_WORK"))

            // Initial Default Notification Preferences (All 14 categories)
            val defaultNotificationPreferences = listOf(
                com.example.data.notification.NotificationPreferenceEntity(
                    id = "PRAYER",
                    userId = "user_default_01",
                    category = "PRAYER",
                    enabled = true,
                    frequency = "DAILY",
                    reminderOffsetMinutes = 15,
                    notifyAtTime = true,
                    notifyAfterTime = false,
                    maxPerDay = 5,
                    sound = true,
                    soundTone = "ADHAN",
                    vibration = true,
                    quietHoursBehavior = "ALLOW"
                ),
                com.example.data.notification.NotificationPreferenceEntity(
                    id = "MORNING_AZKAR",
                    userId = "user_default_01",
                    category = "MORNING_AZKAR",
                    enabled = true,
                    preferredTime = "07:00",
                    reminderOffsetMinutes = 10,
                    notifyAtTime = true,
                    maxPerDay = 1,
                    soundTone = "DEFAULT"
                ),
                com.example.data.notification.NotificationPreferenceEntity(
                    id = "EVENING_AZKAR",
                    userId = "user_default_01",
                    category = "EVENING_AZKAR",
                    enabled = true,
                    preferredTime = "18:00",
                    reminderOffsetMinutes = 10,
                    notifyAtTime = true,
                    maxPerDay = 1,
                    soundTone = "DEFAULT"
                ),
                com.example.data.notification.NotificationPreferenceEntity(
                    id = "QURAN_ROUTINE",
                    userId = "user_default_01",
                    category = "QURAN_ROUTINE",
                    enabled = true,
                    preferredTime = "19:30",
                    startTime = "19:00",
                    endTime = "22:00",
                    reminderOffsetMinutes = 15,
                    maxPerDay = 1
                ),
                com.example.data.notification.NotificationPreferenceEntity(
                    id = "QURAN_VERSE",
                    userId = "user_default_01",
                    category = "QURAN_VERSE",
                    enabled = true,
                    frequency = "TWICE_DAILY",
                    startTime = "10:00",
                    endTime = "20:00",
                    maxPerDay = 2,
                    quietHoursBehavior = "SUPPRESS"
                ),
                com.example.data.notification.NotificationPreferenceEntity(
                    id = "DHIKR",
                    userId = "user_default_01",
                    category = "DHIKR",
                    enabled = true,
                    frequency = "MEDIUM",
                    startTime = "09:00",
                    endTime = "21:00",
                    maxPerDay = 3,
                    quietHoursBehavior = "SUPPRESS"
                ),
                com.example.data.notification.NotificationPreferenceEntity(
                    id = "EXERCISE",
                    userId = "user_default_01",
                    category = "EXERCISE",
                    enabled = true,
                    preferredTime = "18:00",
                    reminderOffsetMinutes = 15,
                    maxPerDay = 1
                ),
                com.example.data.notification.NotificationPreferenceEntity(
                    id = "HABITS",
                    userId = "user_default_01",
                    category = "HABITS",
                    enabled = true,
                    frequency = "DAILY",
                    maxPerDay = 2
                ),
                com.example.data.notification.NotificationPreferenceEntity(
                    id = "TASKS",
                    userId = "user_default_01",
                    category = "TASKS",
                    enabled = true,
                    frequency = "AS_NEEDED",
                    reminderOffsetMinutes = 15,
                    maxPerDay = 8,
                    quietHoursBehavior = "DELAY"
                ),
                com.example.data.notification.NotificationPreferenceEntity(
                    id = "CALENDAR",
                    userId = "user_default_01",
                    category = "CALENDAR",
                    enabled = true,
                    frequency = "AS_NEEDED",
                    reminderOffsetMinutes = 10,
                    maxPerDay = 5
                ),
                com.example.data.notification.NotificationPreferenceEntity(
                    id = "FOCUS",
                    userId = "user_default_01",
                    category = "FOCUS",
                    enabled = true,
                    frequency = "AS_NEEDED",
                    maxPerDay = 4
                ),
                com.example.data.notification.NotificationPreferenceEntity(
                    id = "SLEEP",
                    userId = "user_default_01",
                    category = "SLEEP",
                    enabled = true,
                    preferredTime = "23:00",
                    reminderOffsetMinutes = 30,
                    maxPerDay = 1
                ),
                com.example.data.notification.NotificationPreferenceEntity(
                    id = "AI_SUGGESTIONS",
                    userId = "user_default_01",
                    category = "AI_SUGGESTIONS",
                    enabled = true,
                    frequency = "LOW",
                    maxPerDay = 2,
                    quietHoursBehavior = "SUPPRESS"
                ),
                com.example.data.notification.NotificationPreferenceEntity(
                    id = "SYSTEM",
                    userId = "user_default_01",
                    category = "SYSTEM",
                    enabled = true,
                    frequency = "AS_NEEDED",
                    maxPerDay = 2
                )
            )
            dao.insertNotificationPreferences(defaultNotificationPreferences)
        }
    }
}
