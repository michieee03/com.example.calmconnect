package calmconnectapplication.model

import androidx.lifecycle.LiveData
import calmconnectapplication.db.dao.RoutineDao
import calmconnectapplication.db.entity.RoutineStep
import calmconnectapplication.util.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoutineRepository(private val routineDao: RoutineDao) {

    suspend fun insertAll(steps: List<RoutineStep>) = withContext(Dispatchers.IO) {
        routineDao.insertAll(steps)
    }

    suspend fun update(step: RoutineStep) = withContext(Dispatchers.IO) {
        routineDao.update(step)
    }

    fun getByDate(date: String): LiveData<List<RoutineStep>> =
        routineDao.getByDate(UserSession.uid, date)

    suspend fun getByDateSync(date: String): List<RoutineStep> = withContext(Dispatchers.IO) {
        routineDao.getByDateSync(UserSession.uid, date)
    }

    suspend fun deleteOlderThan(date: String) = withContext(Dispatchers.IO) {
        routineDao.deleteOlderThan(UserSession.uid, date)
    }
}
