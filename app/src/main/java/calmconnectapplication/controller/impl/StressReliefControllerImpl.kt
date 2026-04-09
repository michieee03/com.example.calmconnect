package calmconnectapplication.controller.impl

import androidx.lifecycle.LiveData
import calmconnectapplication.controller.StressReliefController
import calmconnectapplication.db.entity.JournalEntry
import calmconnectapplication.model.BreathingPattern
import calmconnectapplication.model.BreathingSession
import calmconnectapplication.model.JournalRepository
import calmconnectapplication.model.MeditationSession
import calmconnectapplication.util.Result
import calmconnectapplication.util.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StressReliefControllerImpl(
    private val journalRepository: JournalRepository
) : StressReliefController {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        // Each phase is a Pair(label, durationSeconds)
        private val PHASE_SEQUENCES: Map<BreathingPattern, List<Pair<String, Int>>> = mapOf(
            BreathingPattern.BOX_4_4_4_4 to listOf(
                "Inhale" to 4,
                "Hold" to 4,
                "Exhale" to 4,
                "Hold" to 4
            ),
            BreathingPattern.RELAXING_4_7_8 to listOf(
                "Inhale" to 4,
                "Hold" to 7,
                "Exhale" to 8
            ),
            BreathingPattern.ENERGIZING_2_2_4 to listOf(
                "Inhale" to 2,
                "Hold" to 2,
                "Exhale" to 4
            )
        )

        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    override fun startBreathingExercise(pattern: BreathingPattern): BreathingSession {
        val session = BreathingSession(pattern = pattern, isActive = true)
        val phases = PHASE_SEQUENCES[pattern] ?: emptyList()

        scope.launch {
            while (session.isActive) {
                for ((label, durationSeconds) in phases) {
                    if (!session.isActive) break
                    session.currentPhase.postValue(label)
                    delay(durationSeconds * 1000L)
                }
            }
        }

        return session
    }

    override fun startMeditation(durationMinutes: Int): MeditationSession {
        val totalSeconds = durationMinutes * 60
        val session = MeditationSession(durationMinutes = durationMinutes, isActive = true)
        session.remainingSeconds.postValue(totalSeconds)

        scope.launch {
            var remaining = totalSeconds
            while (remaining > 0 && session.isActive) {
                delay(1000L)
                remaining--
                session.remainingSeconds.postValue(remaining)
            }
        }

        return session
    }

    override fun saveJournalEntry(text: String, timestamp: Long): Result<Unit> {
        if (text.isEmpty()) {
            return Result.Error("Journal entry text cannot be empty")
        }
        if (text.length > 2000) {
            return Result.Error("Journal entry text cannot exceed 2000 characters")
        }

        val date = DATE_FORMAT.format(Date(timestamp))
        val entry = JournalEntry(userId = UserSession.uid, text = text, timestamp = timestamp, date = date)
        runBlocking { journalRepository.insert(entry) }
        return Result.Success(Unit)
    }

    override fun getJournalEntries(): LiveData<List<JournalEntry>> =
        journalRepository.getAll()
}
