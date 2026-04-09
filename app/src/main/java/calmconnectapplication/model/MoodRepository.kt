package calmconnectapplication.model

import androidx.lifecycle.LiveData
import calmconnectapplication.db.dao.MoodDao
import calmconnectapplication.db.entity.MoodEntry
import calmconnectapplication.util.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MoodRepository(private val moodDao: MoodDao) {

    suspend fun insert(entry: MoodEntry) = withContext(Dispatchers.IO) {
        moodDao.insert(entry)
    }

    suspend fun deleteById(id: Int) = withContext(Dispatchers.IO) {
        moodDao.deleteById(id, UserSession.uid)
    }

    fun getMoodHistory(): LiveData<List<MoodEntry>> =
        moodDao.getAllByUser(UserSession.uid)
}
