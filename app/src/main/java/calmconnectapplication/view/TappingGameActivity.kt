package calmconnectapplication.view

import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import calmconnectapplication.controller.impl.GameControllerImpl
import calmconnectapplication.databinding.ActivityTappingGameBinding
import calmconnectapplication.model.GameType

class TappingGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTappingGameBinding
    private lateinit var gameController: GameControllerImpl

    private var score = 0
    private var gameStarted = false
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTappingGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameController = GameControllerImpl(this)

        binding.btnClose.setOnClickListener { finish() }

        binding.tvScore.text = "Score: 0"
        binding.tvTimer.text = "Time: 10"
        binding.tvResult.text = "Tap the button as fast as you can!"

        binding.btnTap.setOnClickListener {
            if (!gameStarted) {
                startGame()
            }
            if (gameStarted) {
                score++
                binding.tvScore.text = "Score: $score"
            }
        }

        binding.btnRestart.setOnClickListener {
            resetGame()
        }
    }

    private fun startGame() {
        gameStarted = true
        score = 0
        binding.tvScore.text = "Score: 0"
        binding.tvResult.text = "Go!"

        timer?.cancel()
        timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text = "Time: ${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                gameStarted = false
                binding.tvTimer.text = "Time: 0"
                binding.tvResult.text = "Game Over! Final Score: $score"
                gameController.recordScore(GameType.TAPPING, score)
            }
        }.start()
    }

    private fun resetGame() {
        timer?.cancel()
        gameStarted = false
        score = 0
        binding.tvScore.text = "Score: 0"
        binding.tvTimer.text = "Time: 10"
        binding.tvResult.text = "Tap the button as fast as you can!"
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}