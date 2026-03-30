package com.example.calmconnect.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.calmconnect.controller.impl.GameControllerImpl
import com.example.calmconnect.databinding.ActivityMemoryGameBinding
import com.example.calmconnect.model.GameType

class MemoryGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMemoryGameBinding
    private lateinit var gameController: GameControllerImpl

    private lateinit var cards: List<Button>
    private lateinit var values: MutableList<String>

    private var firstCard: Button? = null
    private var secondCard: Button? = null
    private var firstIndex: Int = -1
    private var secondIndex: Int = -1
    private var isBusy = false

    private var moves = 0
    private var matchedPairs = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoryGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameController = GameControllerImpl(this)

        cards = listOf(
            binding.card1, binding.card2, binding.card3, binding.card4,
            binding.card5, binding.card6, binding.card7, binding.card8,
            binding.card9, binding.card10, binding.card11, binding.card12
        )

        binding.btnRestartMemory.setOnClickListener {
            setupGame()
        }

        setupGame()
    }

    private fun setupGame() {
        val emojis = mutableListOf("🌸", "🌸", "🌈", "🌈", "☁️", "☁️", "🌿", "🌿", "⭐", "⭐", "💧", "💧")
        emojis.shuffle()
        values = emojis

        firstCard = null
        secondCard = null
        firstIndex = -1
        secondIndex = -1
        isBusy = false
        moves = 0
        matchedPairs = 0

        binding.tvMoves.text = "Moves: 0"
        binding.tvPairs.text = "Matched Pairs: 0 / 6"
        binding.tvMemoryResult.text = "Match all the pairs."

        cards.forEachIndexed { index, button ->
            button.text = "?"
            button.isEnabled = true
            button.setTextColor(getColor(android.R.color.white))
            button.setBackgroundResource(com.example.calmconnect.R.drawable.bg_memory_card_hidden)
            button.setOnClickListener {
                onCardClicked(button, index)
            }
        }
    }

    private fun onCardClicked(button: Button, index: Int) {
        if (isBusy) return
        if (button.text != "?") return

        revealCard(button, index)

        if (firstCard == null) {
            firstCard = button
            firstIndex = index
            return
        }

        secondCard = button
        secondIndex = index
        moves++
        binding.tvMoves.text = "Moves: $moves"
        isBusy = true

        if (values[firstIndex] == values[secondIndex]) {
            matchedPairs++
            binding.tvPairs.text = "Matched Pairs: $matchedPairs / 6"
            firstCard?.isEnabled = false
            secondCard?.isEnabled = false
            resetTurn()

            if (matchedPairs == 6) {
                binding.tvMemoryResult.text = "Great job! You matched all pairs."
                val score = 100 - moves
                gameController.recordScore(GameType.MEMORY, score.coerceAtLeast(1))
            } else {
                binding.tvMemoryResult.text = "Nice! That is a match."
            }
        } else {
            binding.tvMemoryResult.text = "Try again."
            Handler(Looper.getMainLooper()).postDelayed({
                hideCard(firstCard, firstIndex)
                hideCard(secondCard, secondIndex)
                resetTurn()
            }, 800)
        }
    }

    private fun revealCard(button: Button, index: Int) {
        button.text = values[index]
        button.setTextColor(getColor(com.example.calmconnect.R.color.calm_text))
        button.setBackgroundResource(com.example.calmconnect.R.drawable.bg_memory_card_revealed)
    }

    private fun hideCard(button: Button?, index: Int) {
        if (button == null || index == -1) return
        button.text = "?"
        button.setTextColor(getColor(android.R.color.white))
        button.setBackgroundResource(com.example.calmconnect.R.drawable.bg_memory_card_hidden)
    }

    private fun resetTurn() {
        firstCard = null
        secondCard = null
        firstIndex = -1
        secondIndex = -1
        isBusy = false
    }
}