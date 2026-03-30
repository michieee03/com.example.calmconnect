package com.example.calmconnect.controller.impl

import androidx.lifecycle.LiveData
import com.example.calmconnect.controller.QuoteController
import com.example.calmconnect.db.entity.Quote
import com.example.calmconnect.model.QuoteRepository
import com.example.calmconnect.util.Result
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class QuoteControllerImpl(private val quoteRepository: QuoteRepository) : QuoteController {

    companion object {
        private val BUNDLED_QUOTES = listOf(
            Quote(1, "The present moment is the only moment available to us, and it is the door to all moments.", "Thich Nhat Hanh"),
            Quote(2, "You don't have to control your thoughts. You just have to stop letting them control you.", "Dan Millman"),
            Quote(3, "Almost everything will work again if you unplug it for a few minutes, including you.", "Anne Lamott"),
            Quote(4, "In the middle of difficulty lies opportunity.", "Albert Einstein"),
            Quote(5, "Breathe. Let go. And remind yourself that this very moment is the only one you know you have for sure.", "Oprah Winfrey"),
            Quote(6, "The greatest weapon against stress is our ability to choose one thought over another.", "William James"),
            Quote(7, "You are enough just as you are.", "Meghan Markle"),
            Quote(8, "Peace comes from within. Do not seek it without.", "Buddha"),
            Quote(9, "Nothing is permanent. Everything is subject to change.", "Buddha"),
            Quote(10, "Be gentle with yourself. You are a child of the universe.", "Max Ehrmann")
        )
    }

    init {
        runBlocking {
            val existing = quoteRepository.getAll()
            if (existing.isEmpty()) {
                quoteRepository.insertAll(BUNDLED_QUOTES)
            }
        }
    }

    override fun getDailyQuote(): Quote {
        val quotes = runBlocking { quoteRepository.getAll() }
        val seed = LocalDate.now().toEpochDay().toInt()
        val index = Math.abs(seed) % quotes.size
        return quotes[index]
    }

    override fun saveToFavorites(quoteId: Int): Result<Unit> {
        val quote = runBlocking { quoteRepository.getById(quoteId) }
            ?: return Result.Error("Quote not found")
        runBlocking { quoteRepository.update(quote.copy(isFavorite = true)) }
        return Result.Success(Unit)
    }

    override fun removeFavorite(quoteId: Int): Result<Unit> {
        val quote = runBlocking { quoteRepository.getById(quoteId) }
            ?: return Result.Error("Quote not found")
        runBlocking { quoteRepository.update(quote.copy(isFavorite = false)) }
        return Result.Success(Unit)
    }

    override fun getFavoriteQuotes(): LiveData<List<Quote>> = quoteRepository.getFavorites()

    override fun searchQuotes(query: String): List<Quote> =
        runBlocking { quoteRepository.search(query) }
}
