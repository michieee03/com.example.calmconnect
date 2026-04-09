package calmconnectapplication.controller

import android.net.Uri
import androidx.lifecycle.LiveData
import calmconnectapplication.db.entity.UserProfile
import calmconnectapplication.util.Result

interface ProfileController {
    fun getProfile(): LiveData<UserProfile>
    fun updateName(name: String): Result<Unit>
    fun updateProfilePicture(uri: Uri): Result<Unit>
    fun toggleDarkMode(enabled: Boolean)
    fun logout()
}
