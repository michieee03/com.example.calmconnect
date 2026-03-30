package com.example.calmconnect.controller

import androidx.lifecycle.LiveData
import com.example.calmconnect.db.entity.Quote
import com.example.calmconnect.util.Result

interface QuoteController {
    fun getDailyQuote(): Quote
    fun saveToFavorites(quoteId: Int): Result<Unit>
    fun getFavoriteQuotes(): LiveData<List<Quote>>
    fun searchQuotes(query: String): List<Quote>
    fun removeFavorite(quoteId: Int): Result<Unit>
}
