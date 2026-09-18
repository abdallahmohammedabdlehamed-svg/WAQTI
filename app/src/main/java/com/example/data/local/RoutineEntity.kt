package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val titleAr: String,
    val category: String, // PERSONAL, HEALTH, WORSHIP, WORK_STUDY
    val type: String = "FIXED", // FIXED, FLEXIBLE, OPTIONAL
    val time: String, // e.g. "06:00"
    val durationMinutes: Int = 20,
    val isProtected: Boolean = true,
    val isCompleted: Boolean = false,
    val streak: Int = 0,
    val date: String = "2026-09-17"
)
