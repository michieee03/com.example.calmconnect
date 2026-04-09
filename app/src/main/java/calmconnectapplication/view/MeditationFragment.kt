package calmconnectapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import calmconnectapplication.controller.impl.StressReliefControllerImpl
import calmconnectapplication.databinding.FragmentMeditationBinding

class MeditationFragment(
    private val controller: StressReliefControllerImpl
) : Fragment() {

    private var _binding: FragmentMeditationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeditationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnStartMeditation.setOnClickListener {
            val minutes = binding.editTextDuration.text.toString().toIntOrNull()
            if (minutes == null || minutes <= 0) {
                Toast.makeText(requireContext(), "Enter a valid duration", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val session = controller.startMeditation(minutes)
            session.remainingSeconds.observe(viewLifecycleOwner) { remaining ->
                val mm = remaining / 60
                val ss = remaining % 60
                binding.tvCountdown.text = "%02d:%02d".format(mm, ss)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
