package com.example.calmconnect.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.calmconnect.db.entity.MoodEntry

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MoodEntry)

    @Delete
    suspend fun delete(entry: MoodEntry)

    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM mood_entries ORDER BY timestamp ASC")
    fun getAllOrderedByTimestamp(): LiveData<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE date = :date")
    suspend fun getByDate(date: String): List<MoodEntry>
}
