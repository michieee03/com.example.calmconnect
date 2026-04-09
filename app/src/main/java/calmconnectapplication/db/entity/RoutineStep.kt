package calmconnectapplication.db.entity

import androidx.room.Entity

@Entity(
    tableName = "routine_steps",
    primaryKeys = ["id", "userId", "date"]   // composite PK — unique per step per user per day
)
data class RoutineStep(
    val id: Int,
    val userId: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val isCompleted: Boolean = false,
    val date: String  // "yyyy-MM-dd", resets daily
)
