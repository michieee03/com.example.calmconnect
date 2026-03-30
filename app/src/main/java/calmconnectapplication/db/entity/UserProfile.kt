package com.example.calmconnect.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,  // single-row table
    val name: String,
    val profilePictureUri: String?,
    val isDarkMode: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val reminderEnabled: Boolean = true
)
