package com.example.calmconnect

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.calmconnect.db.AppDatabase
import com.example.calmconnect.model.ProfileRepository
import kotlinx.coroutines.*

class CalmConnectApp : Application() {

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val profileRepo = ProfileRepository(db.profileDao())

                val profile = profileRepo.getProfileSync()

                val mode = when {
                    profile == null -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    profile.isDarkMode -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_NO
                }

                withContext(Dispatchers.Main) {
                    AppCompatDelegate.setDefaultNightMode(mode)
                }

            } catch (e: Exception) {
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    )
                }
            }
        }
    }
}