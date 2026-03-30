package com.example.calmconnect.model

import androidx.lifecycle.LiveData
import com.example.calmconnect.db.dao.ProfileDao
import com.example.calmconnect.db.entity.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(private val profileDao: ProfileDao) {

    suspend fun insert(profile: UserProfile) = withContext(Dispatchers.IO) {
        profileDao.insert(profile)
    }

    suspend fun update(profile: UserProfile) = withContext(Dispatchers.IO) {
        profileDao.update(profile)
    }

    fun getProfile(): LiveData<UserProfile?> = profileDao.getProfile()

    suspend fun getProfileSync(): UserProfile? = withContext(Dispatchers.IO) {
        profileDao.getProfileSync()
    }
}
