package com.example.localization

enum class AppLanguage(val code: String, val displayName: String, val isRtl: Boolean) {
    ARABIC("ar", "العربية", true),
    ENGLISH("en", "English", false)
}

object Strings {
    fun appTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "وقتي" else "WAQTI"
    fun appSlogan(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "وقتك. خطتك. إنجازك." else "YOUR TIME. YOUR PLAN. YOUR PROGRESS."

    // Navigation Tabs
    fun tabHome(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الرئيسية" else "Home"
    fun tabToday(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "يومي" else "Today"
    fun tabProgram(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "برنامجي" else "Program"
    fun tabFocus(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "التركيز" else "Focus"
    fun tabTasks(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المهام" else "Tasks"
    fun tabMore(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المزيد" else "More"

    // Home Screen
    fun greeting(lang: AppLanguage, name: String) = if (lang == AppLanguage.ARABIC) "صباح الخير، $name 👋" else "Good morning, $name 👋"
    fun dailyProgress(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إنجاز اليوم" else "Daily Progress"
    fun tasksCompleted(lang: AppLanguage, done: Int, total: Int) = if (lang == AppLanguage.ARABIC) "المهام: $done / $total" else "Tasks: $done / $total"
    fun focusTime(lang: AppLanguage, time: String) = if (lang == AppLanguage.ARABIC) "التركيز: $time" else "Focus: $time"
    fun currentTask(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المهمة الحالية" else "CURRENT TASK"
    fun nextUp(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "القادم تاليًا" else "Next Up"
    fun aiDailyInsight(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "✨ اقتراح من وقتي" else "✨ WAQTI Daily Insight"
    fun apply(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تطبيق" else "Apply"
    fun dismiss(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تجاهل" else "Dismiss"
    fun askWaqti(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "اسأل مساعد وقتي" else "Ask WAQTI"
    fun smartRescheduleBtn(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "أعد تنظيم يومي" else "Reschedule My Day"
    fun imBehindBtn(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "التأخيرات" else "Delays"
    fun delays(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "التأخيرات" else "Delays"
    fun quickAddTask(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إضافة مهمة +" else "+ Add Task"
    fun timeSpent(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الوقت المنجز" else "Time Spent"
    fun timeRemaining(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الوقت المتبقي" else "Time Left"
    fun urgentTask(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "مهمة عاجلة" else "Urgent Task"
    fun start(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "ابدأ" else "Start"
    fun complete(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إتمام" else "Complete"
    fun reschedule(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تأجيل" else "Reschedule"

    // Today Screen
    fun timelineTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "جدول اليوم الذكي" else "Today's Smart Schedule"
    fun fixedTag(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "وقت ثابت ومحمي" else "Protected Time"
    fun flexibleTag(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "مرن" else "Flexible"
    fun optionalTag(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "اختياري" else "Optional"

    // Spiritual / Life OS Program
    fun myDailyProgram(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "برنامجي اليومي" else "My Daily Program"
    fun nextPrayer(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الصلاة القادمة" else "Next Prayer"
    fun quranRoutine(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "ورد القرآن" else "Quran Routine"
    fun morningAzkar(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "أذكار الصباح" else "Morning Azkar"
    fun eveningAzkar(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "أذكار المساء" else "Evening Azkar"
    fun ayahOfTheMoment(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "آية اليوم" else "Ayah of the Moment"
    fun smartDhikr(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الذكر والتسبيح" else "Dhikr & Remembrance"
    fun createRoutine(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إضافة روتين جديد" else "Create Routine"
    fun routineTemplates(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "قوالب اليوم المتوازن" else "Routine Templates"

    // Focus Mode
    fun focusModeTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "جلسة تركيز عميق" else "Deep Focus Session"
    fun pomodoroMode(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "بومودورو (25د)" else "Pomodoro (25m)"
    fun deepWorkMode(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "عمل عميق (50د)" else "Deep Work (50m)"
    fun customMode(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "مخصص (90د)" else "Extended (90m)"
    fun pause(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إيقاف مؤقت" else "Pause"
    fun resume(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "استئناف" else "Resume"
    fun finish(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إنهاء الجلسة" else "Finish Session"
    fun ambientSounds(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "أصوات هادئة" else "Ambient Sounds"

    // Tasks & Inbox
    fun inboxQuickCapture(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "صندوق الوارد (سجل أفكارك)" else "Inbox (Quick Capture)"
    fun organizeWithAi(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تنظيم بالذكاء الاصطناعي" else "Organize with AI"
    fun breakdownWithAi(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تقسيم بالذكاء الاصطناعي" else "AI Task Breakdown"
    fun addTask(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إضافة مهمة" else "Add Task"
    fun taskTitleHint(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "مثال: إنهاء مقترح المشروع قبل الخميس" else "e.g. Finish project proposal by Friday"

    // AI Assistant
    fun aiAssistantTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "مساعد وقتي الذكي" else "WAQTI AI Assistant"
    fun aiInputPlaceholder(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "اكتب سؤالك أو اطلب تنظيم يومك..." else "Ask or request a schedule plan..."
    fun behindMessage(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "ولا يهمك. خلينا نعيد ترتيب باقي اليوم بهدوء وبدون ضغط." else "Don't worry at all. Let's recalibrate the rest of your day calmly."

    // Analytics & Pro
    fun weeklyReview(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "أسبوعك مع وقتي" else "Your Week with WAQTI"
    fun subscription(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الاشتراك والترقية" else "Subscription & Plans"
    fun adminDashboard(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "لوحة تحكم المسؤول (Admin)" else "Admin Dashboard"
    fun settings(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الإعدادات" else "Settings"
    fun language(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "اللغة" else "Language"
    fun theme(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المظهر" else "Appearance"
    fun quietHours(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "ساعات الهدوء" else "Quiet Hours"
}
