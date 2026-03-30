package com.example.calmconnect.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.calmconnect.db.dao.JournalDao
import com.example.calmconnect.db.dao.MoodDao
import com.example.calmconnect.db.dao.ProfileDao
import com.example.calmconnect.db.dao.QuoteDao
import com.example.calmconnect.db.dao.RoutineDao
import com.example.calmconnect.db.entity.JournalEntry
import com.example.calmconnect.db.entity.MoodEntry
import com.example.calmconnect.db.entity.Quote
import com.example.calmconnect.db.entity.RoutineStep
import com.example.calmconnect.db.entity.UserProfile

@Database(
    entities = [
        MoodEntry::class,
        Quote::class,
        JournalEntry::class,
        RoutineStep::class,
        UserProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moodDao(): MoodDao
    abstract fun quoteDao(): QuoteDao
    abstract fun journalDao(): JournalDao
    abstract fun routineDao(): RoutineDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calm_connect.db"
                ).build().also { INSTANCE = it }
            }
    }
}
