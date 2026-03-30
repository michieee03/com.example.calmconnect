package com.example.calmconnect.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val emotion: String,
    val note: String?,
    val timestamp: Long,
    val date: String  // "yyyy-MM-dd"
)
