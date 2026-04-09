package calmconnectapplication.controller

import calmconnectapplication.model.GameSession
import calmconnectapplication.model.GameType

interface GameController {
    fun startGame(type: GameType): GameSession
    fun recordScore(type: GameType, score: Int)
    fun getHighScore(type: GameType): Int
}