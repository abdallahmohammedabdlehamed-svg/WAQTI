package com.example.domain.ai

import android.util.Log
import com.example.BuildConfig
import com.example.localization.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WaqtiAiService {

    private const val TAG = "WaqtiAiService"
    private const val GEMINI_MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Executes a real multi-turn conversational query using Gemini 3.5 Flash.
     */
    suspend fun queryGemini(
        query: String,
        history: List<AiChatMessage>,
        context: WaqtiAiContext?,
        language: AppLanguage
    ): StructuredAiResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        val isKeyConfigured = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (!isKeyConfigured) {
            Log.w(TAG, "GEMINI_API_KEY is not configured or using default placeholder.")
            return@withContext fallbackLocalIntelligence(query, context, language, isNetworkError = false)
        }

        try {
            val systemPrompt = WaqtiAiSystemPrompt.getSystemPrompt(language, context)
            val requestBodyJson = buildRequestBody(query, history, systemPrompt)

            val endpoint = "$BASE_URL/$GEMINI_MODEL:generateContent?key=$apiKey"
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseCode = response.code
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful && responseBody.isNotBlank()) {
                val jsonObject = JSONObject(responseBody)
                val candidates = jsonObject.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val rawText = parts.getJSONObject(0).optString("text", "")
                        return@withContext parseModelResponse(rawText, query, language)
                    }
                }
            } else {
                Log.e(TAG, "Gemini API failed with code $responseCode: $responseBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API call exception: ${e.message}", e)
        }

        // Graceful fallback with intelligent assistance
        return@withContext fallbackLocalIntelligence(query, context, language, isNetworkError = true)
    }

    /**
     * Builds the Gemini generateContent request JSON payload with multi-turn history.
     */
    private fun buildRequestBody(
        newQuery: String,
        history: List<AiChatMessage>,
        systemPrompt: String
    ): JSONObject {
        val root = JSONObject()

        // 1. System Instruction
        val systemInstruction = JSONObject().apply {
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", systemPrompt) })
            })
        }
        root.put("systemInstruction", systemInstruction)

        // 2. Multi-turn Contents (taking last 8 messages for balanced context window)
        val contentsArray = JSONArray()
        val recentHistory = history.filter { !it.isError }.takeLast(8)

        for (msg in recentHistory) {
            val role = if (msg.sender == "USER") "user" else "model"
            val text = if (msg.textAr.isNotBlank()) msg.textAr else msg.textEn
            if (text.isNotBlank()) {
                contentsArray.put(JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", text) })
                    })
                })
            }
        }

        // Add the current user query
        contentsArray.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", newQuery) })
            })
        })
        root.put("contents", contentsArray)

        // 3. Generation Config
        val genConfig = JSONObject().apply {
            put("temperature", 0.7)
            put("topP", 0.95)
            put("topK", 40)
        }
        root.put("generationConfig", genConfig)

        return root
    }

    /**
     * Parses the raw model text output, extracting any structured ```action ... ``` blocks.
     */
    fun parseModelResponse(
        rawText: String,
        userQuery: String = "",
        language: AppLanguage = AppLanguage.ARABIC
    ): StructuredAiResult {
        var cleanMessage = rawText
        var extractedAction: AiActionPayload? = null
        var intent = AiIntent.GENERAL_QUESTION

        val actionRegex = Regex("```(?:action|json)?\\s*(\\{[\\s\\S]*?\\})\\s*```")
        val match = actionRegex.find(rawText)

        if (match != null) {
            val actionJsonString = match.groupValues[1].trim()
            cleanMessage = rawText.replace(match.value, "").trim()

            try {
                val actionJson = JSONObject(actionJsonString)
                val actionType = actionJson.optString("action", "")

                when (actionType) {
                    "CREATE_TASK" -> {
                        intent = AiIntent.CREATE_TASK
                        val title = actionJson.optString("title", userQuery)
                        val duration = actionJson.optInt("durationMinutes", 45)
                        val priority = actionJson.optString("priority", "MEDIUM")
                        extractedAction = AiActionPayload(
                            actionType = "CREATE_TASK",
                            title = title,
                            durationMinutes = duration,
                            priority = priority,
                            requiresConfirmation = true
                        )
                    }
                    "APPLY_RESCHEDULE" -> {
                        intent = AiIntent.RESCHEDULE_DAY
                        extractedAction = AiActionPayload(
                            actionType = "APPLY_RESCHEDULE",
                            requiresConfirmation = true
                        )
                    }
                    "IM_BEHIND" -> {
                        intent = AiIntent.IM_BEHIND
                        extractedAction = AiActionPayload(
                            actionType = "IM_BEHIND",
                            requiresConfirmation = true
                        )
                    }
                    "START_FOCUS" -> {
                        intent = AiIntent.START_FOCUS
                        val title = actionJson.optString("title", "")
                        extractedAction = AiActionPayload(
                            actionType = "START_FOCUS",
                            title = title,
                            requiresConfirmation = false
                        )
                    }
                    "BREAKDOWN_TASK" -> {
                        intent = AiIntent.BREAKDOWN_TASK
                        val title = actionJson.optString("title", userQuery)
                        val stepsArray = actionJson.optJSONArray("steps")
                        val steps = mutableListOf<String>()
                        if (stepsArray != null) {
                            for (i in 0 until stepsArray.length()) {
                                steps.add(stepsArray.getString(i))
                            }
                        }
                        extractedAction = AiActionPayload(
                            actionType = "BREAKDOWN_TASK",
                            title = title,
                            breakdownSteps = steps,
                            requiresConfirmation = true
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse action json: ${e.message}")
            }
        }

        // Secondary semantic intent detection if action was not formatted as block but intent is obvious
        if (extractedAction == null) {
            val q = userQuery.lowercase()
            if (q.contains("رتب") || q.contains("نظم يومي") || q.contains("reschedule my day")) {
                intent = AiIntent.RESCHEDULE_DAY
                extractedAction = AiActionPayload(actionType = "APPLY_RESCHEDULE", requiresConfirmation = true)
            } else if (q.contains("اتأخرت") || q.contains("متأخر") || q.contains("behind")) {
                intent = AiIntent.IM_BEHIND
                extractedAction = AiActionPayload(actionType = "IM_BEHIND", requiresConfirmation = true)
            }
        }

        return StructuredAiResult(
            message = cleanMessage,
            intent = intent,
            action = extractedAction
        )
    }

    /**
     * Fallback for when API key is unconfigured or network is unavailable.
     * Provides honest, helpful responses using real WAQTI data without pretending to be an offline full LLM.
     */
    fun fallbackLocalIntelligence(
        query: String,
        context: WaqtiAiContext? = null,
        language: AppLanguage = AppLanguage.ARABIC,
        isNetworkError: Boolean = false
    ): StructuredAiResult {
        val q = query.lowercase().trim()
        val isAr = language == AppLanguage.ARABIC

        return when {
            q.contains("ماذا أفعل") || q.contains("what should i do") || q.contains("الآن") -> {
                val currentTask = context?.currentOrNextTaskTitle
                val currentTaskTime = context?.currentOrNextTaskTime
                val duration = context?.currentOrNextTaskDurationMin ?: 45
                val nextPrayer = context?.nextPrayerName
                val nextPrayerTime = context?.nextPrayerTime
                val minutesToPrayer = context?.minutesUntilNextPrayer

                val msg = if (isAr) {
                    if (currentTask != null) {
                        "حسب جدولك الحالي، مهمتك القادمة هي **\"$currentTask\"** المقررة في الساعة **$currentTaskTime** (لمدة $duration دقيقة)." +
                        (if (nextPrayer != null && minutesToPrayer != null && minutesToPrayer > 0) "\n\n🕌 أقرب صلاة قادمة: **$nextPrayer** في **$nextPrayerTime** (بعد $minutesToPrayer دقيقة)." else "") +
                        "\n\nهل تود بدء جلسة تركيز لهذه المهمة الآن؟"
                    } else {
                        "ليس لديك مهام مجدولة في هذه اللحظة. يمكنك إضافة مهمة جديدة أو مراجعة جدول اليوم."
                    }
                } else {
                    if (currentTask != null) {
                        "According to your live schedule, your upcoming task is **\"$currentTask\"** scheduled at **$currentTaskTime** ($duration mins)." +
                        (if (nextPrayer != null) "\n\n🕌 Next prayer: **$nextPrayer** at **$nextPrayerTime**." else "") +
                        "\n\nWould you like to start a focus session for it now?"
                    } else {
                        "You have no tasks scheduled at this moment. You can add a new task or review today's schedule."
                    }
                }

                StructuredAiResult(
                    message = msg,
                    intent = AiIntent.SHOW_TODAY,
                    action = if (currentTask != null) AiActionPayload("START_FOCUS", title = currentTask, requiresConfirmation = false) else null
                )
            }

            q.contains("اتأخرت") || q.contains("متأخر") || q.contains("behind") -> {
                val msg = if (isAr) {
                    "ولا يهمك، كلنا نمر بأيام غير متوقعة! قمت بإعداد خطة مخففة بهدوء تركز على الضروري فقط وتؤجل المهام غير العاجلة للمساء لحماية وقت راحتك وصلواتك."
                } else {
                    "No worries at all! Life happens. I've prepared a relaxed plan that focuses on high priorities and gracefully postpones non-urgent items."
                }
                StructuredAiResult(
                    message = msg,
                    intent = AiIntent.IM_BEHIND,
                    action = AiActionPayload(actionType = "IM_BEHIND", requiresConfirmation = true)
                )
            }

            (q.contains("رتب") || q.contains("نظم") || q.contains("أعد ترتيب") || q.contains("تنظيم يوم") || q.contains("إعادة جدولة") || q.contains("reschedule"))
                && !q.contains("كيف") && !q.contains("طريقة") && !q.contains("ما هي") && !q.contains("how") -> {
                val msg = if (isAr) {
                    "أستطيع إعادة ترتيب يومك بذكاء لحماية مواعيد الصلوات والاجتماعات، مع إعطاء الأولوية للمهام الأكثر أهمية وتوفير فترات راحة مرنة. هل تحب تطبيق إعادة الجدولة على جدول اليوم؟"
                } else {
                    "I can smartly reschedule your remaining day to safeguard prayer times and key commitments. Would you like to apply the reschedule to today's tasks?"
                }
                StructuredAiResult(
                    message = msg,
                    intent = AiIntent.RESCHEDULE_DAY,
                    action = AiActionPayload(actionType = "APPLY_RESCHEDULE", requiresConfirmation = true)
                )
            }

            q.contains("قسم") || q.contains("breakdown") -> {
                val cleanedTitle = query.replace(Regex("^(قسم|قسملي|breakdown)\\s*", RegexOption.IGNORE_CASE), "").trim()
                val targetTitle = if (cleanedTitle.isNotBlank()) cleanedTitle else "المشروع الجديد"
                val dynamicSteps = WaqtiAiEngine.breakdownTask(targetTitle)

                val msg = if (isAr) {
                    "إليك تقسيم مقترح لـ **\"$targetTitle\"** إلى خطوات تنفيذية متسلسلة:\n\n" +
                    dynamicSteps.mapIndexed { idx, s -> "${idx + 1}. $s" }.joinToString("\n") +
                    "\n\nيمكنك اعتماد هذه الخطوات كمهام فرعية بضغطة واحدة."
                } else {
                    "Here is a suggested breakdown for **\"$targetTitle\"** into actionable steps:\n\n" +
                    dynamicSteps.mapIndexed { idx, s -> "${idx + 1}. $s" }.joinToString("\n") +
                    "\n\nWould you like to save these as subtasks?"
                }

                StructuredAiResult(
                    message = msg,
                    intent = AiIntent.BREAKDOWN_TASK,
                    action = AiActionPayload(
                        actionType = "BREAKDOWN_TASK",
                        title = targetTitle,
                        breakdownSteps = dynamicSteps,
                        requiresConfirmation = true
                    )
                )
            }

            else -> {
                val note = if (isNetworkError) {
                    if (isAr)
                        "تعذر الاتصال بخدمة الذكاء الاصطناعي السحابية مؤقتًا (تأكد من الاتصال بالإنترنت). ومع ذلك، يمكنك استخدام مساعد وقتي لتنظيم جدولك، حل التأخيرات، وتقسيم المهام."
                    else
                        "Temporarily unable to connect to the cloud AI service (check internet connection). However, you can still use WAQTI tools to organize your schedule, handle delays, and break down tasks."
                } else {
                    if (isAr)
                        "مرحبًا بك! لتفعيل المحادثة العامة الكاملة (برمجة، دراسة، كتابة)، يرجى ضبط مفتاح Gemini API في لوحة الإعدادات. يمكنك الآن استخدام كافة أدوات وقتي لتنظيم اليوم وإعادة الجدولة ومتابعة الصلوات."
                    else
                        "Welcome! To enable full conversational AI (coding, study, writing), please configure your Gemini API Key in the Secrets panel. You can currently use all WAQTI schedule, prayer, and task optimization tools."
                }

                StructuredAiResult(
                    message = note,
                    intent = AiIntent.GENERAL_QUESTION
                )
            }
        }
    }

    /**
     * Dynamically breaks down ANY task title using Gemini 3.5 Flash.
     */
    suspend fun breakdownTaskDynamic(taskTitle: String, language: AppLanguage): List<String> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext WaqtiAiEngine.breakdownTask(taskTitle)
        }

        try {
            val isAr = language == AppLanguage.ARABIC
            val prompt = if (isAr) {
                "قسم هذه المهمة: \"$taskTitle\" إلى 4 إلى 6 خطوات تنفيذية محددة وعملية وموجزة. اكتب فقط قائمة الخطوات كل خطوة في سطر منفصل بدون ترقيم وبدون مقدمة."
            } else {
                "Break down this task: \"$taskTitle\" into 4 to 6 specific, actionable, and concise steps. Output only the steps, one per line, without numbering or introduction."
            }

            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }

            val endpoint = "$BASE_URL/$GEMINI_MODEL:generateContent?key=$apiKey"
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody.toString().toRequestBody(mediaType))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val text = json.optJSONArray("candidates")?.optJSONObject(0)
                    ?.optJSONObject("content")?.optJSONArray("parts")
                    ?.optJSONObject(0)?.optString("text", "") ?: ""

                val lines = text.lines()
                    .map { it.trim().removePrefix("-").removePrefix("*").trim().replace(Regex("^\\d+[.)]\\s*"), "") }
                    .filter { it.isNotBlank() }

                if (lines.isNotEmpty()) {
                    return@withContext lines
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error in dynamic task breakdown: ${e.message}")
        }

        return@withContext WaqtiAiEngine.breakdownTask(taskTitle)
    }
}
