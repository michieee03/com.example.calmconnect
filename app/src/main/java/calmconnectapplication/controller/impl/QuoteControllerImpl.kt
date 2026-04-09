package calmconnectapplication.controller.impl

import androidx.lifecycle.LiveData
import calmconnectapplication.controller.QuoteController
import calmconnectapplication.db.entity.Quote
import calmconnectapplication.model.QuoteRepository
import calmconnectapplication.util.Result
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
            Quote(10, "Be gentle with yourself. You are a child of the universe.", "Max Ehrmann"),
            Quote(11, "Happiness is not something ready-made. It comes from your own actions.", "Dalai Lama"),
            Quote(12, "The mind is everything. What you think you become.", "Buddha"),
            Quote(13, "Do not dwell in the past, do not dream of the future, concentrate the mind on the present moment.", "Buddha"),
            Quote(14, "You yourself, as much as anybody in the entire universe, deserve your love and affection.", "Buddha"),
            Quote(15, "Calm mind brings inner strength and self-confidence.", "Dalai Lama"),
            Quote(16, "It's not the load that breaks you down, it's the way you carry it.", "Lou Holtz"),
            Quote(17, "You can't calm the storm, so stop trying. What you can do is calm yourself.", "Timber Hawkeye"),
            Quote(18, "Within you, there is a stillness and a sanctuary to which you can retreat at any time.", "Hermann Hesse"),
            Quote(19, "The secret of health for both mind and body is not to mourn for the past, worry about the future, but to live the present moment wisely.", "Buddha"),
            Quote(20, "Tension is who you think you should be. Relaxation is who you are.", "Chinese Proverb"),
            Quote(21, "Take rest; a field that has rested gives a bountiful crop.", "Ovid"),
            Quote(22, "Almost everything will work again if you unplug it for a few minutes.", "Anne Lamott"),
            Quote(23, "Your calm mind is the ultimate weapon against your challenges.", "Bryant McGill"),
            Quote(24, "Slow down and everything you are chasing will come around and catch you.", "John De Paola"),
            Quote(25, "The time to relax is when you don't have time for it.", "Sydney J. Harris"),
            Quote(26, "Rest when you're weary. Refresh and renew yourself, your body, your mind, your spirit.", "Ralph Marston"),
            Quote(27, "Sometimes the most productive thing you can do is relax.", "Mark Black"),
            Quote(28, "Breathe deeply, until sweet air extinguishes the burn of fear in your lungs.", "Arthur Gregor"),
            Quote(29, "One breath at a time. One moment at a time.", "Unknown"),
            Quote(30, "You don't always need a plan. Sometimes you just need to breathe, trust, let go, and see what happens.", "Mandy Hale"),
            Quote(31, "Inhale the future, exhale the past.", "Unknown"),
            Quote(32, "Feelings come and go like clouds in a windy sky. Conscious breathing is my anchor.", "Thich Nhat Hanh"),
            Quote(33, "Smile, breathe, and go slowly.", "Thich Nhat Hanh"),
            Quote(34, "The present moment always will have been.", "Eckhart Tolle"),
            Quote(35, "Life is available only in the present moment.", "Thich Nhat Hanh"),
            Quote(36, "Wherever you are, be all there.", "Jim Elliot"),
            Quote(37, "Be happy in the moment, that's enough. Each moment is all we need, not more.", "Mother Teresa"),
            Quote(38, "You have power over your mind, not outside events. Realize this, and you will find strength.", "Marcus Aurelius"),
            Quote(39, "The soul that sees beauty may sometimes walk alone.", "Johann Wolfgang von Goethe"),
            Quote(40, "Keep your face always toward the sunshine, and shadows will fall behind you.", "Walt Whitman"),
            Quote(41, "Even the darkest night will end and the sun will rise.", "Victor Hugo"),
            Quote(42, "Stars can't shine without darkness.", "Unknown"),
            Quote(43, "Every day may not be good, but there is something good in every day.", "Alice Morse Earle"),
            Quote(44, "You are braver than you believe, stronger than you seem, and smarter than you think.", "A.A. Milne"),
            Quote(45, "Believe you can and you're halfway there.", "Theodore Roosevelt"),
            Quote(46, "It always seems impossible until it's done.", "Nelson Mandela"),
            Quote(47, "The only way out is through.", "Robert Frost"),
            Quote(48, "Fall seven times, stand up eight.", "Japanese Proverb"),
            Quote(49, "What lies behind us and what lies before us are tiny matters compared to what lies within us.", "Ralph Waldo Emerson"),
            Quote(50, "You are not a drop in the ocean. You are the entire ocean in a drop.", "Rumi"),
            Quote(51, "Out of difficulties grow miracles.", "Jean de la Bruyere"),
            Quote(52, "The wound is the place where the light enters you.", "Rumi"),
            Quote(53, "Don't be pushed around by the fears in your mind. Be led by the dreams in your heart.", "Roy T. Bennett"),
            Quote(54, "Gratitude turns what we have into enough.", "Unknown"),
            Quote(55, "Joy is not in things; it is in us.", "Richard Wagner"),
            Quote(56, "Happiness is a direction, not a place.", "Sydney J. Harris"),
            Quote(57, "The purpose of our lives is to be happy.", "Dalai Lama"),
            Quote(58, "Count your age by friends, not years. Count your life by smiles, not tears.", "John Lennon"),
            Quote(59, "Kindness is a language which the deaf can hear and the blind can see.", "Mark Twain"),
            Quote(60, "Be the change you wish to see in the world.", "Mahatma Gandhi"),
            Quote(61, "In the end, it's not the years in your life that count. It's the life in your years.", "Abraham Lincoln"),
            Quote(62, "The best time to plant a tree was 20 years ago. The second best time is now.", "Chinese Proverb"),
            Quote(63, "An unexamined life is not worth living.", "Socrates"),
            Quote(64, "To thine own self be true.", "William Shakespeare"),
            Quote(65, "This too shall pass.", "Persian Proverb")
        )
    }

    init {
        runBlocking {
            // Always upsert the full bundled list so new quotes are added on app update
            quoteRepository.insertAll(BUNDLED_QUOTES)
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
