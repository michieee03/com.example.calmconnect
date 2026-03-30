package com.example.calmconnect.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.calmconnect.controller.impl.GameControllerImpl
import com.example.calmconnect.databinding.FragmentGamesBinding
import com.example.calmconnect.model.GameType

class GamesFragment : Fragment() {

    private var _binding: FragmentGamesBinding? = null
    private val binding get() = _binding!!

    private lateinit var gameController: GameControllerImpl

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGamesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gameController = GameControllerImpl(requireContext())
        refreshHighScores()

        binding.cardTapping.setOnClickListener {
            gameController.startGame(GameType.TAPPING)
            startActivity(Intent(requireContext(), TappingGameActivity::class.java))
        }

        binding.cardMemory.setOnClickListener {
            gameController.startGame(GameType.MEMORY)
            startActivity(Intent(requireContext(), MemoryGameActivity::class.java))
        }

        binding.cardBreathingGame.setOnClickListener {
            gameController.startGame(GameType.BREATHING)
            startActivity(Intent(requireContext(), BreathingGameActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHighScores()
    }

    private fun refreshHighScores() {
        binding.tvTappingHighScore.text =
            "High Score: ${gameController.getHighScore(GameType.TAPPING)}"

        binding.tvMemoryHighScore.text =
            "High Score: ${gameController.getHighScore(GameType.MEMORY)}"

        binding.tvBreathingGameHighScore.text =
            "High Score: ${gameController.getHighScore(GameType.BREATHING)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}