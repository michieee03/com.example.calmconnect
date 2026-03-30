package com.example.calmconnect.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.example.calmconnect.controller.impl.StressReliefControllerImpl
import com.example.calmconnect.databinding.FragmentStressReliefBinding
import com.example.calmconnect.db.AppDatabase
import com.example.calmconnect.model.JournalRepository

class StressReliefFragment : Fragment() {

    private var _binding: FragmentStressReliefBinding? = null
    private val binding get() = _binding!!

    private lateinit var controller: StressReliefControllerImpl

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStressReliefBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())
        controller = StressReliefControllerImpl(JournalRepository(db.journalDao()))

        binding.viewPager.adapter = StressReliefPagerAdapter()

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Breathing"
                1 -> "Meditation"
                else -> "Journal"
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class StressReliefPagerAdapter : FragmentStateAdapter(this) {
        override fun getItemCount() = 3
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> BreathingFragment(controller)
            1 -> MeditationFragment(controller)
            else -> JournalFragment(controller)
        }
    }
}
