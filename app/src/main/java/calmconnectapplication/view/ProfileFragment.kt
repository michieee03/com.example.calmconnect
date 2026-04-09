package calmconnectapplication.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import calmconnectapplication.R
import calmconnectapplication.controller.impl.NotificationControllerImpl
import calmconnectapplication.controller.impl.ProfileControllerImpl
import calmconnectapplication.databinding.FragmentProfileBinding
import calmconnectapplication.db.AppDatabase
import calmconnectapplication.model.MoodRepository
import calmconnectapplication.model.ProfileRepository
import calmconnectapplication.util.Result
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileController: ProfileControllerImpl
    private lateinit var notificationController: NotificationControllerImpl

    private var suppressDarkModeListener = false
    private var suppressReminderListener = false

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri ?: return@registerForActivityResult
            // Take persistent read permission so the URI survives app restarts
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { /* not all URIs support this — safe to ignore */ }
            when (val result = profileController.updateProfilePicture(uri)) {
                is Result.Success -> loadProfilePicture(uri)
                is Result.Error -> toast(result.message)
            }
        }

    // Runtime permission launcher for Android 13+
    private val requestMediaPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) pickImageLauncher.launch("image/*")
            else toast("Permission denied — cannot access photos")
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())
        profileController = ProfileControllerImpl(ProfileRepository(db.profileDao()), requireContext())
        notificationController = NotificationControllerImpl(requireContext())

        setupTimePickers()
        setupObserver()
        setupStats(db)
        setupListeners()
    }

    private fun setupTimePickers() {
        binding.pickerHour.minValue = 0
        binding.pickerHour.maxValue = 23
        binding.pickerMinute.minValue = 0
        binding.pickerMinute.maxValue = 59
    }

    private fun setupStats(db: AppDatabase) {
        val moodRepo = MoodRepository(db.moodDao())
        moodRepo.getMoodHistory().observe(viewLifecycleOwner) { entries ->
            val sessions = entries.size
            binding.tvSessionsDone.text = sessions.toString()
            binding.tvMinutesMindful.text = (sessions * 5).toString()
            // Streak
            val dates = entries.map { it.date }.toSet()
            var streak = 0
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val cal = java.util.Calendar.getInstance()
            while (true) {
                val d = sdf.format(cal.time)
                if (dates.contains(d)) { streak++; cal.add(java.util.Calendar.DAY_OF_YEAR, -1) }
                else break
            }
            binding.tvDaysStreak.text = streak.toString()

            // Zen garden preview
            val unlockedCount = when {
                sessions >= 30 -> 16
                sessions >= 14 -> 12
                sessions >= 7  -> 9
                sessions >= 3  -> 6
                sessions >= 1  -> 3
                else           -> 0
            }
            binding.tvGardenPreview.text = "$unlockedCount / 16 plants 🌿"

            // Mini mood chart — last 7 entries
            setupMiniChart(entries.takeLast(7))
        }

        binding.cardZenGarden.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_zen_garden)
        }
    }

    private val emotionValues = mapOf(
        "Happy" to 5f, "Excited" to 5f, "Relax" to 4f,
        "Grateful" to 4f, "Calm" to 4f, "Neutral" to 3f,
        "Tired" to 2f, "Sad" to 2f,
        "Anxious" to 1f, "Stressed" to 1f, "Angry" to 1f
    )

    private fun setupMiniChart(entries: List<calmconnectapplication.db.entity.MoodEntry>) {
        val chart = binding.miniMoodChart
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setTouchEnabled(false)
        chart.setDrawGridBackground(false)
        chart.setBackgroundColor(Color.TRANSPARENT)
        chart.xAxis.isEnabled = false
        chart.axisLeft.isEnabled = false
        chart.axisRight.isEnabled = false

        if (entries.isEmpty()) {
            chart.clear()
            chart.invalidate()
            return
        }

        val chartEntries = entries.mapIndexed { i, e ->
            Entry(i.toFloat(), emotionValues[e.emotion] ?: 3f)
        }
        val dataSet = LineDataSet(chartEntries, "").apply {
            color = Color.parseColor("#4CAF93")
            setCircleColor(Color.parseColor("#4CAF93"))
            lineWidth = 2f
            circleRadius = 3f
            circleHoleRadius = 1.5f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#4CAF93")
            fillAlpha = 40
        }
        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    private fun setupObserver() {
        profileController.getProfile().observe(viewLifecycleOwner) { profile ->
            profile ?: return@observe

            binding.tvProfileName.text = profile.name.ifBlank { "Your Name" }
            // Always sync the edit field with the saved name
            binding.etName.setText(profile.name)

            profile.profilePictureUri?.let { loadProfilePicture(Uri.parse(it)) }

            suppressDarkModeListener = true
            binding.switchDarkMode.isChecked = profile.isDarkMode
            suppressDarkModeListener = false

            suppressReminderListener = true
            binding.switchReminder.isChecked = profile.reminderEnabled
            binding.layoutReminderTime.visibility = if (profile.reminderEnabled) View.VISIBLE else View.GONE
            suppressReminderListener = false

            binding.pickerHour.value = profile.reminderHour
            binding.pickerMinute.value = profile.reminderMinute
        }
    }

    private fun setupListeners() {
        binding.btnSaveName.setOnClickListener {
            val name = binding.etName.text?.toString().orEmpty()
            when (val result = profileController.updateName(name)) {
                is Result.Success -> {
                    binding.tvProfileName.text = name
                    binding.tilName.error = null
                    toast("Name saved ✓")
                }
                is Result.Error -> binding.tilName.error = result.message
            }
        }

        binding.btnChangePhoto.setOnClickListener { openPhotoPicker() }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (suppressDarkModeListener) return@setOnCheckedChangeListener
            profileController.toggleDarkMode(isChecked)
        }

        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            if (suppressReminderListener) return@setOnCheckedChangeListener
            binding.layoutReminderTime.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) notificationController.cancelReminder()
        }

        binding.btnSetReminder.setOnClickListener {
            if (binding.switchReminder.isChecked) {
                val hour = binding.pickerHour.value
                val minute = binding.pickerMinute.value
                notificationController.scheduleDaily(hour, minute, "Time to check in with Calm Connect!")
                toast("Reminder set for %02d:%02d".format(hour, minute))
            } else {
                notificationController.cancelReminder()
                toast("Reminder cancelled")
            }
        }

        binding.btnLogout.setOnClickListener {
            // Sign out from Firebase
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            // Clear the "remember me" flag so SplashActivity doesn't auto-login
            requireContext()
                .getSharedPreferences("calm_connect_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .putBoolean("remember_me", false)
                .apply()
            // Navigate to LoginActivity, clearing the entire back stack
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

        binding.btnDeleteAccount.setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("This will permanently delete your account and all your data. This cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    deleteAccount()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun openPhotoPicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                pickImageLauncher.launch("image/*")
            }
            else -> requestMediaPermission.launch(permission)
        }
    }

    private fun loadProfilePicture(uri: Uri) {
        Glide.with(this).load(uri).circleCrop().into(binding.imgProfilePicture)
    }

    private fun deleteAccount() {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user == null) {
            toast("No account found")
            return
        }
        user.delete()
            .addOnSuccessListener {
                // Clear local Room data on IO thread
                android.os.AsyncTask.execute {
                    AppDatabase.getInstance(requireContext()).clearAllTables()
                }
                // Clear prefs
                requireContext()
                    .getSharedPreferences("calm_connect_prefs", android.content.Context.MODE_PRIVATE)
                    .edit().clear().apply()
                toast("Account deleted")
                startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            .addOnFailureListener { e ->
                if (e is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                    toast("Please log out and log back in, then try again")
                } else {
                    toast("Failed to delete account: ${e.message}")
                }
            }
    }

    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
