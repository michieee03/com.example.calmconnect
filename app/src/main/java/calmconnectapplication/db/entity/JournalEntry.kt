package calmconnectapplication.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val text: String,
    val timestamp: Long,
    val date: String  // "yyyy-MM-dd"
)
