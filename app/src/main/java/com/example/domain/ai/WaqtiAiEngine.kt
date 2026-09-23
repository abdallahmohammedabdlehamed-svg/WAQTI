package com.example.domain.ai

import com.example.data.local.RoutineEntity
import com.example.data.local.TaskEntity
import com.example.data.prayer.PrayerCalculator
import com.example.localization.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class RescheduleResult(
    val explanationAr: String,
    val explanationEn: String,
    val changes: List<String>,
    val updatedTasks: List<TaskEntity>
)

object WaqtiAiEngine {

    // 1. SMART RESCHEDULING ENGINE ("أعد تنظيم يومي")
    fun computeSmartReschedule(
        currentTasks: List<TaskEntity>,
        routines: List<RoutineEntity>,
        lang: AppLanguage
    ): RescheduleResult {
        val prayerTimes = PrayerCalculator.getPrayerTimes()
        val protectedTimeSlots = mutableListOf<Pair<Int, Int>>() // Pair of startMin to endMin

        // Add protected routines (prayers & fixed meetings)
        for (p in prayerTimes.filter { it.isPrescribedPrayer }) {
            val min = p.rawMinutesOfDay
            protectedTimeSlots.add(Pair(min, min + 25))
        }
        for (r in routines.filter { it.isProtected }) {
            val parts = r.time.split(":")
            if (parts.size == 2) {
                val min = parts[0].toInt() * 60 + parts[1].toInt()
                protectedTimeSlots.add(Pair(min, min + r.durationMinutes))
            }
        }
        for (t in currentTasks.filter { it.isProtected }) {
            val parts = t.startTime.split(":")
            if (parts.size == 2) {
                val min = parts[0].toInt() * 60 + parts[1].toInt()
                protectedTimeSlots.add(Pair(min, min + t.durationMinutes))
            }
        }

        // Sort flexible uncompleted tasks by priority: HIGH first, then MEDIUM, then LOW
        val uncompletedFlexible = currentTasks.filter { !it.isProtected && it.status != "COMPLETED" }
            .sortedWith(compareByDescending<TaskEntity> { it.priority == "HIGH" }.thenByDescending { it.priority == "MEDIUM" })

        var cursorMinute = 13 * 60 // Resume from 1:00 PM (13:00) onwards
        val updatedTasks = currentTasks.toMutableList()
        val changes = mutableListOf<String>()

        for (task in uncompletedFlexible) {
            val duration = task.durationMinutes

            // Find next available slot that does not overlap with protected times
            var slotFound = false
            while (!slotFound && cursorMinute < 22 * 60) {
                val taskEnd = cursorMinute + duration
                val overlap = protectedTimeSlots.any { (pStart, pEnd) ->
                    (cursorMinute in pStart until pEnd) || (taskEnd in (pStart + 1)..pEnd) || (cursorMinute <= pStart && taskEnd >= pEnd)
                }
                if (overlap) {
                    cursorMinute += 15 // bump by 15 mins
                } else {
                    slotFound = true
                }
            }

            if (slotFound) {
                val newStart = String.format(Locale.US, "%02d:%02d", cursorMinute / 60, cursorMinute % 60)
                val newEndMin = cursorMinute + duration
                val newEnd = String.format(Locale.US, "%02d:%02d", newEndMin / 60, newEndMin % 60)

                val idx = updatedTasks.indexOfFirst { it.id == task.id }
                if (idx != -1) {
                    val original = updatedTasks[idx]
                    if (original.startTime != newStart) {
                        changes.add(
                            if (lang == AppLanguage.ARABIC)
                                "نقل \"${original.title}\" من ${original.startTime} إلى $newStart (حماية لمواعيد الصلاة والاجتماع)"
                            else
                                "Moved \"${original.title}\" from ${original.startTime} to $newStart (protected prayer & meetings)"
                        )
                        updatedTasks[idx] = original.copy(startTime = newStart, endTime = newEnd)
                    }
                }
                cursorMinute += duration + 10 // 10 min buffer
            }
        }

        if (changes.isEmpty()) {
            changes.add(
                if (lang == AppLanguage.ARABIC)
                    "جدولك متوازن حاليًا وتمت حماية أوقات الصلوات والأنشطة الثابتة."
                else
                    "Your schedule is balanced; prayer times and commitments are securely protected."
            )
        }

        val explanationAr = "تمت إعادة ترتيب يومك بذكاء لحماية أوقات الصلوات والاجتماعات، مع إعطاء الأولوية القصوى للمهام الهامة وتوفير فترات راحة مرنة."
        val explanationEn = "Your day was intelligently rescheduled to protect prayer times and meetings, prioritizing high-impact tasks with healthy buffers."

        return RescheduleResult(explanationAr, explanationEn, changes, updatedTasks)
    }

    // 2. "I'M BEHIND" (اتأخرت) HANDLER
    fun handleImBehind(currentTasks: List<TaskEntity>, lang: AppLanguage): RescheduleResult {
        val pending = currentTasks.filter { it.status != "COMPLETED" }
        val updated = currentTasks.map { task ->
            if (task.priority == "LOW" && task.status != "COMPLETED") {
                // Postpone low priority task to tomorrow or evening calmly
                task.copy(startTime = "20:30", endTime = "21:00")
            } else {
                task
            }
        }

        val changes = listOf(
            if (lang == AppLanguage.ARABIC) "تخفيف الجدول: تأجيل المهام غير العاجلة إلى المساء أو الغد" else "Lightened schedule: postponed non-urgent tasks to evening",
            if (lang == AppLanguage.ARABIC) "حماية ساعات الراحة وأوقات الصلاة المتبقية" else "Protected remaining rest and prayer hours",
            if (lang == AppLanguage.ARABIC) "التركيز فقط على أهم مهمة تالية بدون ضغط" else "Focused solely on next high-priority task without guilt"
        )

        val expAr = "ولا يهمك أبدًا. كلنا نمر بأيام غير متوقعة. قمنا بتعديل باقي اليوم بهدوء ليناسب طاقتك المتاحة."
        val expEn = "No worries at all. Life happens! We adjusted the rest of your day calmly to match your current energy."

        return RescheduleResult(expAr, expEn, changes, updated)
    }

    // 3. AI TASK BREAKDOWN ("تقسيم المهمة")
    fun breakdownTask(taskTitle: String): List<String> {
        val lower = taskTitle.lowercase()
        return when {
            lower.contains("portfolio") || lower.contains("بورتفوليو") -> listOf(
                "تحديد المشاريع المميزة لعرضها (Selected Works)",
                "كتابة الوصف والدروس المستفادة لكل مشروع",
                "تصميم واجهة البطل (Hero Section) بهوية جذابة",
                "إضافة روابط التواصل ووسائل التواصل المهني",
                "مراجعة التجاوب على الجوال وسرعة التصفح",
                "نشر الموقع واختبار الروابط"
            )
            lower.contains("e-commerce") || lower.contains("متجر") || lower.contains("shop") -> listOf(
                "تحديد متطلبات المشروع ونطاق العمل",
                "تصميم المخطط الشبكي للواجهات (Wireframes)",
                "بناء الصفحة الرئيسية وقائمة المنتجات",
                "تصميم صفحة تفاصيل المنتج وسلة المشتريات",
                "ربط بوابة الدفع الإلكتروني والتحقق الأمني",
                "إجراء اختبارات الشراء والتسليم"
            )
            lower.contains("javascript") || lower.contains("برمجة") || lower.contains("code") || lower.contains("python") || lower.contains("kotlin") -> listOf(
                "تحديد المتطلبات وهيكلية الكود الرئيسية",
                "إعداد بيئة العمل والتبعيات اللازمة",
                "بناء الوظيفة الأساسية واختبار المنطق الداخلي",
                "معالجة حالات الخطأ والمدخلات غير المتوقعة",
                "تحسين الأداء وكتابة الاختبارات التلقائية"
            )
            lower.contains("study") || lower.contains("دراسة") || lower.contains("مذاكرة") || lower.contains("امتحان") -> listOf(
                "مراجعة الفهرس وتحديد المفاهيم الأساسية",
                "تلخيص النقاط الصعبة والرسومات التوضيحية",
                "حل تدريبات وأسئلة امتحانات سابقة",
                "المراجعة السريعة وتثبيت المعلومات بالاسترجاع الفعال"
            )
            lower.contains("بحث") || lower.contains("كتابة") || lower.contains("مقالة") || lower.contains("تقرير") -> listOf(
                "تحديد موضوع البحث والأسئلة الرئيسية",
                "جمع المراجع والمصادر الموثوقة",
                "كتابة المسودة الأولى والهيكل العام",
                "التدقيق اللغوي وإضافة المراجع والتنسيق النهائي"
            )
            else -> listOf(
                "توضيح الهدف النهائي للمهمة بدقة والمعايير المطلوبة",
                "تجهيز المتطلبات والأدوات اللازمة لبدء العمل",
                "تنفيذ الجزء الأساسي الأول في جلسة تركيز (30 دقيقة)",
                "المراجعة والتنقيح واستكمال اللمسات الأخيرة واعتماد النتيجة"
            )
        }
    }

    // 4. INBOX "ORGANIZE WITH AI"
    fun classifyInboxThought(thought: String): TaskEntity {
        val lower = thought.lowercase()
        val priority = if (lower.contains("عاجل") || lower.contains("urgent") || lower.contains("مهم") || lower.contains("deadline")) "HIGH" else "MEDIUM"
        val category = when {
            lower.contains("workout") || lower.contains("تمرين") || lower.contains("صحة") || lower.contains("رياضة") -> "HEALTH"
            lower.contains("study") || lower.contains("مذاكرة") || lower.contains("كتاب") || lower.contains("كورس") -> "STUDY"
            lower.contains("صلاة") || lower.contains("قرآن") || lower.contains("ذكر") -> "WORSHIP"
            else -> "WORK"
        }
        return TaskEntity(
            title = thought,
            description = "تم التنظيم والتصنيف تلقائيًا عبر مساعد وقتي الذكي",
            status = "PLANNED",
            priority = priority,
            category = category,
            durationMinutes = 30,
            startTime = "15:00",
            endTime = "15:30"
        )
    }

    // 5. WAQTI AI 2.0 CONVERSATIONAL ENGINE
    suspend fun queryAiAssistant(
        query: String,
        history: List<AiChatMessage> = emptyList(),
        context: WaqtiAiContext? = null,
        lang: AppLanguage = AppLanguage.ARABIC
    ): AiChatMessage = withContext(Dispatchers.IO) {
        val structuredResult = WaqtiAiService.queryGemini(query, history, context, lang)
        val suggestion = structuredResult.action?.actionType ?: when (structuredResult.intent) {
            AiIntent.RESCHEDULE_DAY -> "APPLY_RESCHEDULE"
            AiIntent.IM_BEHIND -> "IM_BEHIND"
            AiIntent.BREAKDOWN_TASK -> "BREAKDOWN_TASK"
            AiIntent.START_FOCUS -> "START_FOCUS"
            else -> null
        }

        AiChatMessage(
            sender = "WAQTI",
            textAr = structuredResult.message,
            textEn = structuredResult.message,
            suggestionAction = suggestion,
            actionPayload = structuredResult.action
        )
    }

    // Backward-compatible query wrapper
    suspend fun queryChatGpt(
        query: String,
        lang: AppLanguage,
        customApiKey: String? = null
    ): AiChatMessage = withContext(Dispatchers.IO) {
        queryAiAssistant(query, emptyList(), null, lang)
    }

    // Fallback Conversational Assistant Responder
    fun getAssistantResponse(query: String, lang: AppLanguage): AiChatMessage {
        val q = query.lowercase().trim()
        val isAr = lang == AppLanguage.ARABIC

        return when {
            q.contains("رتب") || q.contains("نظم") || q.contains("plan") || q.contains("schedule") -> {
                AiChatMessage(
                    sender = "WAQTI",
                    textAr = "قمت بمراجعة مهامك وصلواتك المتبقية. أستطيع إعادة جدولة المهام بما يحمي أوقات الصلاة والراحة. هل تحب تطبيق إعادة الجدولة؟",
                    textEn = "I reviewed your schedule. I can reschedule your tasks to safeguard prayer and rest times. Would you like to apply the reschedule?",
                    suggestionAction = "APPLY_RESCHEDULE",
                    actionPayload = AiActionPayload(actionType = "APPLY_RESCHEDULE", requiresConfirmation = true)
                )
            }
            q.contains("اتأخرت") || q.contains("behind") || q.contains("late") || q.contains("متأخر") -> {
                AiChatMessage(
                    sender = "WAQTI",
                    textAr = "ولا يهمك، الحياة مليئة بالمفاجآت. لقد جهزت خطة مخففة تركز على الضروري فقط وتؤجل الباقي بدون أي ضغط.",
                    textEn = "No worries at all! Life happens. I've prepared a relaxed plan focusing only on essentials and safely postponing the rest.",
                    suggestionAction = "IM_BEHIND",
                    actionPayload = AiActionPayload(actionType = "IM_BEHIND", requiresConfirmation = true)
                )
            }
            q.contains("ماذا أفعل") || q.contains("what should i do") || q.contains("الآن") -> {
                AiChatMessage(
                    sender = "WAQTI",
                    textAr = "أنصحك بمراجعة المهمة التالية والبدء بجلسة تركيز عميق لمدة 25-45 دقيقة مع الاستعانة بالله.",
                    textEn = "I recommend checking your next upcoming task and starting a 25-45 min deep focus session.",
                    suggestionAction = "START_FOCUS",
                    actionPayload = AiActionPayload(actionType = "START_FOCUS", requiresConfirmation = false)
                )
            }
            q.contains("قسم") || q.contains("breakdown") || q.contains("مشروع") -> {
                val steps = breakdownTask(query)
                AiChatMessage(
                    sender = "WAQTI",
                    textAr = "إليك تقسيم مقترح للخطوات التنفيذية:\n\n" + steps.mapIndexed { i, s -> "${i+1}. $s" }.joinToString("\n"),
                    textEn = "Here is an actionable breakdown:\n\n" + steps.mapIndexed { i, s -> "${i+1}. $s" }.joinToString("\n"),
                    suggestionAction = "BREAKDOWN_TASK",
                    actionPayload = AiActionPayload(actionType = "BREAKDOWN_TASK", breakdownSteps = steps, requiresConfirmation = true)
                )
            }
            else -> {
                AiChatMessage(
                    sender = "WAQTI",
                    textAr = if (isAr) "مرحبًا بك في مساعد وقتي الذكي! كيف أساعدك اليوم في تنظيم وقتك أو الإجابة على استفساراتك؟" else "Welcome to Waqti AI Assistant! How can I help you organize your time or answer your questions today?",
                    textEn = "Welcome to Waqti AI Assistant! How can I help you organize your time or answer your questions today?"
                )
            }
        }
    }
}

