package com.example.calmconnect.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_steps")
data class RoutineStep(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val isCompleted: Boolean = false,
    val date: String  // "yyyy-MM-dd", resets daily
)
