package com.example.notification

import android.content.Context
import android.util.Log
import com.example.data.local.TaskEntity
import com.example.data.local.WaqtiDatabase
import com.example.data.notification.AdvancedNotificationSettings
import com.example.data.notification.NotificationPreferenceEntity
import com.example.data.notification.NotificationScheduleEntity
import com.example.data.notification.PrayerLocationAndCalcSettings
import com.example.data.prayer.PrayerCalculator
import com.example.data.spiritual.QuranAzkarData
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SmartNotificationEngine {

    private const val TAG = "SmartNotificationEngine"

    /**
     * Recalculates and schedules the day's notifications according to smart rules:
     * - Respects category preferences & toggles
     * - Respects quiet hours & exceptions
     * - Enforces daily limits and priority order
     * - Avoids collision by spacing notifications
     * - Suppresses already-completed tasks/routines
     */
    suspend fun recalculateAndScheduleAll(
        context: Context,
        advancedSettings: AdvancedNotificationSettings = AdvancedNotificationSettings(),
        prayerCalcSettings: PrayerLocationAndCalcSettings = PrayerLocationAndCalcSettings()
    ): List<NotificationScheduleEntity> {
        val db = WaqtiDatabase.getInstance(context)
        val dao = db.waqtiDao()

        val activeUser = dao.getActiveUser()
        val userId = activeUser?.id ?: "user_default_01"

        val prefs = dao.getNotificationPreferences(userId).firstOrNull() ?: emptyList()
        val prefMap = prefs.associateBy { it.category }

        val tasks = dao.getPlannedTasksForUser(userId).firstOrNull() ?: dao.getPlannedTasks().firstOrNull() ?: emptyList()
        val routines = dao.getAllRoutines().firstOrNull() ?: emptyList()

        // Clear existing scheduled notifications to avoid duplicates
        dao.clearSchedulesForUser(userId)

        val calendar = Calendar.getInstance()
        val currentMillis = System.currentTimeMillis()
        val todayStartMillis = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val candidateSchedules = mutableListOf<NotificationScheduleEntity>()

        // 1. PRAYER NOTIFICATIONS (Critical & High Priority)
        val prayerPref = prefMap["PRAYER"]
        if (prayerPref?.enabled != false) {
            val prayerTimes = PrayerCalculator.getPrayerTimes(Date(), prayerCalcSettings)
            prayerTimes.forEach { p ->
                val prayerCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, p.rawMinutesOfDay / 60)
                    set(Calendar.MINUTE, p.rawMinutesOfDay % 60)
                    set(Calendar.SECOND, 0)
                }

                // 1a. Before Prayer reminder (e.g. 15 minutes before)
                val offsetMins = prayerPref?.reminderOffsetMinutes ?: 15
                if (offsetMins > 0) {
                    val beforeMillis = prayerCal.timeInMillis - (offsetMins * 60 * 1000)
                    if (beforeMillis > currentMillis) {
                        candidateSchedules.add(
                            NotificationScheduleEntity(
                                userId = userId,
                                type = "PRAYER",
                                title = "اقتربت صلاة ${p.nameAr} 🕌",
                                body = "متبقي $offsetMins دقيقة على أذان ${p.nameAr}. استعد للوضوء وصلاة الجماعة.",
                                scheduledAt = beforeMillis,
                                priority = "HIGH",
                                channel = WaqtiNotificationChannels.CHANNEL_PRAYERS,
                                sourceEntityType = "PRAYER_BEFORE",
                                sourceEntityId = p.nameEn
                            )
                        )
                    }
                }

                // 1b. At Prayer Time
                if (prayerPref?.notifyAtTime != false) {
                    val atTimeMillis = prayerCal.timeInMillis
                    if (atTimeMillis > currentMillis) {
                        candidateSchedules.add(
                            NotificationScheduleEntity(
                                userId = userId,
                                type = "PRAYER",
                                title = "حان الآن وقت صلاة ${p.nameAr} 🕌",
                                body = "الله أكبر، الله أكبر. حان الآن موعد أذان ${p.nameAr} حسب توقيت ${prayerCalcSettings.city}.",
                                scheduledAt = atTimeMillis,
                                priority = "CRITICAL",
                                channel = WaqtiNotificationChannels.CHANNEL_PRAYERS,
                                sourceEntityType = "PRAYER_EXACT",
                                sourceEntityId = p.nameEn
                            )
                        )
                    }
                }
            }
        }

        // 2. MORNING AZKAR
        val morningAzkarPref = prefMap["MORNING_AZKAR"]
        val morningRoutine = routines.find { it.title.contains("Morning Azkar", ignoreCase = true) || it.titleAr.contains("أذكار الصباح") }
        val isMorningCompleted = morningRoutine?.isCompleted == true

        if (morningAzkarPref?.enabled != false && !isMorningCompleted) {
            val timeParts = (morningAzkarPref?.preferredTime ?: "07:00").split(":")
            val h = timeParts.getOrNull(0)?.toIntOrNull() ?: 7
            val m = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            val azkarTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            if (azkarTime > currentMillis) {
                candidateSchedules.add(
                    NotificationScheduleEntity(
                        userId = userId,
                        type = "MORNING_AZKAR",
                        title = "حان وقت أذكار الصباح 🌿",
                        body = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ.. ابدأ يومك بسكينة وتوكل على الله.",
                        scheduledAt = azkarTime,
                        priority = "NORMAL",
                        channel = WaqtiNotificationChannels.CHANNEL_AZKAR,
                        sourceEntityType = "ROUTINE",
                        sourceEntityId = "morning_azkar"
                    )
                )
            }
        }

        // 3. EVENING AZKAR
        val eveningAzkarPref = prefMap["EVENING_AZKAR"]
        val eveningRoutine = routines.find { it.title.contains("Evening Azkar", ignoreCase = true) || it.titleAr.contains("أذكار المساء") }
        val isEveningCompleted = eveningRoutine?.isCompleted == true

        if (eveningAzkarPref?.enabled != false && !isEveningCompleted) {
            val timeParts = (eveningAzkarPref?.preferredTime ?: "18:00").split(":")
            val h = timeParts.getOrNull(0)?.toIntOrNull() ?: 18
            val m = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            val azkarTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            if (azkarTime > currentMillis) {
                candidateSchedules.add(
                    NotificationScheduleEntity(
                        userId = userId,
                        type = "EVENING_AZKAR",
                        title = "حان وقت أذكار المساء 🌙",
                        body = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ.. حصن نفسك واختم نهارك بذكر الله وحمده.",
                        scheduledAt = azkarTime,
                        priority = "NORMAL",
                        channel = WaqtiNotificationChannels.CHANNEL_AZKAR,
                        sourceEntityType = "ROUTINE",
                        sourceEntityId = "evening_azkar"
                    )
                )
            }
        }

        // 4. QURAN ROUTINE
        val quranPref = prefMap["QURAN_ROUTINE"]
        val quranRoutine = routines.find { it.title.contains("Quran", ignoreCase = true) || it.titleAr.contains("القرآن") }
        val isQuranCompleted = quranRoutine?.isCompleted == true

        if (quranPref?.enabled != false && !isQuranCompleted) {
            val timeParts = (quranPref?.preferredTime ?: "19:30").split(":")
            val h = timeParts.getOrNull(0)?.toIntOrNull() ?: 19
            val m = timeParts.getOrNull(1)?.toIntOrNull() ?: 30
            val quranTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            if (quranTime > currentMillis) {
                candidateSchedules.add(
                    NotificationScheduleEntity(
                        userId = userId,
                        type = "QURAN_ROUTINE",
                        title = "ورد القرآن الكريم 📖",
                        body = "خصص 20 دقيقة الآن لتلاوة وردك اليومي وتدبر آيات الذكر الحكيم.",
                        scheduledAt = quranTime,
                        priority = "NORMAL",
                        channel = WaqtiNotificationChannels.CHANNEL_QURAN,
                        sourceEntityType = "ROUTINE",
                        sourceEntityId = "quran_routine"
                    )
                )
            }
        }

        // 5. AYAH OF THE MOMENT (آية اليوم - Verified from QuranAzkarData)
        val ayahPref = prefMap["QURAN_VERSE"]
        if (ayahPref?.enabled != false) {
            val verse = QuranAzkarData.authenticVerses.random()
            val ayahTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 11)
                set(Calendar.MINUTE, 15)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            if (ayahTime > currentMillis) {
                candidateSchedules.add(
                    NotificationScheduleEntity(
                        userId = userId,
                        type = "QURAN_VERSE",
                        title = "آية اليوم 🌟 (${verse.surahNameAr} - آية ${verse.ayahNumber})",
                        body = "﴿ ${verse.arabicText} ﴾ — [${verse.reference}]",
                        scheduledAt = ayahTime,
                        priority = "LOW",
                        channel = WaqtiNotificationChannels.CHANNEL_QURAN,
                        sourceEntityType = "VERSE",
                        sourceEntityId = "${verse.surahNameEn}_${verse.ayahNumber}"
                    )
                )
            }
        }

        // 6. OCCASIONAL DHIKR REMINDER (أذكار خلال اليوم)
        val dhikrPref = prefMap["DHIKR"]
        if (dhikrPref?.enabled != false) {
            val phrase = QuranAzkarData.dhikrPhrases.random()
            val dhikrTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 14)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            if (dhikrTime > currentMillis) {
                candidateSchedules.add(
                    NotificationScheduleEntity(
                        userId = userId,
                        type = "DHIKR",
                        title = "تذكير بالذكر 📿",
                        body = phrase.first,
                        scheduledAt = dhikrTime,
                        priority = "LOW",
                        channel = WaqtiNotificationChannels.CHANNEL_AZKAR,
                        sourceEntityType = "DHIKR",
                        sourceEntityId = "dhikr_midday"
                    )
                )
            }
        }

        // 7. EXERCISE REMINDERS (With Smart Reschedule hook)
        val exercisePref = prefMap["EXERCISE"]
        val workoutRoutine = routines.find { it.category == "HEALTH" || it.title.contains("Fitness", ignoreCase = true) || it.titleAr.contains("تمرين") }
        val isWorkoutCompleted = workoutRoutine?.isCompleted == true

        if (exercisePref?.enabled != false && !isWorkoutCompleted) {
            val timeParts = (workoutRoutine?.time ?: exercisePref?.preferredTime ?: "17:00").split(":")
            val h = timeParts.getOrNull(0)?.toIntOrNull() ?: 17
            val m = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            val workoutTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            val offsetMins = exercisePref?.reminderOffsetMinutes ?: 15
            val reminderTime = workoutTime - (offsetMins * 60 * 1000)

            if (reminderTime > currentMillis) {
                candidateSchedules.add(
                    NotificationScheduleEntity(
                        userId = userId,
                        type = "EXERCISE",
                        title = "موعد التمرين الرياضي بعد $offsetMins دقيقة 💪",
                        body = "حافظ على لياقتك ونشاطك البدني، جهز ملابسك الرياضية وابدأ.",
                        scheduledAt = reminderTime,
                        priority = "NORMAL",
                        channel = WaqtiNotificationChannels.CHANNEL_EXERCISE,
                        sourceEntityType = "ROUTINE",
                        sourceEntityId = "workout_reminder"
                    )
                )
            }
        }

        // 8. TASK NOTIFICATIONS (Smart suppression for completed ones)
        val taskPref = prefMap["TASKS"]
        if (taskPref?.enabled != false) {
            val uncompletedTasks = tasks.filter { it.status != "COMPLETED" }
            uncompletedTasks.forEach { task ->
                val timeParts = task.startTime.split(":")
                val h = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
                val m = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

                val taskStartTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                    set(Calendar.SECOND, 0)
                }.timeInMillis

                val offsetMins = taskPref?.reminderOffsetMinutes ?: 15
                val reminderTime = taskStartTime - (offsetMins * 60 * 1000)

                if (reminderTime > currentMillis) {
                    val priorityScore = when (task.priority) {
                        "HIGH" -> "HIGH"
                        "LOW" -> "LOW"
                        else -> "NORMAL"
                    }

                    candidateSchedules.add(
                        NotificationScheduleEntity(
                            userId = userId,
                            type = "TASKS",
                            title = "مهمتك التالية: ${task.title} ⏱️",
                            body = "تبدأ الساعة ${task.startTime} (مدة ${task.durationMinutes} دقيقة). جاهز للتركيز؟",
                            scheduledAt = reminderTime,
                            priority = if (task.isProtected) "CRITICAL" else priorityScore,
                            channel = WaqtiNotificationChannels.CHANNEL_TASKS,
                            sourceEntityType = "TASK",
                            sourceEntityId = task.id.toString()
                        )
                    )
                }
            }
        }

        // 9. FRIDAY SPECIAL REMINDERS (if enabled and today is Friday)
        val isFriday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        if (isFriday && advancedSettings.fridaySpecialEnabled) {
            if (advancedSettings.fridayKahfReminder) {
                val kahfTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis
                if (kahfTime > currentMillis) {
                    candidateSchedules.add(
                        NotificationScheduleEntity(
                            userId = userId,
                            type = "SYSTEM",
                            title = "نورٌ ما بين الجمعتين 📖",
                            body = "تذكير بقراءة سورة الكهف اليوم، جعل الله يومك مباركاً ومليئاً بالسكينة.",
                            scheduledAt = kahfTime,
                            priority = "NORMAL",
                            channel = WaqtiNotificationChannels.CHANNEL_QURAN,
                            sourceEntityType = "FRIDAY",
                            sourceEntityId = "surah_kahf"
                        )
                    )
                }
            }
        }

        // 10. AI PRODUCTIVITY SUMMARY (Night summary)
        val aiPref = prefMap["AI_SUGGESTIONS"]
        if (aiPref?.enabled != false) {
            val nightSummaryTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 21)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis
            if (nightSummaryTime > currentMillis) {
                val completedCount = tasks.count { it.status == "COMPLETED" }
                candidateSchedules.add(
                    NotificationScheduleEntity(
                        userId = userId,
                        type = "AI_SUGGESTIONS",
                        title = "ملخص وقتي اليومي 📊",
                        body = "أنجزت اليوم $completedCount مهام! راجع جدول الغد وحافظ على استمرارية إنتاجيتك.",
                        scheduledAt = nightSummaryTime,
                        priority = "LOW",
                        channel = WaqtiNotificationChannels.CHANNEL_AI,
                        sourceEntityType = "SUMMARY",
                        sourceEntityId = "daily_summary"
                    )
                )
            }
        }

        // 11. QUIET HOURS FILTERING & COLLISION MANAGEMENT
        val filteredSchedules = filterQuietHoursAndLimit(
            schedules = candidateSchedules,
            advancedSettings = advancedSettings
        )

        // Save finalized schedule to database and set alarms
        filteredSchedules.forEach { s ->
            val savedId = dao.insertSchedule(s)
            NotificationScheduler.scheduleNotification(context, s.copy(id = savedId))
        }

        Log.d(TAG, "SmartNotificationEngine successfully scheduled ${filteredSchedules.size} notifications for user $userId")
        return filteredSchedules
    }

    /**
     * Filters candidate notifications based on:
     * - Quiet Hours (suppresses non-prayer/non-critical notifications)
     * - Collision Spacing (enforces minimum gap between flexible reminders)
     * - Daily Maximum Cap (prioritizes Critical and High)
     */
    private fun filterQuietHoursAndLimit(
        schedules: List<NotificationScheduleEntity>,
        advancedSettings: AdvancedNotificationSettings
    ): List<NotificationScheduleEntity> {
        val quietStartHour = advancedSettings.quietHoursStart.split(":").firstOrNull()?.toIntOrNull() ?: 22
        val quietEndHour = advancedSettings.quietHoursEnd.split(":").firstOrNull()?.toIntOrNull() ?: 7

        val nonQuietSchedules = schedules.filter { s ->
            val cal = Calendar.getInstance().apply { timeInMillis = s.scheduledAt }
            val hour = cal.get(Calendar.HOUR_OF_DAY)

            val inQuietHours = if (quietStartHour > quietEndHour) {
                hour >= quietStartHour || hour < quietEndHour
            } else {
                hour in quietStartHour until quietEndHour
            }

            if (inQuietHours) {
                // Allow prayers or critical events during quiet hours if enabled
                val allowPrayer = s.type == "PRAYER" && advancedSettings.quietHoursAllowPrayers
                val allowCritical = s.priority == "CRITICAL" && advancedSettings.quietHoursAllowCritical
                allowPrayer || allowCritical
            } else {
                true
            }
        }

        // Sort by scheduled time
        val sorted = nonQuietSchedules.sortedBy { it.scheduledAt }

        // Spacing / Collision management
        val spaced = mutableListOf<NotificationScheduleEntity>()
        val minGapMillis = advancedSettings.minGapBetweenNotificationsMinutes * 60 * 1000L

        for (s in sorted) {
            val isTimeFixed = s.type == "PRAYER" || s.priority == "CRITICAL"
            if (isTimeFixed || spaced.isEmpty()) {
                spaced.add(s)
            } else {
                val lastScheduled = spaced.last().scheduledAt
                if (Math.abs(s.scheduledAt - lastScheduled) >= minGapMillis) {
                    spaced.add(s)
                } else {
                    // Adjust timing slightly forward if space permits
                    val adjustedTime = lastScheduled + minGapMillis
                    spaced.add(s.copy(scheduledAt = adjustedTime))
                }
            }
        }

        // Enforce daily cap (suppress lowest priority first)
        val maxPerDay = advancedSettings.maxNotificationsPerDay
        if (spaced.size <= maxPerDay) {
            return spaced
        }

        // Prioritized selection
        val prioritized = spaced.sortedWith(
            compareByDescending<NotificationScheduleEntity> {
                when (it.priority) {
                    "CRITICAL" -> 4
                    "HIGH" -> 3
                    "NORMAL" -> 2
                    else -> 1
                }
            }.thenBy { it.scheduledAt }
        ).take(maxPerDay).sortedBy { it.scheduledAt }

        return prioritized
    }

    /**
     * Checks if fatigue alert is recommended based on recent user dismissals/snoozes
     */
    suspend fun checkFatigueStatus(context: Context): Boolean {
        return try {
            val db = WaqtiDatabase.getInstance(context)
            val dao = db.waqtiDao()
            val activeUser = dao.getActiveUser()
            val userId = activeUser?.id ?: "user_default_01"

            val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
            val totalCount = dao.getNotificationCountSince(userId, oneDayAgo)
            val fatigueCount = dao.getFatigueCountSince(userId, oneDayAgo)

            totalCount >= 5 && (fatigueCount.toFloat() / totalCount.toFloat()) > 0.6f
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Creates an immediate test notification to demonstrate the engine, channels, and quick action buttons
     */
    fun sendTestNotification(context: Context, category: String) {
        val schedule = when (category) {
            "PRAYER" -> NotificationScheduleEntity(
                title = "حان الآن وقت صلاة العصر 🕌",
                body = "الله أكبر، الله أكبر. حان الآن موعد أذان العصر بتوقيت القاهرة.",
                scheduledAt = System.currentTimeMillis() + 100,
                type = "PRAYER",
                priority = "CRITICAL",
                channel = WaqtiNotificationChannels.CHANNEL_PRAYERS
            )
            "EXERCISE" -> NotificationScheduleEntity(
                title = "تذكير التمرين الرياضي 💪",
                body = "حان وقت التمرين البدني اليومي (45 دقيقة).",
                scheduledAt = System.currentTimeMillis() + 100,
                type = "EXERCISE",
                priority = "NORMAL",
                channel = WaqtiNotificationChannels.CHANNEL_EXERCISE
            )
            "QURAN_ROUTINE" -> NotificationScheduleEntity(
                title = "ورد القرآن الكريم 📖",
                body = "خصص 20 دقيقة لتلاوة الورد اليومي والتدبر في آيات الله.",
                scheduledAt = System.currentTimeMillis() + 100,
                type = "QURAN_ROUTINE",
                priority = "NORMAL",
                channel = WaqtiNotificationChannels.CHANNEL_QURAN
            )
            else -> NotificationScheduleEntity(
                title = "مهمتك التالية: العمل على الـPortfolio ⏱️",
                body = "متبقي 15 دقيقة على البدء. هل أنت جاهز للتركيز؟",
                scheduledAt = System.currentTimeMillis() + 100,
                type = "TASKS",
                priority = "HIGH",
                channel = WaqtiNotificationChannels.CHANNEL_TASKS
            )
        }

        val intent = android.content.Intent(context, NotificationReceiver::class.java).apply {
            putExtra("schedule_id", System.currentTimeMillis() % 100000)
            putExtra("type", schedule.type)
            putExtra("title", schedule.title)
            putExtra("body", schedule.body)
            putExtra("channel", schedule.channel)
            putExtra("priority", schedule.priority)
        }
        context.sendBroadcast(intent)
    }
}
