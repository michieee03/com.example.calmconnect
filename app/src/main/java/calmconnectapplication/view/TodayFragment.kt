package calmconnectapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import calmconnectapplication.controller.impl.RoutineControllerImpl
import calmconnectapplication.databinding.FragmentTodayBinding
import calmconnectapplication.databinding.ItemRoutineStepBinding
import calmconnectapplication.db.AppDatabase
import calmconnectapplication.db.entity.RoutineStep
import calmconnectapplication.model.RoutineRepository

class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!

    private lateinit var routineController: RoutineControllerImpl
    private lateinit var adapter: RoutineStepAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
        }

        binding.rvRoutineSteps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRoutineSteps.adapter = adapter

        routineController.getTodayRoutine().observe(viewLifecycleOwner) { steps ->
            adapter.submitList(steps)
            val percent = (routineController.getCompletionPercentage() * 100).toInt()
            binding.progressBar.progress = percent
            binding.circularProgress.progress = percent
            binding.tvProgressLabel.text = "$percent%"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Emoji icons mapped to each step id
private val STEP_ICONS = mapOf(
    1 to "📝",  // Morning Meditation → journal icon
    2 to "🙏",  // Gratitude Journal
    3 to "🌿",  // Gentle Stretch
    4 to "🥗",  // Healthy Breakfast
    5 to "🚶"   // Daily Walk
)

class RoutineStepAdapter(
    private val onStepChecked: (RoutineStep) -> Unit
) : ListAdapter<RoutineStep, RoutineStepAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(private val b: ItemRoutineStepBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(step: RoutineStep) {
            b.tvStepTitle.text = step.title
            b.tvStepDescription.text = "${step.description}. ${step.durationMinutes} mins"
            b.tvStepIcon.text = STEP_ICONS[step.id] ?: "✨"

            if (step.isCompleted) {
                b.viewCheckEmpty.visibility = View.GONE
                b.tvCheckDone.visibility = View.VISIBLE
                b.flCheck.isClickable = false
                // Strike-through title
                b.tvStepTitle.paintFlags =
                    b.tvStepTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                b.tvStepTitle.alpha = 0.5f
                b.tvStepDescription.alpha = 0.5f
            } else {
                b.viewCheckEmpty.visibility = View.VISIBLE
                b.tvCheckDone.visibility = View.GONE
                b.flCheck.isClickable = true
                b.tvStepTitle.paintFlags =
                    b.tvStepTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                b.tvStepTitle.alpha = 1f
                b.tvStepDescription.alpha = 1f

                b.flCheck.setOnClickListener {
                    onStepChecked(step)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemRoutineStepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RoutineStep>() {
            override fun areItemsTheSame(old: RoutineStep, new: RoutineStep) = old.id == new.id
            override fun areContentsTheSame(old: RoutineStep, new: RoutineStep) = old == new
        }
    }
}
