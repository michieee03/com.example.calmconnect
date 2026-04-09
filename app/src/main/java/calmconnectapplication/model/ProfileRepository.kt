package calmconnectapplication.model

import androidx.lifecycle.LiveData
import calmconnectapplication.db.dao.ProfileDao
import calmconnectapplication.db.entity.UserProfile
import calmconnectapplication.util.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(private val profileDao: ProfileDao) {

    suspend fun insert(profile: UserProfile) = withContext(Dispatchers.IO) {
        profileDao.insert(profile)
    }

    suspend fun update(profile: UserProfile) = withContext(Dispatchers.IO) {
        profileDao.update(profile)
    }

    fun getProfile(): LiveData<UserProfile?> =
        profileDao.getProfile(UserSession.uid)

    suspend fun getProfileSync(): UserProfile? = withContext(Dispatchers.IO) {
        profileDao.getProfileSync(UserSession.uid)
    }
}
