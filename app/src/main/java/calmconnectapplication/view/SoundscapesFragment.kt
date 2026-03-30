package com.example.calmconnect.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.calmconnect.controller.impl.SoundControllerImpl
import com.example.calmconnect.databinding.FragmentSoundscapesBinding
import com.example.calmconnect.model.SoundType

class SoundscapesFragment : Fragment() {

    private var _binding: FragmentSoundscapesBinding? = null
    private val binding get() = _binding!!

    private lateinit var soundController: SoundControllerImpl

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSoundscapesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        soundController = SoundControllerImpl(requireContext()) {
            Toast.makeText(requireContext(), "Playback error: audio focus denied", Toast.LENGTH_SHORT).show()
        }

        // Set initial volume from seekbar
        val initialVolume = binding.seekBarVolume.progress / 100f
        soundController.setVolume(initialVolume)

        binding.btnPlay.setOnClickListener {
            val soundType = selectedSoundType()
            if (soundType == null) {
                Toast.makeText(requireContext(), "Please select a sound", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            soundController.play(soundType)
        }

        binding.btnPause.setOnClickListener { soundController.pause() }

        binding.btnStop.setOnClickListener { soundController.stop() }

        binding.seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                soundController.setVolume(progress / 100f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.btnSetTimer.setOnClickListener {
            val minutes = binding.editTextTimer.text.toString().toIntOrNull() ?: 0
            if (minutes > 0) {
                soundController.setTimer(minutes)
                Toast.makeText(requireContext(), "Timer set for $minutes min", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectedSoundType(): SoundType? = when (binding.radioGroupSounds.checkedRadioButtonId) {
        binding.radioRainDrops.id -> SoundType.RAIN_DROPS
        binding.radioOceanWaves.id -> SoundType.OCEAN_WAVES
        binding.radioGentlePiano.id -> SoundType.GENTLE_PIANO
        binding.radioForestAmbience.id -> SoundType.FOREST_AMBIENCE
        else -> null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        soundController.stop()
        _binding = null
    }
}
