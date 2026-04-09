package calmconnectapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import calmconnectapplication.R
import calmconnectapplication.databinding.FragmentToolsBinding

class ToolsFragment : Fragment() {

    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cards = listOf(
            binding.cardSoundscapes to R.id.action_tools_to_soundscapes,
            binding.cardStressRelief to R.id.action_tools_to_stress_relief,
            binding.cardPomodoro to R.id.action_tools_to_pomodoro,
            binding.cardGames to R.id.action_tools_to_games,
            binding.cardQuotes to R.id.action_tools_to_quotes,
            binding.cardNearbyPlaces to R.id.action_tools_to_nearby_places
        )

        cards.forEach { (card, actionId) ->
            card.setOnClickListener {
                it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
                    findNavController().navigate(actionId)
                }.start()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
