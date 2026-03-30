package com.example.calmconnect.controller

import androidx.lifecycle.LiveData
import com.example.calmconnect.db.entity.JournalEntry
import com.example.calmconnect.model.BreathingPattern
import com.example.calmconnect.model.BreathingSession
import com.example.calmconnect.model.MeditationSession
import com.example.calmconnect.util.Result

interface StressReliefController {
    fun startBreathingExercise(pattern: BreathingPattern): BreathingSession
    fun startMeditation(durationMinutes: Int): MeditationSession
    fun saveJournalEntry(text: String, timestamp: Long): Result<Unit>
    fun getJournalEntries(): LiveData<List<JournalEntry>>
}
