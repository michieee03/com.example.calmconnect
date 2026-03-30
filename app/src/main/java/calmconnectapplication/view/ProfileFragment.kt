package com.example.calmconnect.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.calmconnect.controller.impl.NotificationControllerImpl
import com.example.calmconnect.controller.impl.ProfileControllerImpl
import com.example.calmconnect.databinding.FragmentProfileBinding
import com.example.calmconnect.db.AppDatabase
import com.example.calmconnect.model.ProfileRepository
import com.example.calmconnect.util.Result

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileController: ProfileControllerImpl
    private lateinit var notificationController: NotificationControllerImpl

    // Suppress programmatic switch changes from triggering the listener
    private var suppressDarkModeListener = false
    private var suppressReminderListener = false

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri ?: return@registerForActivityResult
            when (val result = profileController.updateProfilePicture(uri)) {
                is Result.Success -> loadProfilePicture(uri)
                is Result.Error -> toast(result.message)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        setupListeners()
    }

    private fun setupTimePickers() {
        binding.pickerHour.minValue = 0
        binding.pickerHour.maxValue = 23
        binding.pickerMinute.minValue = 0
        binding.pickerMinute.maxValue = 59
    }

    private fun setupObserver() {
        profileController.getProfile().observe(viewLifecycleOwner) { profile ->
            profile ?: return@observe

            // Name
            if (binding.etName.text.isNullOrEmpty()) {
                binding.etName.setText(profile.name)
            }

            // Profile picture
            profile.profilePictureUri?.let { uriStr ->
                loadProfilePicture(Uri.parse(uriStr))
            }

            // Dark mode toggle — suppress listener to avoid re-triggering
            suppressDarkModeListener = true
            binding.switchDarkMode.isChecked = profile.isDarkMode
            suppressDarkModeListener = false

            // Reminder toggle and time
            suppressReminderListener = true
            binding.switchReminder.isChecked = profile.reminderEnabled
            binding.layoutReminderTime.visibility =
                if (profile.reminderEnabled) View.VISIBLE else View.GONE
            suppressReminderListener = false

            binding.pickerHour.value = profile.reminderHour
            binding.pickerMinute.value = profile.reminderMinute
        }
    }

    private fun setupListeners() {
        // Save name
        binding.btnSaveName.setOnClickListener {
            val name = binding.etName.text?.toString().orEmpty()
            when (val result = profileController.updateName(name)) {
                is Result.Success -> toast("Name saved")
                is Result.Error -> {
                    binding.tilName.error = result.message
                    return@setOnClickListener
                }
            }
            binding.tilName.error = null
        }

        // Change photo
        binding.btnChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Dark mode toggle
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (suppressDarkModeListener) return@setOnCheckedChangeListener
            profileController.toggleDarkMode(isChecked)
        }

        // Reminder toggle
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            if (suppressReminderListener) return@setOnCheckedChangeListener
            binding.layoutReminderTime.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                notificationController.cancelReminder()
            }
        }

        // Set reminder
        binding.btnSetReminder.setOnClickListener {
            if (binding.switchReminder.isChecked) {
                val hour = binding.pickerHour.value
                val minute = binding.pickerMinute.value
                notificationController.scheduleDaily(hour, minute, "Time to check in with Calm Connect!")

                // Req 12.5 — if permission was denied, inform the user and offer settings shortcut
                if (notificationController.isPermissionDenied()) {
                    com.google.android.material.snackbar.Snackbar.make(
                        binding.root,
                        "Notification permission denied. Reminders will not be delivered.",
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                    ).setAction("Settings") {
                        startActivity(notificationController.buildNotificationSettingsIntent())
                    }.show()
                } else {
                    toast("Reminder set for %02d:%02d".format(hour, minute))
                }
            } else {
                notificationController.cancelReminder()
                toast("Reminder cancelled")
            }
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            profileController.logout()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadProfilePicture(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(binding.imgProfilePicture)
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
