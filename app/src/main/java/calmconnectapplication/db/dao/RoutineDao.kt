package calmconnectapplication.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import calmconnectapplication.db.entity.RoutineStep

@Dao
interface RoutineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(steps: List<RoutineStep>)

    @Update
    suspend fun update(step: RoutineStep)

    @Query("SELECT * FROM routine_steps WHERE userId = :userId AND date = :date ORDER BY id ASC")
    fun getByDate(userId: String, date: String): LiveData<List<RoutineStep>>

    @Query("SELECT * FROM routine_steps WHERE userId = :userId AND date = :date ORDER BY id ASC")
    suspend fun getByDateSync(userId: String, date: String): List<RoutineStep>

    // Clean up rows older than the given date to keep the DB tidy
    @Query("DELETE FROM routine_steps WHERE userId = :userId AND date < :date")
    suspend fun deleteOlderThan(userId: String, date: String)
}
