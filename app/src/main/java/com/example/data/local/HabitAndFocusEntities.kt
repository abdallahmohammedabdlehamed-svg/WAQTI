package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val titleAr: String,
    val target: String,
    val frequency: String = "DAILY",
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val completedToday: Boolean = false
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskTitle: String,
    val durationMinutes: Int,
    val mode: String, // POMODORO, DEEP_WORK, CUSTOM
    val timestamp: Long = System.currentTimeMillis()
)
