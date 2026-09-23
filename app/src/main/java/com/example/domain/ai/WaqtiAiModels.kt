package com.example.domain.ai

import java.util.UUID

enum class AiIntent {
    GENERAL_QUESTION,
    RESCHEDULE_DAY,
    IM_BEHIND,
    START_FOCUS,
    BREAKDOWN_TASK,
    CREATE_TASK,
    SHOW_TODAY,
    SHOW_NEXT_TASK,
    SHOW_PRAYER,
    UNKNOWN
}

data class AiActionPayload(
    val actionType: String, // "CREATE_TASK", "APPLY_RESCHEDULE", "IM_BEHIND", "START_FOCUS", "BREAKDOWN_TASK"
    val title: String? = null,
    val durationMinutes: Int? = null,
    val priority: String? = null, // HIGH, MEDIUM, LOW
    val category: String? = null, // WORK, STUDY, PERSONAL, HEALTH, WORSHIP
    val startTime: String? = null,
    val breakdownSteps: List<String>? = null,
    val requiresConfirmation: Boolean = true,
    var isExecuted: Boolean = false
)

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "USER" or "WAQTI"
    val textAr: String,
    val textEn: String,
    val time: String = "",
    val suggestionAction: String? = null, // For backward compatibility e.g. "APPLY_RESCHEDULE"
    val actionPayload: AiActionPayload? = null,
    val isError: Boolean = false,
    val canRetry: Boolean = false
)

data class StructuredAiResult(
    val message: String,
    val intent: AiIntent,
    val action: AiActionPayload? = null,
    val confidence: Float = 1.0f
)
