package calmconnectapplication.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import calmconnectapplication.controller.impl.MoodControllerImpl
import calmconnectapplication.databinding.FragmentMoodHistoryBinding
import calmconnectapplication.databinding.ItemMoodEntryBinding
import calmconnectapplication.db.entity.MoodEntry
import calmconnectapplication.model.MoodRepository
import calmconnectapplication.db.AppDatabase
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
        "Happy"    to 5f, "Excited"  to 5f,
        "Grateful" to 4f, "Calm"     to 4f, "Relax" to 4f,
        "Neutral"  to 3f,
        "Tired"    to 2f, "Sad"      to 2f,
        "Anxious"  to 1f, "Stressed" to 1f, "Angry" to 1f
    )

    private val emotionEmojis = mapOf(
        "Happy"    to "😊", "Excited"  to "🎉",
        "Grateful" to "🙏", "Calm"     to "😌", "Relax" to "😇",
        "Neutral"  to "😐", "Tired"    to "😴",
        "Sad"      to "😔", "Anxious"  to "😰",
        "Stressed" to "😓", "Angry"    to "😠"
    )

    // Accent bar color per mood level
    private val emotionColors = mapOf(
        "Happy"    to "#4CAF93", "Excited"  to "#4CAF93",
        "Grateful" to "#66BB6A", "Calm"     to "#66BB6A", "Relax" to "#66BB6A",
        "Neutral"  to "#FFA726",
        "Tired"    to "#EF5350", "Sad"      to "#EF5350",
        "Anxious"  to "#B71C1C", "Stressed" to "#B71C1C", "Angry" to "#B71C1C"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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

        binding.btnClose.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = MoodEntryAdapter()
        binding.rvMoodEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMoodEntries.adapter = adapter

        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val entry = adapter.getItem(viewHolder.adapterPosition)
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
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            setBackgroundColor(Color.TRANSPARENT)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.parseColor("#7A748A")
                textSize = 9f
            }
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 6f
                granularity = 1f
                textColor = Color.parseColor("#7A748A")
                setDrawGridLines(true)
                gridColor = Color.parseColor("#22000000")
            }
            axisRight.isEnabled = false
            legend.apply {
                isEnabled = false
            }
        }
    }

    private fun observeMoodHistory() {
        moodController.getMoodHistory().observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries, emotionEmojis, emotionColors)
            updateChart(entries)
            updateStats(entries)
        }
    }

    private fun updateStats(entries: List<MoodEntry>) {
        binding.tvTotalValue.text = "${entries.size} logs"

        if (entries.isEmpty()) {
            binding.tvTopMoodValue.text = "—"
            binding.tvTopMoodEmoji.text = "😊"
            binding.tvStreakValue.text = "0 days"
            return
        }

        // Top mood
        val topMood = entries.groupingBy { it.emotion }.eachCount().maxByOrNull { it.value }?.key ?: "—"
        binding.tvTopMoodValue.text = topMood
        binding.tvTopMoodEmoji.text = emotionEmojis[topMood] ?: "😊"

        // Streak: count consecutive distinct days up to today
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val distinctDays = entries.map { it.date }.distinct().sortedDescending()
        var streak = 0
        var expected = dayFormat.format(Date())
        for (day in distinctDays) {
            if (day == expected) {
                streak++
                val cal = java.util.Calendar.getInstance()
                cal.time = dayFormat.parse(expected)!!
                cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                expected = dayFormat.format(cal.time)
            } else break
        }
        binding.tvStreakValue.text = "$streak day${if (streak != 1) "s" else ""}"
    }

    private fun updateChart(entries: List<MoodEntry>) {
        if (entries.isEmpty()) {
            binding.lineChart.clear()
            binding.lineChart.invalidate()
            return
        }

        val recent = entries.takeLast(14) // show last 14 entries max
        val chartEntries = recent.mapIndexed { index, moodEntry ->
            Entry(index.toFloat(), emotionValues[moodEntry.emotion] ?: 3f)
        }
        val labels = recent.map {
            SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(it.timestamp))
        }

        val dataSet = LineDataSet(chartEntries, "Mood").apply {
            color = Color.parseColor("#4CAF93")
            setCircleColor(Color.parseColor("#4CAF93"))
            circleHoleColor = Color.WHITE
            lineWidth = 2.5f
            circleRadius = 5f
            circleHoleRadius = 2.5f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#4CAF93")
            fillAlpha = 40
        }

        binding.lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.lineChart.xAxis.labelCount = minOf(labels.size, 7)
        binding.lineChart.data = LineData(dataSet)
        binding.lineChart.animateX(600)
        binding.lineChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Adapter ---

    inner class MoodEntryAdapter : RecyclerView.Adapter<MoodEntryAdapter.ViewHolder>() {

        private val items = mutableListOf<MoodEntry>()
        private var emojiMap: Map<String, String> = emptyMap()
        private var colorMap: Map<String, String> = emptyMap()
        private val dateFormatter = SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())

        fun submitList(
            newItems: List<MoodEntry>,
            emojis: Map<String, String>,
            colors: Map<String, String>
        ) {
            items.clear()
            items.addAll(newItems)
            emojiMap = emojis
            colorMap = colors
            notifyDataSetChanged()
        }

        fun getItem(position: Int): MoodEntry = items[position]

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemMoodEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
        override fun getItemCount() = items.size

        inner class ViewHolder(private val b: ItemMoodEntryBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind(entry: MoodEntry) {
                b.tvEmotion.text = entry.emotion
                b.tvEmoji.text = emojiMap[entry.emotion] ?: "😐"
                b.tvDate.text = dateFormatter.format(Date(entry.timestamp))

                // Accent bar color based on mood
                val hexColor = colorMap[entry.emotion] ?: "#4CAF93"
                b.viewAccentBar.setBackgroundColor(Color.parseColor(hexColor))

                if (!entry.note.isNullOrBlank()) {
                    b.tvNote.visibility = View.VISIBLE
                    b.tvNote.text = entry.note
                } else {
                    b.tvNote.visibility = View.GONE
                }
            }
        }
    }
}
