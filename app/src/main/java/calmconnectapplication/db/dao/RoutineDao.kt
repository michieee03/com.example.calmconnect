package com.example.calmconnect.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.calmconnect.db.entity.RoutineStep

@Dao
interface RoutineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(steps: List<RoutineStep>)

    @Update
    suspend fun update(step: RoutineStep)

    @Query("SELECT * FROM routine_steps WHERE date = :date")
    fun getByDate(date: String): LiveData<List<RoutineStep>>

    @Query("SELECT * FROM routine_steps WHERE date = :date")
    suspend fun getByDateSync(date: String): List<RoutineStep>
}
