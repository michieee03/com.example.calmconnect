package calmconnectapplication.controller

import androidx.lifecycle.LiveData
import calmconnectapplication.db.entity.RoutineStep
import calmconnectapplication.util.Result

interface RoutineController {
    fun getTodayRoutine(): LiveData<List<RoutineStep>>
    fun markStepComplete(stepId: Int): Result<Unit>
    fun resetDailyRoutine()
    fun getCompletionPercentage(): Float
}
