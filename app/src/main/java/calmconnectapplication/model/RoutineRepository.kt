package com.example.calmconnect.model

import androidx.lifecycle.LiveData
import com.example.calmconnect.db.dao.RoutineDao
import com.example.calmconnect.db.entity.RoutineStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoutineRepository(private val routineDao: RoutineDao) {

    suspend fun insertAll(steps: List<RoutineStep>) = withContext(Dispatchers.IO) {
        routineDao.insertAll(steps)
    }

    suspend fun update(step: RoutineStep) = withContext(Dispatchers.IO) {
        routineDao.update(step)
    }

    fun getByDate(date: String): LiveData<List<RoutineStep>> = routineDao.getByDate(date)

    suspend fun getByDateSync(date: String): List<RoutineStep> = withContext(Dispatchers.IO) {
        routineDao.getByDateSync(date)
    }
}
