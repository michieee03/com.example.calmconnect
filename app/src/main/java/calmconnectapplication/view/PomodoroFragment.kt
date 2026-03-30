package com.example.calmconnect.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.calmconnect.controller.impl.StudyTimerControllerImpl
import com.example.calmconnect.databinding.FragmentPomodoroBinding
import com.example.calmconnect.util.Result

class PomodoroFragment : Fragment() {

    private var _binding: FragmentPomodoroBinding? = null
    private val binding get() = _binding!!

    private val timerController = StudyTimerControllerImpl()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPomodoroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timerController.getSessionState().observe(viewLifecycleOwner) { state ->
            val mm = state.remainingSeconds / 60
            val ss = state.remainingSeconds % 60
            binding.tvTimer.text = "%02d:%02d".format(mm, ss)
            binding.tvPhase.text = state.phase.name.replace('_', ' ')
            binding.tvPomodoros.text = "Completed: ${state.completedPomodoros}"
        }

        binding.btnStart.setOnClickListener {
            val work = binding.editTextWork.text.toString().toIntOrNull() ?: 25
            val brk = binding.editTextBreak.text.toString().toIntOrNull() ?: 5
            val result = timerController.startSession(work, brk)
            if (result is Result.Error) {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnPause.setOnClickListener { timerController.pause() }

        binding.btnResume.setOnClickListener { timerController.resume() }

        binding.btnReset.setOnClickListener { timerController.reset() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
