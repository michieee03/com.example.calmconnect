package calmconnectapplication.model

import androidx.lifecycle.LiveData
import calmconnectapplication.db.dao.JournalDao
import calmconnectapplication.db.entity.JournalEntry
import calmconnectapplication.util.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JournalRepository(private val journalDao: JournalDao) {

    suspend fun insert(entry: JournalEntry) = withContext(Dispatchers.IO) {
        journalDao.insert(entry)
    }

    fun getAll(): LiveData<List<JournalEntry>> =
        journalDao.getAllByUser(UserSession.uid)
}
