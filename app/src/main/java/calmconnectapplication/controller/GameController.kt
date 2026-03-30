package com.example.calmconnect.controller

import com.example.calmconnect.model.GameSession
import com.example.calmconnect.model.GameType

interface GameController {
    fun startGame(type: GameType): GameSession
    fun recordScore(type: GameType, score: Int)
    fun getHighScore(type: GameType): Int
}