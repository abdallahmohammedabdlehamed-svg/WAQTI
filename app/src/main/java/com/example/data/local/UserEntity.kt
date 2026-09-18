package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val plan: String = "FREE", // "FREE" or "PRO"
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
