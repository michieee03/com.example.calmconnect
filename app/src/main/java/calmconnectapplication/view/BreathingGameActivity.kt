package calmconnectapplication.view

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import calmconnectapplication.controller.impl.GameControllerImpl
import calmconnectapplication.databinding.ActivityBreathingGameBinding
import calmconnectapplication.model.GameType

class BreathingGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBreathingGameBinding
    private lateinit var gameController: GameControllerImpl

    private var completedRounds = 0
    private var timer: CountDownTimer? = null
    private var currentStep = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBreathingGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameController = GameControllerImpl(this)

        binding.btnClose.setOnClickListener { finish() }

        binding.tvRounds.text = "Rounds: 0"
        binding.tvInstruction.text = "Press Start"

        binding.btnStartBreathing.setOnClickListener {
            startBreathingExercise()
        }

        binding.btnRestart.setOnClickListener {
            resetBreathingExercise()
        }
    }

    private fun startBreathingExercise() {
        completedRounds = 0
        currentStep = 0
        binding.tvRounds.text = "Rounds: 0"
        binding.tvInstruction.text = "Get ready..."

        timer?.cancel()
        runBreathingCycle()
    }

    private fun runBreathingCycle() {
        timer = object : CountDownTimer(16000, 4000) {
            override fun onTick(millisUntilFinished: Long) {
                when (currentStep) {
                    0 -> {
                        binding.tvInstruction.text = "Inhale"
                        animateCircle(1.5f, 4000)
                    }
                    1 -> {
                        binding.tvInstruction.text = "Hold"
                        animateCircle(1.5f, 4000)
                    }
                    2 -> {
                        binding.tvInstruction.text = "Exhale"
                        animateCircle(1.0f, 4000)
                    }
                    3 -> {
                        binding.tvInstruction.text = "Relax"
                        animateCircle(1.0f, 4000)
                    }
                }
                currentStep++
            }

            override fun onFinish() {
                completedRounds++
                binding.tvRounds.text = "Rounds: $completedRounds"
                binding.tvInstruction.text = "Great job!"

                gameController.recordScore(GameType.BREATHING, completedRounds)
            }
        }.start()
    }

    private fun animateCircle(targetScale: Float, duration: Long) {
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", targetScale)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", targetScale)

        ObjectAnimator.ofPropertyValuesHolder(binding.breathingCircle, scaleX, scaleY).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun resetBreathingExercise() {
        timer?.cancel()
        completedRounds = 0
        currentStep = 0
        binding.tvRounds.text = "Rounds: 0"
        binding.tvInstruction.text = "Press Start"
        binding.breathingCircle.scaleX = 1f
        binding.breathingCircle.scaleY = 1f
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}