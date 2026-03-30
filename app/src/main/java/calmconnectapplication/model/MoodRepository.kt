package com.example.calmconnect.model

import androidx.lifecycle.LiveData
import com.example.calmconnect.db.dao.MoodDao
import com.example.calmconnect.db.entity.MoodEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MoodRepository(private val moodDao: MoodDao) {

    suspend fun insert(entry: MoodEntry) = withContext(Dispatchers.IO) {
        moodDao.insert(entry)
    }

    suspend fun deleteById(id: Int) = withContext(Dispatchers.IO) {
        moodDao.deleteById(id)
    }

    fun getMoodHistory(): LiveData<List<MoodEntry>> = moodDao.getAllOrderedByTimestamp()
}
