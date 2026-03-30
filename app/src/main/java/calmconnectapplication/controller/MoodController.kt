package com.example.calmconnect.controller

import androidx.lifecycle.LiveData
import com.example.calmconnect.util.Result
import java.time.LocalDate

interface MoodController {
    fun saveMood(emotion: String, note: String?, timestamp: Long): Result<Unit>
    fun getMoodHistory(): LiveData<List<com.example.calmconnect.db.entity.MoodEntry>>
    fun getMoodForDate(date: LocalDate): com.example.calmconnect.db.entity.MoodEntry?
    fun deleteMood(id: Int): Result<Unit>
}
