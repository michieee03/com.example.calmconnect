package com.example.calmconnect.controller

import androidx.lifecycle.LiveData
import com.example.calmconnect.db.entity.RoutineStep
import com.example.calmconnect.util.Result

interface RoutineController {
    fun getTodayRoutine(): LiveData<List<RoutineStep>>
    fun markStepComplete(stepId: Int): Result<Unit>
    fun resetDailyRoutine()
    fun getCompletionPercentage(): Float
}
