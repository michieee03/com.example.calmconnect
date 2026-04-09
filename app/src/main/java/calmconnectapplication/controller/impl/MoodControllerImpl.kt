package calmconnectapplication.controller.impl

import androidx.lifecycle.LiveData
import calmconnectapplication.controller.MoodController
import calmconnectapplication.db.entity.MoodEntry
import calmconnectapplication.model.MoodRepository
import calmconnectapplication.util.Constants
import calmconnectapplication.util.Result
import calmconnectapplication.util.UserSession
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class MoodControllerImpl(private val moodRepository: MoodRepository) : MoodController {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun saveMood(emotion: String, note: String?, timestamp: Long): Result<Unit> {
        if (emotion.isBlank()) {
            return Result.Error("Emotion must be selected")
        }
        if (emotion !in Constants.VALID_EMOTIONS_SET) {
            return Result.Error("Invalid emotion value")
        }
        if (note != null && note.length > 500) {
            return Result.Error("Note exceeds 500 characters")
        }

        val date = dateFormatter.format(Date(timestamp))
        val entry = MoodEntry(
            userId = UserSession.uid,
            emotion = emotion,
            note = note,
            timestamp = timestamp,
            date = date
        )

        runBlocking { moodRepository.insert(entry) }
        return Result.Success(Unit)
    }

    override fun getMoodHistory(): LiveData<List<MoodEntry>> = moodRepository.getMoodHistory()

    override fun getMoodForDate(date: LocalDate): MoodEntry? {
        // Synchronous lookup not directly supported by LiveData-based repo;
        // returns null as a safe default — callers should use getMoodHistory() for reactive access.
        return null
    }

    override fun deleteMood(id: Int): Result<Unit> {
        runBlocking { moodRepository.deleteById(id) }
        return Result.Success(Unit)
    }
}
