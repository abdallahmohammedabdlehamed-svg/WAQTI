package com.example.domain.ai

import com.example.data.local.RoutineEntity
import com.example.data.local.TaskEntity
import com.example.data.prayer.PrayerTime
import com.example.localization.AppLanguage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WaqtiAiContext(
    val currentDate: String,
    val currentTime: String,
    val language: AppLanguage,
    val totalTasksCount: Int,
    val completedTasksCount: Int,
    val pendingTasksCount: Int,
    val currentOrNextTaskTitle: String?,
    val currentOrNextTaskTime: String?,
    val currentOrNextTaskDurationMin: Int?,
    val nextPrayerName: String?,
    val nextPrayerTime: String?,
    val minutesUntilNextPrayer: Int?,
    val isFocusSessionRunning: Boolean,
    val activeFocusTaskName: String?,
    val taskSnippets: List<String>,
    val routineSnippets: List<String>
) {
    fun toPromptSummary(): String {
        val sb = StringBuilder()
        sb.appendLine("--- WAQTI USER LIVE CONTEXT ---")
        sb.appendLine("Current Date: $currentDate")
        sb.appendLine("Current Time (24h format): $currentTime")
        sb.appendLine("App Language: ${if (language == AppLanguage.ARABIC) "Arabic (العربية)" else "English"}")
        sb.appendLine("Progress: $completedTasksCount completed out of $totalTasksCount tasks ($pendingTasksCount remaining)")

        if (!currentOrNextTaskTitle.isNullOrBlank()) {
            sb.appendLine("Current / Upcoming Task: \"$currentOrNextTaskTitle\" at $currentOrNextTaskTime ($currentOrNextTaskDurationMin mins)")
        } else {
            sb.appendLine("Current / Upcoming Task: None scheduled right now")
        }

        if (!nextPrayerName.isNullOrBlank() && !nextPrayerTime.isNullOrBlank()) {
            sb.appendLine("Next Prayer (Astronomical source of truth): $nextPrayerName at $nextPrayerTime (in $minutesUntilNextPrayer minutes)")
        }

        if (isFocusSessionRunning) {
            sb.appendLine("Focus Session: Active on \"${activeFocusTaskName ?: "Deep Work"}\"")
        }

        if (taskSnippets.isNotEmpty()) {
            sb.appendLine("Today's Tasks (up to 7):")
            taskSnippets.take(7).forEach { sb.appendLine("  • $it") }
        }

        if (routineSnippets.isNotEmpty()) {
            sb.appendLine("Key Daily Commitments:")
            routineSnippets.take(5).forEach { sb.appendLine("  • $it") }
        }

        sb.appendLine("-------------------------------")
        return sb.toString()
    }

    companion object {
        fun build(
            tasks: List<TaskEntity>,
            routines: List<RoutineEntity>,
            nextPrayer: PrayerTime?,
            isFocusRunning: Boolean,
            activeFocusTask: String?,
            language: AppLanguage,
            now: Date = Date()
        ): WaqtiAiContext {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val currentTime = timeFormat.format(now)
            val currentDate = dateFormat.format(now)

            val completed = tasks.count { it.status == "COMPLETED" }
            val pending = tasks.filter { it.status != "COMPLETED" }

            val nextTask = pending.firstOrNull { it.startTime >= currentTime } ?: pending.firstOrNull()

            val taskSnippets = tasks.map { t ->
                val statusMarker = if (t.status == "COMPLETED") "[DONE]" else "[PENDING]"
                "${t.title} ($statusMarker, ${t.startTime}-${t.endTime}, ${t.durationMinutes}m, Priority: ${t.priority})"
            }

            val routineSnippets = routines.map { r ->
                val title = if (language == AppLanguage.ARABIC) r.titleAr else r.title
                "$title (${r.time}, ${r.durationMinutes}m, Protected: ${r.isProtected})"
            }

            val prayerName = if (nextPrayer != null) {
                if (language == AppLanguage.ARABIC) nextPrayer.nameAr else nextPrayer.nameEn
            } else null

            return WaqtiAiContext(
                currentDate = currentDate,
                currentTime = currentTime,
                language = language,
                totalTasksCount = tasks.size,
                completedTasksCount = completed,
                pendingTasksCount = pending.size,
                currentOrNextTaskTitle = nextTask?.title,
                currentOrNextTaskTime = nextTask?.startTime,
                currentOrNextTaskDurationMin = nextTask?.durationMinutes,
                nextPrayerName = prayerName,
                nextPrayerTime = nextPrayer?.timeFormatted,
                minutesUntilNextPrayer = nextPrayer?.minutesUntil,
                isFocusSessionRunning = isFocusRunning,
                activeFocusTaskName = activeFocusTask,
                taskSnippets = taskSnippets,
                routineSnippets = routineSnippets
            )
        }
    }
}
