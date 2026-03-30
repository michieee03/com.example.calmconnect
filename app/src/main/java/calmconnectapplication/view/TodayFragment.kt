package com.example.calmconnect.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.calmconnect.controller.impl.RoutineControllerImpl
import com.example.calmconnect.databinding.FragmentTodayBinding
import com.example.calmconnect.databinding.ItemRoutineStepBinding
import com.example.calmconnect.db.AppDatabase
import com.example.calmconnect.db.entity.RoutineStep
import com.example.calmconnect.model.RoutineRepository

class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!

    private lateinit var routineController: RoutineControllerImpl
    private lateinit var adapter: RoutineStepAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())
        routineController = RoutineControllerImpl(RoutineRepository(db.routineDao()))

        adapter = RoutineStepAdapter { step ->
            routineController.markStepComplete(step.id)
            updateProgress()
        }

        binding.rvRoutineSteps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRoutineSteps.adapter = adapter

        routineController.getTodayRoutine().observe(viewLifecycleOwner) { steps ->
            adapter.submitList(steps)
            updateProgress()
        }
    }

    private fun updateProgress() {
        val percentage = routineController.getCompletionPercentage()
        val percent = (percentage * 100).toInt()
        binding.progressBar.progress = percent
        binding.tvProgressLabel.text = "$percent% complete"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class RoutineStepAdapter(
    private val onStepChecked: (RoutineStep) -> Unit
) : ListAdapter<RoutineStep, RoutineStepAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(private val binding: ItemRoutineStepBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(step: RoutineStep) {
            binding.tvStepTitle.text = step.title
            binding.tvStepDescription.text = step.description
            binding.tvStepDuration.text = "${step.durationMinutes} min"

            // Set checked state without triggering listener
            binding.cbStepComplete.setOnCheckedChangeListener(null)
            binding.cbStepComplete.isChecked = step.isCompleted
            binding.cbStepComplete.isEnabled = !step.isCompleted

            binding.cbStepComplete.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && !step.isCompleted) {
                    onStepChecked(step)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRoutineStepBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RoutineStep>() {
            override fun areItemsTheSame(old: RoutineStep, new: RoutineStep) = old.id == new.id
            override fun areContentsTheSame(old: RoutineStep, new: RoutineStep) = old == new
        }
    }
}
