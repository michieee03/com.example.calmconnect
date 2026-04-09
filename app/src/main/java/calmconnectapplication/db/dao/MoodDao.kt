package calmconnectapplication.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import calmconnectapplication.db.entity.MoodEntry

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MoodEntry)

    @Query("DELETE FROM mood_entries WHERE id = :id AND userId = :userId")
    suspend fun deleteById(id: Int, userId: String)

    @Query("SELECT * FROM mood_entries WHERE userId = :userId ORDER BY timestamp ASC")
    fun getAllByUser(userId: String): LiveData<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE userId = :userId ORDER BY timestamp ASC")
    suspend fun getAllByUserSync(userId: String): List<MoodEntry>

    @Query("SELECT * FROM mood_entries WHERE userId = :userId AND date = :date")
    suspend fun getByDate(userId: String, date: String): List<MoodEntry>
}
