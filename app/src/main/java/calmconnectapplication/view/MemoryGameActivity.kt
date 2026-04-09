package calmconnectapplication.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import calmconnectapplication.controller.impl.GameControllerImpl
import calmconnectapplication.model.GameType
import calmconnectapplication.databinding.ActivityMemoryGameBinding
import calmconnectapplication.R

class MemoryGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMemoryGameBinding
    private lateinit var gameController: GameControllerImpl

    private val allEmojis = listOf(
        "🌸", "🌈", "☁️", "🌿", "⭐", "💧",
        "🦋", "🌺", "🍃", "🌙", "🔮", "🌊"
    )

    private var cardButtons = mutableListOf<Button>()
    private var values = mutableListOf<String>()
    private var firstCard: Button? = null
    private var secondCard: Button? = null
    private var firstIndex = -1
    private var secondIndex = -1
    private var isBusy = false
    private var moves = 0
    private var matchedPairs = 0
    private var totalPairs = 6
    private var currentDifficulty = 12 // default Easy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoryGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameController = GameControllerImpl(this)

        binding.btnClose.setOnClickListener { finish() }

        binding.btnEasy.setOnClickListener {
            currentDifficulty = 12
            highlightDifficulty(0)
            setupGame()
        }
        binding.btnMedium.setOnClickListener {
            currentDifficulty = 18
            highlightDifficulty(1)
            setupGame()
        }
        binding.btnHard.setOnClickListener {
            currentDifficulty = 24
            highlightDifficulty(2)
            setupGame()
        }
        binding.btnRestartMemory.setOnClickListener { setupGame() }

        highlightDifficulty(0)
        setupGame()
    }

    private fun highlightDifficulty(selected: Int) {
        val colors = listOf(
            binding.btnEasy to if (selected == 0) "#4CAF93" else "#A8D5C2",
            binding.btnMedium to if (selected == 1) "#8FA8D8" else "#C5D8EA",
            binding.btnHard to if (selected == 2) "#B09AE0" else "#D4C5F0"
        )
        colors.forEach { (btn, color) ->
            btn.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor(color)
            )
        }
    }

    private fun setupGame() {
        totalPairs = currentDifficulty / 2
        val columns = if (currentDifficulty <= 12) 3 else if (currentDifficulty <= 18) 3 else 4

        // Build shuffled emoji list
        val emojiPool = allEmojis.take(totalPairs)
        val shuffled = (emojiPool + emojiPool).shuffled().toMutableList()
        values = shuffled

        // Reset state
        firstCard = null; secondCard = null
        firstIndex = -1; secondIndex = -1
        isBusy = false; moves = 0; matchedPairs = 0

        binding.tvMoves.text = "Moves: 0"
        binding.tvPairs.text = "Pairs: 0 / $totalPairs"
        binding.tvMemoryResult.text = "Match all the pairs."

        // Rebuild grid
        binding.gridMemory.removeAllViews()
        binding.gridMemory.columnCount = columns
        cardButtons.clear()

        val cardSize = when (currentDifficulty) {
            12 -> 100
            18 -> 90
            else -> 80
        }
        val dpSize = (cardSize * resources.displayMetrics.density).toInt()

        for (i in 0 until currentDifficulty) {
            val btn = Button(this).apply {
                text = "?"
                textSize = if (currentDifficulty <= 12) 22f else 18f
                isAllCaps = false
                setTextColor(getColor(android.R.color.white))
                setBackgroundResource(R.drawable.bg_memory_card_hidden)
                val params = GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
                ).also {
                    it.width = 0
                    it.height = dpSize
                    it.setMargins(4, 4, 4, 4)
                }
                layoutParams = params
                tag = i
            }
            btn.setOnClickListener { onCardClicked(btn, i) }
            binding.gridMemory.addView(btn)
            cardButtons.add(btn)
        }
    }

    private fun onCardClicked(button: Button, index: Int) {
        if (isBusy || button.text != "?") return

        revealCard(button, index)

        if (firstCard == null) {
            firstCard = button; firstIndex = index; return
        }

        secondCard = button; secondIndex = index
        moves++
        binding.tvMoves.text = "Moves: $moves"
        isBusy = true

        if (values[firstIndex] == values[secondIndex]) {
            matchedPairs++
            binding.tvPairs.text = "Pairs: $matchedPairs / $totalPairs"
            firstCard?.isEnabled = false
            secondCard?.isEnabled = false
            resetTurn()

            if (matchedPairs == totalPairs) {
                binding.tvMemoryResult.text = "🎉 You matched all pairs in $moves moves!"
                val score = (totalPairs * 10) - moves
                gameController.recordScore(GameType.MEMORY, score.coerceAtLeast(1))
            } else {
                binding.tvMemoryResult.text = "✅ Match!"
            }
        } else {
            binding.tvMemoryResult.text = "❌ Try again."
            Handler(Looper.getMainLooper()).postDelayed({
                hideCard(firstCard, firstIndex)
                hideCard(secondCard, secondIndex)
                resetTurn()
            }, 800)
        }
    }

    private fun revealCard(button: Button, index: Int) {
        button.text = values[index]
        button.setTextColor(getColor(R.color.calm_text))
        button.setBackgroundResource(R.drawable.bg_memory_card_revealed)
    }

    private fun hideCard(button: Button?, index: Int) {
        if (button == null || index == -1) return
        button.text = "?"
        button.setTextColor(getColor(android.R.color.white))
        button.setBackgroundResource(R.drawable.bg_memory_card_hidden)
    }

    private fun resetTurn() {
        firstCard = null; secondCard = null
        firstIndex = -1; secondIndex = -1
        isBusy = false
    }
}
