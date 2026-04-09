package calmconnectapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import calmconnectapplication.controller.impl.StudyTimerControllerImpl
import calmconnectapplication.databinding.FragmentPomodoroBinding
import calmconnectapplication.util.Result

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
            updateDots(state.completedPomodoros)
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

        binding.btnClose.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateDots(completed: Int) {
        binding.llDots.removeAllViews()
        val total = 4
        for (i in 0 until total) {
            val dot = android.widget.TextView(requireContext()).apply {
                text = if (i < completed) "🍅" else "○"
                textSize = if (i < completed) 16f else 18f
                setPadding(6, 0, 6, 0)
            }
            binding.llDots.addView(dot)
        }
    }
}
