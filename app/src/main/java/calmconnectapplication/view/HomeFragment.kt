package com.example.calmconnect.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.calmconnect.R
import com.example.calmconnect.controller.impl.MoodControllerImpl
import com.example.calmconnect.controller.impl.QuoteControllerImpl
import com.example.calmconnect.databinding.FragmentHomeBinding
import com.example.calmconnect.db.AppDatabase
import com.example.calmconnect.model.MoodRepository
import com.example.calmconnect.model.QuoteRepository
import com.example.calmconnect.util.Result
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var moodController: MoodControllerImpl
    private lateinit var quoteController: QuoteControllerImpl

    // Maps chip IDs to emotion string values from VALID_EMOTIONS_SET
    private val chipEmotionMap by lazy {
        mapOf(
            binding.chipHappy.id    to "Happy",
            binding.chipSad.id      to "Sad",
            binding.chipAnxious.id  to "Anxious",
            binding.chipCalm.id     to "Calm",
            binding.chipExcited.id  to "Excited",
            binding.chipTired.id    to "Tired",
            binding.chipAngry.id    to "Angry",
            binding.chipStressed.id to "Stressed",
            binding.chipGrateful.id to "Grateful",
            binding.chipNeutral.id  to "Neutral"
        )
    }

    private var selectedEmotion: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())
        moodController = MoodControllerImpl(MoodRepository(db.moodDao()))
        quoteController = QuoteControllerImpl(QuoteRepository(db.quoteDao()))

        setupEmotionChips()
        displayDailyQuote()
        setupSaveMoodButton()
        setupViewHistoryButton()
    }

    private fun setupViewHistoryButton() {
        binding.btnViewHistory.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_mood_history)
        }
    }

    private fun setupEmotionChips() {
        binding.chipGroupEmotions.setOnCheckedStateChangeListener { group, checkedIds ->
            selectedEmotion = if (checkedIds.isEmpty()) {
                null
            } else {
                chipEmotionMap[checkedIds.first()]
            }
        }
    }

    private fun displayDailyQuote() {
        val quote = quoteController.getDailyQuote()
        binding.tvQuoteText.text = "\u201C${quote.text}\u201D"
        binding.tvQuoteAuthor.text = "— ${quote.author}"
    }

    private fun setupSaveMoodButton() {
        binding.btnSaveMood.setOnClickListener {
            val emotion = selectedEmotion
            if (emotion == null) {
                Snackbar.make(binding.root, "Please select an emotion", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val note = binding.etNote.text?.toString()?.takeIf { it.isNotBlank() }

            when (val result = moodController.saveMood(emotion, note, System.currentTimeMillis())) {
                is Result.Success -> {
                    Snackbar.make(binding.root, "Mood saved!", Snackbar.LENGTH_SHORT).show()
                    clearSelection()
                }
                is Result.Error -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun clearSelection() {
        binding.chipGroupEmotions.clearCheck()
        binding.etNote.text?.clear()
        selectedEmotion = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
