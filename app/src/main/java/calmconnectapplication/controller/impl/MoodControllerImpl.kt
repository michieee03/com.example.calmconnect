package com.example.calmconnect.controller.impl

import androidx.lifecycle.LiveData
import com.example.calmconnect.controller.MoodController
import com.example.calmconnect.db.entity.MoodEntry
import com.example.calmconnect.model.MoodRepository
import com.example.calmconnect.util.Constants
import com.example.calmconnect.util.Result
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
