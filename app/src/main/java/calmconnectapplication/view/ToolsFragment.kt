package com.example.calmconnect.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.calmconnect.R
import com.example.calmconnect.databinding.FragmentToolsBinding

class ToolsFragment : Fragment() {

    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cardSoundscapes.setOnClickListener {
            findNavController().navigate(R.id.action_tools_to_soundscapes)
        }
        binding.cardStressRelief.setOnClickListener {
            findNavController().navigate(R.id.action_tools_to_stress_relief)
        }
        binding.cardPomodoro.setOnClickListener {
            findNavController().navigate(R.id.action_tools_to_pomodoro)
        }
        binding.cardGames.setOnClickListener {
            findNavController().navigate(R.id.action_tools_to_games)
        }
        binding.cardQuotes.setOnClickListener {
            findNavController().navigate(R.id.action_tools_to_quotes)
        }
        binding.cardNearbyPlaces.setOnClickListener {
            findNavController().navigate(R.id.action_tools_to_nearby_places)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
