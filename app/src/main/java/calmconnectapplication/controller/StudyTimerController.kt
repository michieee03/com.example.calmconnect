package calmconnectapplication.controller

import androidx.lifecycle.LiveData
import calmconnectapplication.model.TimerState
import calmconnectapplication.util.Result

interface StudyTimerController {
    fun startSession(workMinutes: Int, breakMinutes: Int): Result<Unit>
    fun pause()
    fun resume()
    fun reset()
    fun getSessionState(): LiveData<TimerState>
}
