package com.example.calmconnect.controller.impl

import androidx.lifecycle.LiveData
import com.example.calmconnect.controller.RoutineController
import com.example.calmconnect.db.entity.RoutineStep
import com.example.calmconnect.model.RoutineRepository
import com.example.calmconnect.util.Result
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class RoutineControllerImpl(private val routineRepository: RoutineRepository) : RoutineController {

    companion object {
        private val DEFAULT_STEPS = listOf(
            Triple(1, "Morning Meditation", "5 minutes of mindful breathing" to 5),
            Triple(2, "Gratitude Journal", "Write 3 things you're grateful for" to 5),
            Triple(3, "Gentle Stretch", "Light stretching to wake up your body" to 10),
            Triple(4, "Healthy Breakfast", "Eat a nutritious meal to start your day" to 15),
            Triple(5, "Daily Walk", "Take a 10-minute walk outside" to 10)
        )
    }

    private val today: String get() = LocalDate.now().toString()

    init {
        runBlocking {
            val existing = routineRepository.getByDateSync(today)
            if (existing.isEmpty()) {
                val steps = DEFAULT_STEPS.map { (id, title, descDuration) ->
                    RoutineStep(
                        id = id,
                        title = title,
                        description = descDuration.first,
                        durationMinutes = descDuration.second,
                        isCompleted = false,
                        date = today
                    )
                }
                routineRepository.insertAll(steps)
            }
        }
    }

    override fun getTodayRoutine(): LiveData<List<RoutineStep>> =
        routineRepository.getByDate(today)

    override fun markStepComplete(stepId: Int): Result<Unit> {
        val steps = runBlocking { routineRepository.getByDateSync(today) }
        val step = steps.find { it.id == stepId }
            ?: return Result.Error("Step with id $stepId not found for today")
        runBlocking { routineRepository.update(step.copy(isCompleted = true)) }
        return Result.Success(Unit)
    }

    override fun getCompletionPercentage(): Float {
        val steps = runBlocking { routineRepository.getByDateSync(today) }
        if (steps.isEmpty()) return 0.0f
        val completedCount = steps.count { it.isCompleted }
        return (completedCount.toFloat() / steps.size).coerceIn(0.0f, 1.0f)
    }

    override fun resetDailyRoutine() {
        val steps = runBlocking { routineRepository.getByDateSync(today) }
        runBlocking {
            steps.forEach { step ->
                routineRepository.update(step.copy(isCompleted = false))
            }
        }
    }
}
