package calmconnectapplication.view

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import calmconnectapplication.R
import calmconnectapplication.controller.impl.MoodControllerImpl
import calmconnectapplication.controller.impl.QuoteControllerImpl
import calmconnectapplication.databinding.FragmentHomeBinding
import calmconnectapplication.databinding.ItemEmotionBinding
import calmconnectapplication.db.AppDatabase
import calmconnectapplication.model.MoodRepository
import calmconnectapplication.model.ProfileRepository
import calmconnectapplication.model.QuoteRepository
import calmconnectapplication.util.Result
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Emotion(val emoji: String, val label: String)

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var moodController: MoodControllerImpl
    private lateinit var quoteController: QuoteControllerImpl

    private var selectedEmotion: String? = null

    private val emotions = listOf(
        Emotion("😠", "Angry"),
        Emotion("😌", "Calm"),
        Emotion("😊", "Happy"),
        Emotion("😇", "Relax"),
        Emotion("😔", "Sad"),
        Emotion("😰", "Anxious"),
        Emotion("🎉", "Excited"),
        Emotion("😴", "Tired"),
        Emotion("😓", "Stressed"),
        Emotion("🙏", "Grateful"),
        Emotion("😐", "Neutral")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())
        moodController = MoodControllerImpl(MoodRepository(db.moodDao()))
        quoteController = QuoteControllerImpl(QuoteRepository(db.quoteDao()))

        setupHeader()
        setupEmotionRecyclerView()
        setupButtons()
        setupInfoButton()
        displayDailyQuote()
        setupHealthCards()
    }

    private fun setupHeader() {
        val dateStr = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date())
        binding.tvDate.text = "📅 $dateStr"

        val db = AppDatabase.getInstance(requireContext())
        val profileRepo = ProfileRepository(db.profileDao())

        // Load profile photo and name from Room
        profileRepo.getProfile().observe(viewLifecycleOwner) { profile ->
            val name = profile?.name?.trim()
            if (!name.isNullOrEmpty()) {
                binding.tvGreeting.text = "Hello, $name! 👋"
            }
            val uri = profile?.profilePictureUri
            if (!uri.isNullOrEmpty()) {
                try {
                    binding.ivAvatar.setImageURI(android.net.Uri.parse(uri))
                } catch (e: Exception) {
                    binding.ivAvatar.setImageResource(R.drawable.calmconnectlogo)
                }
            } else {
                binding.ivAvatar.setImageResource(R.drawable.calmconnectlogo)
            }
        }
    }

    private fun setupInfoButton() {
        binding.ivBell.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("About Calm Connect")
                .setMessage(
                    "Calm Connect helps you track your mood, find peaceful places nearby, " +
                    "and access tools for relaxation and stress relief.\n\n" +
                    "Select an emotion daily and tap Save Mood to log how you feel."
                )
                .setPositiveButton("Got it", null)
                .show()
        }
    }

    private fun setupEmotionRecyclerView() {
        val adapter = EmotionAdapter(emotions) { emotion ->
            selectedEmotion = emotion.label
            binding.tvSelectedEmotion.text = "You're feeling ${emotion.label} ${emotion.emoji}"
        }
        binding.rvEmotions.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvEmotions.adapter = adapter
        // Scroll to center (Happy) on start
        binding.rvEmotions.scrollToPosition(2)
    }

    private fun setupButtons() {
        binding.btnSaveMood.setOnClickListener {
            val emotion = selectedEmotion
            if (emotion == null) {
                Snackbar.make(binding.root, "Please select an emotion first", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            when (val result = moodController.saveMood(emotion, null, System.currentTimeMillis())) {
                is Result.Success -> {
                    Snackbar.make(binding.root, "Mood saved! 🌿", Snackbar.LENGTH_SHORT).show()
                    selectedEmotion = null
                    binding.tvSelectedEmotion.text = ""
                    // Sync to cloud
                    androidx.lifecycle.lifecycleScope.launch {
                        calmconnectapplication.util.FirestoreSyncService.syncToCloud(
                            calmconnectapplication.db.AppDatabase.getInstance(requireContext())
                        )
                    }
                }
                is Result.Error -> Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
            }
        }

        binding.btnViewHistory.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_mood_history)
        }
    }

    private fun displayDailyQuote() {
        val quote = quoteController.getDailyQuote()
        binding.tvQuoteText.text = "\u201C${quote.text}\u201D"
        binding.tvQuoteAuthor.text = "— ${quote.author}"
    }

    private fun setupHealthCards() {
        binding.cardMyHeart.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_mood_history)
        }
        binding.cardStressLevel.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_stress_tracker)
        }
        binding.cardBreathing.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_stress_relief)
        }
        binding.cardSleep.setOnClickListener {
            Toast.makeText(requireContext(), "Sleep tips coming soon 🌙", Toast.LENGTH_SHORT).show()
        }
        binding.cardSounds.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_soundscapes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class EmotionAdapter(
    private val items: List<Emotion>,
    private val onSelected: (Emotion) -> Unit
) : RecyclerView.Adapter<EmotionAdapter.VH>() {

    private var selectedPos = 2 // default Happy

    inner class VH(val binding: ItemEmotionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemEmotionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val emotion = items[position]
        holder.binding.tvEmoji.text = emotion.emoji
        holder.binding.tvEmotionLabel.text = emotion.label

        val isSelected = position == selectedPos
        // Selected: transparent fill + green stroke ring (emoji stays fully visible)
        // Unselected: light teal fill, no stroke
        holder.binding.tvEmoji.background = holder.itemView.context.getDrawable(
            if (isSelected) R.drawable.bg_emotion_selected
            else R.drawable.bg_emotion_item
        )
        // No scale on the emoji itself — hover is handled on the whole item
        holder.binding.tvEmoji.scaleX = 1.0f
        holder.binding.tvEmoji.scaleY = 1.0f

        holder.itemView.setOnClickListener {
            val prev = selectedPos
            selectedPos = holder.adapterPosition
            notifyItemChanged(prev)
            notifyItemChanged(selectedPos)
            onSelected(emotion)
        }

        // Hover: scale the whole item up on press, back on release
        holder.itemView.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(1.15f).scaleY(1.15f).setDuration(100).start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    v.performClick()
                }
            }
            true
        }
    }

    override fun getItemCount() = items.size
}
