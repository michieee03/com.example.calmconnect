package calmconnectapplication.controller.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import calmconnectapplication.controller.StudyTimerController
import calmconnectapplication.model.TimerPhase
import calmconnectapplication.model.TimerState
import calmconnectapplication.util.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StudyTimerControllerImpl : StudyTimerController {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _sessionState = MutableLiveData<TimerState>()
    override fun getSessionState(): LiveData<TimerState> = _sessionState

    private var sessionJob: Job? = null

    @Volatile private var isPaused = false

    // Stored so reset() can restore the original durations
    private var savedWorkMinutes: Int = 25
    private var savedBreakMinutes: Int = 5

    override fun startSession(workMinutes: Int, breakMinutes: Int): Result<Unit> {
        if (workMinutes < 1 || workMinutes > 60) {
            return Result.Error("workMinutes must be in range [1, 60]")
        }
        if (breakMinutes < 1 || breakMinutes > 30) {
            return Result.Error("breakMinutes must be in range [1, 30]")
        }

        savedWorkMinutes = workMinutes
        savedBreakMinutes = breakMinutes
        isPaused = false

        // Cancel any existing session before starting a new one
        sessionJob?.cancel()
        sessionJob = scope.launch {
            runSession(workMinutes, breakMinutes)
        }

        return Result.Success(Unit)
    }

    override fun pause() {
        isPaused = true
    }

    override fun resume() {
        isPaused = false
    }

    override fun reset() {
        sessionJob?.cancel()
        sessionJob = null
        isPaused = false
        _sessionState.postValue(
            TimerState(
                phase = TimerPhase.WORK,
                remainingSeconds = savedWorkMinutes * 60,
                completedPomodoros = 0
            )
        )
    }

    // ---------------------------------------------------------------------------
    // Internal coroutine logic
    // ---------------------------------------------------------------------------

    private suspend fun runSession(workMinutes: Int, breakMinutes: Int) {
        var completedPomodoros = 0

        while (true) {
            // --- Work phase ---
            var remaining = workMinutes * 60
            while (remaining > 0) {
                _sessionState.postValue(
                    TimerState(
                        phase = TimerPhase.WORK,
                        remainingSeconds = maxOf(0, remaining),
                        completedPomodoros = completedPomodoros
                    )
                )
                waitOneSecond()
                remaining--
            }
            // Emit the final 0-second state for the work phase
            _sessionState.postValue(
                TimerState(
                    phase = TimerPhase.WORK,
                    remainingSeconds = 0,
                    completedPomodoros = completedPomodoros
                )
            )

            completedPomodoros++

            // --- Break phase ---
            val (breakPhase, breakDuration) = if (completedPomodoros % 4 == 0) {
                TimerPhase.LONG_BREAK to breakMinutes * 3
            } else {
                TimerPhase.SHORT_BREAK to breakMinutes * 60
            }

            remaining = breakDuration
            while (remaining > 0) {
                _sessionState.postValue(
                    TimerState(
                        phase = breakPhase,
                        remainingSeconds = maxOf(0, remaining),
                        completedPomodoros = completedPomodoros
                    )
                )
                waitOneSecond()
                remaining--
            }
            // Emit the final 0-second state for the break phase
            _sessionState.postValue(
                TimerState(
                    phase = breakPhase,
                    remainingSeconds = 0,
                    completedPomodoros = completedPomodoros
                )
            )
        }
    }

    /**
     * Waits approximately one second, polling every 100 ms while paused so that
     * pause/resume is responsive without burning CPU.
     */
    private suspend fun waitOneSecond() {
        var elapsed = 0L
        while (elapsed < 1000L) {
            if (isPaused) {
                // Spin-wait in 100 ms increments while paused; don't advance elapsed time
                delay(100L)
            } else {
                delay(100L)
                elapsed += 100L
            }
        }
    }
}
