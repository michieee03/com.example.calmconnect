package calmconnectapplication.controller

import androidx.lifecycle.LiveData
import calmconnectapplication.db.entity.JournalEntry
import calmconnectapplication.model.BreathingPattern
import calmconnectapplication.model.BreathingSession
import calmconnectapplication.model.MeditationSession
import calmconnectapplication.util.Result

interface StressReliefController {
    fun startBreathingExercise(pattern: BreathingPattern): BreathingSession
    fun startMeditation(durationMinutes: Int): MeditationSession
    fun saveJournalEntry(text: String, timestamp: Long): Result<Unit>
    fun getJournalEntries(): LiveData<List<JournalEntry>>
}
