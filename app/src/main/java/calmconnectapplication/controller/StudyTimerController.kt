package com.example.calmconnect.controller

import androidx.lifecycle.LiveData
import com.example.calmconnect.model.TimerState
import com.example.calmconnect.util.Result

interface StudyTimerController {
    fun startSession(workMinutes: Int, breakMinutes: Int): Result<Unit>
    fun pause()
    fun resume()
    fun reset()
    fun getSessionState(): LiveData<TimerState>
}
