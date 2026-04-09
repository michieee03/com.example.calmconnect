package calmconnectapplication.controller

import androidx.lifecycle.LiveData
import calmconnectapplication.db.entity.Quote
import calmconnectapplication.util.Result

interface QuoteController {
    fun getDailyQuote(): Quote
    fun saveToFavorites(quoteId: Int): Result<Unit>
    fun getFavoriteQuotes(): LiveData<List<Quote>>
    fun searchQuotes(query: String): List<Quote>
    fun removeFavorite(quoteId: Int): Result<Unit>
}
