package com.example.calmconnect.controller.impl

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import com.example.calmconnect.controller.ProfileController
import com.example.calmconnect.db.entity.UserProfile
import com.example.calmconnect.model.ProfileRepository
import com.example.calmconnect.util.Result
import kotlinx.coroutines.runBlocking

class ProfileControllerImpl(
    private val profileRepository: ProfileRepository,
    private val context: Context
) : ProfileController {

    init {
        runBlocking {
            val existing = profileRepository.getProfileSync()
            if (existing == null) {
                profileRepository.insert(
                    UserProfile(
                        id = 1,
                        name = "User",
                        profilePictureUri = null,
                        isDarkMode = false
                    )
                )
            }
        }
    }

    override fun getProfile(): LiveData<UserProfile> {
        @Suppress("UNCHECKED_CAST")
        return profileRepository.getProfile() as LiveData<UserProfile>
    }

    override fun updateName(name: String): Result<Unit> {
        if (name.isBlank()) {
            return Result.Error("Name cannot be empty")
        }
        val current = runBlocking { profileRepository.getProfileSync() }
        val updated = current?.copy(name = name)
            ?: UserProfile(id = 1, name = name, profilePictureUri = null, isDarkMode = false)
        runBlocking {
            if (current != null) profileRepository.update(updated)
            else profileRepository.insert(updated)
        }
        return Result.Success(Unit)
    }

    override fun updateProfilePicture(uri: Uri): Result<Unit> {
        try {
            context.contentResolver.openInputStream(uri)?.close()
                ?: return Result.Error("URI is not readable")
        } catch (e: Exception) {
            return Result.Error("URI is not readable: ${e.message}")
        }
        val current = runBlocking { profileRepository.getProfileSync() }
        val updated = current?.copy(profilePictureUri = uri.toString())
            ?: UserProfile(id = 1, name = "User", profilePictureUri = uri.toString(), isDarkMode = false)
        runBlocking {
            if (current != null) profileRepository.update(updated)
            else profileRepository.insert(updated)
        }
        return Result.Success(Unit)
    }

    override fun toggleDarkMode(enabled: Boolean) {
        val current = runBlocking { profileRepository.getProfileSync() }
        val updated = current?.copy(isDarkMode = enabled)
            ?: UserProfile(id = 1, name = "User", profilePictureUri = null, isDarkMode = enabled)
        runBlocking {
            if (current != null) profileRepository.update(updated)
            else profileRepository.insert(updated)
        }
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    override fun logout() {
        // Navigation is handled by the UI layer; log the logout event here.
        Log.d("ProfileControllerImpl", "logout")
    }
}
