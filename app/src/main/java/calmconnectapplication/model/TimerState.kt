package calmconnectapplication.model

data class TimerState(
    val phase: TimerPhase,
    val remainingSeconds: Int,
    val completedPomodoros: Int
)
