package calmconnectapplication.controller

import androidx.lifecycle.LiveData
import calmconnectapplication.util.Result
import java.time.LocalDate

interface MoodController {
    fun saveMood(emotion: String, note: String?, timestamp: Long): Result<Unit>
    fun getMoodHistory(): LiveData<List<calmconnectapplication.db.entity.MoodEntry>>
    fun getMoodForDate(date: LocalDate): calmconnectapplication.db.entity.MoodEntry?
    fun deleteMood(id: Int): Result<Unit>
}
