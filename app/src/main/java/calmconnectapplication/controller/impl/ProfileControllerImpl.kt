package calmconnectapplication.controller.impl

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import calmconnectapplication.controller.ProfileController
import calmconnectapplication.db.entity.UserProfile
import calmconnectapplication.model.ProfileRepository
import calmconnectapplication.util.Result
import calmconnectapplication.util.UserSession
import kotlinx.coroutines.runBlocking

class ProfileControllerImpl(
    private val profileRepository: ProfileRepository,
    private val context: Context
) : ProfileController {

    init {
        runBlocking {
            val uid = UserSession.uid
            if (uid.isNotEmpty()) {
                val existing = profileRepository.getProfileSync()
                if (existing == null) {
                    profileRepository.insert(
                        UserProfile(
                            userId = uid,
                            name = "User",
                            profilePictureUri = null,
                            isDarkMode = false
                        )
                    )
                }
            }
        }
    }

    override fun getProfile(): LiveData<UserProfile> {
        @Suppress("UNCHECKED_CAST")
        return profileRepository.getProfile() as LiveData<UserProfile>
    }

    override fun updateName(name: String): Result<Unit> {
        if (name.isBlank()) return Result.Error("Name cannot be empty")
        val uid = UserSession.uid
        val current = runBlocking { profileRepository.getProfileSync() }
        val updated = current?.copy(name = name)
            ?: UserProfile(userId = uid, name = name, profilePictureUri = null, isDarkMode = false)
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
        val uid = UserSession.uid
        val current = runBlocking { profileRepository.getProfileSync() }
        val updated = current?.copy(profilePictureUri = uri.toString())
            ?: UserProfile(userId = uid, name = "User", profilePictureUri = uri.toString(), isDarkMode = false)
        runBlocking {
            if (current != null) profileRepository.update(updated)
            else profileRepository.insert(updated)
        }
        return Result.Success(Unit)
    }

    override fun toggleDarkMode(enabled: Boolean) {
        val uid = UserSession.uid
        val current = runBlocking { profileRepository.getProfileSync() }
        val updated = current?.copy(isDarkMode = enabled)
            ?: UserProfile(userId = uid, name = "User", profilePictureUri = null, isDarkMode = enabled)
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
