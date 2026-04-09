package calmconnectapplication.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import calmconnectapplication.db.entity.JournalEntry

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntry)

    @Delete
    suspend fun delete(entry: JournalEntry)

    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllByUser(userId: String): LiveData<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllByUserSync(userId: String): List<JournalEntry>
}
