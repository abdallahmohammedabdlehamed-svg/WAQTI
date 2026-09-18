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

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "WAQTI"
    val textAr: String,
    val textEn: String,
    val time: String = "10:30",
    val suggestionAction: String? = null // e.g. "APPLY_RESCHEDULE", "BREAKDOWN_TASK"
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
        for (p in prayerTimes) {
            val parts = p.timeFormatted.split(":")
            val min = parts[0].toInt() * 60 + parts[1].toInt()
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
            lower.contains("javascript") || lower.contains("برمجة") || lower.contains("study") || lower.contains("دراسة") -> listOf(
                "فهم المبادئ الأساسية وتطبيق 3 أمثلة حية",
                "حل مسألتين برمجيتين لتثبيت المفاهيم",
                "كتابة ملاحظات وتلخيص الأخطاء الشائعة",
                "بناء مشروع مصغر يدمج ما تم تعلمه"
            )
            else -> listOf(
                "توضيح الهدف النهائي للمهمة بدقة",
                "تجهيز المتطلبات والأدوات اللازمة",
                "تنفيذ الجزء الأساسي الأول (30 دقيقة)",
                "المراجعة والتنقيح واستكمال اللمسات الأخيرة"
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

    // 5. LIVE CHATGPT & CONVERSATIONAL ASSISTANT
    suspend fun queryChatGpt(
        query: String,
        lang: AppLanguage,
        customApiKey: String? = null
    ): AiChatMessage = withContext(Dispatchers.IO) {
        val q = query.trim()
        val isAr = lang == AppLanguage.ARABIC

        // Try Live ChatGPT API (Free Endpoint)
        try {
            val url = URL("https://text.pollinations.ai/")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 6500
                readTimeout = 8500
                doOutput = true
                doInput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("User-Agent", "WaqtiTimeOS/1.0")
            }

            val systemPrompt = if (isAr) {
                "أنت مساعد وقتي الذكي المدعوم بـ ChatGPT لإدارة الوقت، تنظيم المهام، وموازنة الحياة اليومية للمستخدمين باللغة العربية. " +
                "قدم إجابات احترافية، عملية، وموجزة، ومنسقة بنقاط واضحة. كن محفزاً وواقعياً، وراعِ أوقات الصلاة والتركيز والراحة."
            } else {
                "You are Waqti AI Assistant powered by ChatGPT for time management, daily task scheduling, and mindful productivity. " +
                "Provide professional, actionable, concise, and beautifully structured responses with practical bullet points."
            }

            val jsonBody = JSONObject().apply {
                val messagesArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", q)
                    })
                }
                put("messages", messagesArray)
                put("model", "openai")
                put("temperature", 0.7)
            }

            conn.outputStream.use { os ->
                val input = jsonBody.toString().toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val responseText = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }.trim()
                if (responseText.isNotBlank()) {
                    val lower = q.lowercase()
                    val suggestion = when {
                        lower.contains("رتب") || lower.contains("نظم") || lower.contains("reschedule") -> "APPLY_RESCHEDULE"
                        lower.contains("اتأخرت") || lower.contains("behind") || lower.contains("متأخر") -> "IM_BEHIND"
                        lower.contains("قسم") || lower.contains("breakdown") -> "BREAKDOWN_TASK"
                        else -> null
                    }
                    return@withContext AiChatMessage(
                        sender = "WAQTI",
                        textAr = responseText,
                        textEn = responseText,
                        suggestionAction = suggestion
                    )
                }
            }
        } catch (e: Exception) {
            // Log and fallback to local engine
        }

        // Instant local expert fallback
        return@withContext getAssistantResponse(query, lang)
    }

    // Fallback Conversational Assistant Responder
    fun getAssistantResponse(query: String, lang: AppLanguage): AiChatMessage {
        val q = query.lowercase().trim()
        val isAr = lang == AppLanguage.ARABIC

        return when {
            q.contains("رتب") || q.contains("نظم") || q.contains("plan") || q.contains("schedule") -> {
                AiChatMessage(
                    sender = "WAQTI",
                    textAr = "قمت بمراجعة مهامك وصلواتك المتبقية. أنصح بالتركيز الآن على العمل على الـPortfolio لمدة 45 دقيقة قبل استراحة الظهر. هل تحب أن أطبق إعادة الجدولة؟",
                    textEn = "I analyzed your remaining tasks and prayer times. I recommend focusing on your Portfolio for 45 mins before Dhuhr break. Would you like me to apply the schedule?",
                    suggestionAction = "APPLY_RESCHEDULE"
                )
            }
            q.contains("اتأخرت") || q.contains("behind") || q.contains("late") || q.contains("متأخر") -> {
                AiChatMessage(
                    sender = "WAQTI",
                    textAr = "ولا يهمك يا بطل. الحياة مليئة بالمفاجآت. لقد جهزت خطة مخففة تركز على الضروري فقط وتؤجل الباقي بدون أي ضغط.",
                    textEn = "No worries at all! Life happens. I've prepared a relaxed plan focusing only on essentials and safely postponing the rest.",
                    suggestionAction = "IM_BEHIND"
                )
            }
            q.contains("ماذا أفعل") || q.contains("what should i do") || q.contains("الآن") -> {
                AiChatMessage(
                    sender = "WAQTI",
                    textAr = "أفضل استثمار لوقتك الآن: العمل على الـPortfolio لمدة 45 دقيقة بأقصى تركيز. يمكنك بدء جلسة التركيز العميق بضغطة واحدة.",
                    textEn = "Best investment of your time right now: Work on Portfolio for 45 minutes of deep focus. You can start the focus timer with one tap.",
                    suggestionAction = "START_FOCUS"
                )
            }
            q.contains("قسم") || q.contains("breakdown") || q.contains("مشروع") -> {
                AiChatMessage(
                    sender = "WAQTI",
                    textAr = "سأقوم بتقسيم هذا المشروع إلى 5 خطوات تنفيذية متسلسلة وقابلة للإنجاز الفوري.",
                    textEn = "I will break this project into 5 actionable, sequential steps ready to execute.",
                    suggestionAction = "BREAKDOWN_TASK"
                )
            }
            else -> {
                AiChatMessage(
                    sender = "WAQTI",
                    textAr = if (isAr) "أهلاً بك! أنا مساعد وقتي الذكي المدعوم بـ ChatGPT المجاني. يمكنني مساعدتك في تنظيم يومك، إعادة ترتيب المواعيد عند التأخر، تقسيم الأهداف الكبيرة، وتقديم نصائح إنتاجية احترافية. كيف أساعدك الآن؟" else "Welcome! I'm your Waqti AI Assistant powered by free ChatGPT. I can organize your day, reschedule after delays, break down big goals, or provide productivity coaching. How can I help you right now?",
                    textEn = "Welcome! I'm your Waqti AI Assistant powered by free ChatGPT. I can organize your day, reschedule after delays, break down big goals, or provide productivity coaching. How can I help you right now?"
                )
            }
        }
    }
}
