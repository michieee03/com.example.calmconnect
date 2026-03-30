package com.example.calmconnect.model

import androidx.lifecycle.LiveData
import com.example.calmconnect.db.dao.JournalDao
import com.example.calmconnect.db.entity.JournalEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JournalRepository(private val journalDao: JournalDao) {

    suspend fun insert(entry: JournalEntry) = withContext(Dispatchers.IO) {
        journalDao.insert(entry)
    }

    fun getAll(): LiveData<List<JournalEntry>> = journalDao.getAll()
}
