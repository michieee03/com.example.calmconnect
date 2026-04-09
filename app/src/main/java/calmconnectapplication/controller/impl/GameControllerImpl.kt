package calmconnectapplication.controller.impl

import android.content.Context
import android.content.SharedPreferences
import calmconnectapplication.controller.GameController
import calmconnectapplication.model.GameSession
import calmconnectapplication.model.GameType

class GameControllerImpl(context: Context) : GameController {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mindful_games_prefs", Context.MODE_PRIVATE)

    override fun startGame(type: GameType): GameSession {
        return GameSession(type = type, isActive = true)
    }

    override fun recordScore(type: GameType, score: Int) {
        val currentHighScore = getHighScore(type)
        if (score > currentHighScore) {
            prefs.edit().putInt(getKey(type), score).apply()
        }
    }

    override fun getHighScore(type: GameType): Int {
        return prefs.getInt(getKey(type), 0)
    }

    private fun getKey(type: GameType): String {
        return when (type) {
            GameType.TAPPING -> "high_score_tapping"
            GameType.MEMORY -> "high_score_memory"
            GameType.BREATHING -> "high_score_breathing"
        }
    }
}