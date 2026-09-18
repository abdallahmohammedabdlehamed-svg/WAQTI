package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "user_default_01",
    val title: String,
    val description: String = "",
    val status: String = "PLANNED", // INBOX, PLANNED, IN_PROGRESS, COMPLETED, DEFERRED, CANCELLED
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val category: String = "WORK", // WORK, STUDY, PERSONAL, HEALTH, WORSHIP
    val projectName: String = "",
    val goalName: String = "",
    val durationMinutes: Int = 45,
    val startTime: String = "09:00",
    val endTime: String = "09:45",
    val date: String = "2026-09-17",
    val isProtected: Boolean = false,
    val subtasksRaw: String = "", // newline or pipe separated subtasks
    val tags: String = ""
)
