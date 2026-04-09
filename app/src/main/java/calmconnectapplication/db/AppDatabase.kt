package calmconnectapplication.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import calmconnectapplication.db.dao.JournalDao
import calmconnectapplication.db.dao.MoodDao
import calmconnectapplication.db.dao.ProfileDao
import calmconnectapplication.db.dao.QuoteDao
import calmconnectapplication.db.dao.RoutineDao
import calmconnectapplication.db.dao.StressLogDao
import calmconnectapplication.db.entity.JournalEntry
import calmconnectapplication.db.entity.MoodEntry
import calmconnectapplication.db.entity.Quote
import calmconnectapplication.db.entity.RoutineStep
import calmconnectapplication.db.entity.StressLogEntry
import calmconnectapplication.db.entity.UserProfile

@Database(
    entities = [
        MoodEntry::class,
        Quote::class,
        JournalEntry::class,
        RoutineStep::class,
        UserProfile::class,
        StressLogEntry::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moodDao(): MoodDao
    abstract fun quoteDao(): QuoteDao
    abstract fun journalDao(): JournalDao
    abstract fun routineDao(): RoutineDao
    abstract fun profileDao(): ProfileDao
    abstract fun stressLogDao(): StressLogDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Migration: add userId columns to all user-data tables
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // mood_entries: add userId, default empty string for existing rows
                db.execSQL("ALTER TABLE mood_entries ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                // journal_entries: add userId
                db.execSQL("ALTER TABLE journal_entries ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                // routine_steps: add userId
                db.execSQL("ALTER TABLE routine_steps ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                // user_profile: recreate with userId as PK (can't ALTER PRIMARY KEY in SQLite)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_profile_new (
                        userId TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        profilePictureUri TEXT,
                        isDarkMode INTEGER NOT NULL DEFAULT 0,
                        reminderHour INTEGER NOT NULL DEFAULT 8,
                        reminderMinute INTEGER NOT NULL DEFAULT 0,
                        reminderEnabled INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())
                // Copy existing profile row with empty userId (will be overwritten on next login)
                db.execSQL("""
                    INSERT OR IGNORE INTO user_profile_new (userId, name, profilePictureUri, isDarkMode, reminderHour, reminderMinute, reminderEnabled)
                    SELECT '', name, profilePictureUri, isDarkMode, reminderHour, reminderMinute, reminderEnabled
                    FROM user_profile
                """.trimIndent())
                db.execSQL("DROP TABLE user_profile")
                db.execSQL("ALTER TABLE user_profile_new RENAME TO user_profile")
            }
        }

        // Migration 2→3: change routine_steps PK from (id) to (id, userId, date)
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS routine_steps_new (
                        id INTEGER NOT NULL,
                        userId TEXT NOT NULL DEFAULT '',
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        date TEXT NOT NULL,
                        PRIMARY KEY(id, userId, date)
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT OR IGNORE INTO routine_steps_new
                    SELECT id, userId, title, description, durationMinutes, isCompleted, date
                    FROM routine_steps
                """.trimIndent())
                db.execSQL("DROP TABLE routine_steps")
                db.execSQL("ALTER TABLE routine_steps_new RENAME TO routine_steps")
            }
        }

        // Migration 3→4: add stress_logs table
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS stress_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL DEFAULT '',
                        level INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calm_connect.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build().also { INSTANCE = it }
            }
    }
}
