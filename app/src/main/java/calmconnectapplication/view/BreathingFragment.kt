package calmconnectapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import calmconnectapplication.controller.impl.StressReliefControllerImpl
import calmconnectapplication.databinding.FragmentBreathingBinding
import calmconnectapplication.model.BreathingPattern

class BreathingFragment(
    private val controller: StressReliefControllerImpl
) : Fragment() {

    private var _binding: FragmentBreathingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBreathingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnStartBreathing.setOnClickListener {
            val pattern = when (binding.radioGroupPattern.checkedRadioButtonId) {
                binding.radioRelaxing.id -> BreathingPattern.RELAXING_4_7_8
                binding.radioEnergizing.id -> BreathingPattern.ENERGIZING_2_2_4
                else -> BreathingPattern.BOX_4_4_4_4
            }
            val session = controller.startBreathingExercise(pattern)
            session.currentPhase.observe(viewLifecycleOwner) { phase ->
                binding.tvPhaseLabel.text = phase
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
