package com.example.calmconnect.controller

import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.calmconnect.db.entity.UserProfile
import com.example.calmconnect.util.Result

interface ProfileController {
    fun getProfile(): LiveData<UserProfile>
    fun updateName(name: String): Result<Unit>
    fun updateProfilePicture(uri: Uri): Result<Unit>
    fun toggleDarkMode(enabled: Boolean)
    fun logout()
}
