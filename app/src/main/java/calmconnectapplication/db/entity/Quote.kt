package com.example.calmconnect.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey val id: Int,
    val text: String,
    val author: String,
    val isFavorite: Boolean = false
)
