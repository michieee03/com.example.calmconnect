package com.example.calmconnect.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.calmconnect.controller.impl.MoodControllerImpl
import com.example.calmconnect.databinding.FragmentMoodHistoryBinding
import com.example.calmconnect.databinding.ItemMoodEntryBinding
import com.example.calmconnect.db.entity.MoodEntry
import com.example.calmconnect.model.MoodRepository
import com.example.calmconnect.db.AppDatabase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoodHistoryFragment : Fragment() {

    private var _binding: FragmentMoodHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var moodController: MoodControllerImpl
    private lateinit var adapter: MoodEntryAdapter

    private val emotionValues = mapOf(
        "Happy" to 5f,
        "Excited" to 5f,
        "Grateful" to 4f,
        "Calm" to 4f,
        "Neutral" to 3f,
        "Tired" to 2f,
        "Sad" to 2f,
        "Anxious" to 1f,
        "Stressed" to 1f,
        "Angry" to 1f
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())
        moodController = MoodControllerImpl(MoodRepository(db.moodDao()))

        setupRecyclerView()
        setupChart()
        observeMoodHistory()
    }

    private fun setupRecyclerView() {
        adapter = MoodEntryAdapter()
        binding.rvMoodEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMoodEntries.adapter = adapter

        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val entry = adapter.getItem(position)
                moodController.deleteMood(entry.id)
                Snackbar.make(binding.root, "Entry deleted", Snackbar.LENGTH_SHORT).show()
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvMoodEntries)
    }

    private fun setupChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }

            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 6f
                granularity = 1f
            }

            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }

    private fun observeMoodHistory() {
        moodController.getMoodHistory().observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries)
            updateChart(entries)
        }
    }

    private fun updateChart(entries: List<MoodEntry>) {
        if (entries.isEmpty()) {
            binding.lineChart.clear()
            binding.lineChart.invalidate()
            return
        }

        val chartEntries = entries.mapIndexed { index, moodEntry ->
            val value = emotionValues[moodEntry.emotion] ?: 3f
            Entry(index.toFloat(), value)
        }

        val labels = entries.map { it.date }

        val dataSet = LineDataSet(chartEntries, "Mood").apply {
            color = Color.parseColor("#6200EE")
            setCircleColor(Color.parseColor("#6200EE"))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.lineChart.xAxis.labelCount = minOf(labels.size, 5)

        binding.lineChart.data = LineData(dataSet)
        binding.lineChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Adapter ---

    inner class MoodEntryAdapter : RecyclerView.Adapter<MoodEntryAdapter.ViewHolder>() {

        private val items = mutableListOf<MoodEntry>()
        private val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

        fun submitList(newItems: List<MoodEntry>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        fun getItem(position: Int): MoodEntry = items[position]

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemMoodEntryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(private val binding: ItemMoodEntryBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(entry: MoodEntry) {
                binding.tvEmotion.text = entry.emotion
                binding.tvDate.text = dateFormatter.format(Date(entry.timestamp))
                if (!entry.note.isNullOrBlank()) {
                    binding.tvNote.visibility = View.VISIBLE
                    binding.tvNote.text = entry.note
                } else {
                    binding.tvNote.visibility = View.GONE
                }
            }
        }
    }
}
