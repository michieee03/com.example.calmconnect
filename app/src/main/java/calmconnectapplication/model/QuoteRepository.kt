package calmconnectapplication.model

import androidx.lifecycle.LiveData
import calmconnectapplication.db.dao.QuoteDao
import calmconnectapplication.db.entity.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteRepository(private val quoteDao: QuoteDao) {

    suspend fun insertAll(quotes: List<Quote>) = withContext(Dispatchers.IO) {
        quoteDao.insertAll(quotes)
    }

    suspend fun update(quote: Quote) = withContext(Dispatchers.IO) {
        quoteDao.update(quote)
    }

    suspend fun getAll(): List<Quote> = withContext(Dispatchers.IO) {
        quoteDao.getAll()
    }

    fun getFavorites(): LiveData<List<Quote>> = quoteDao.getFavorites()

    suspend fun getById(id: Int): Quote? = withContext(Dispatchers.IO) {
        quoteDao.getById(id)
    }

    suspend fun search(query: String): List<Quote> = withContext(Dispatchers.IO) {
        quoteDao.searchByTextOrAuthor(query)
    }
}
