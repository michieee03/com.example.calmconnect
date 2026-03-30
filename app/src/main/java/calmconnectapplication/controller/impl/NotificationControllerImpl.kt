package com.example.calmconnect.controller.impl

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.calmconnect.controller.NotificationController
import com.example.calmconnect.util.ReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationControllerImpl(private val context: Context) : NotificationController {

    companion object {
        private const val TAG = "NotificationController"
        private const val WORK_TAG = "daily_reminder"
        private const val PREFS_NAME = "calm_connect_notification_prefs"
        private const val KEY_REMINDER_ACTIVE = "is_reminder_active"
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // -------------------------------------------------------------------------
    // Req 12.1 — schedule a recurring daily WorkManager task
    // -------------------------------------------------------------------------
    override fun scheduleDaily(hour: Int, minute: Int, message: String) {
        // Req 12.5 — check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted; reminders will not be delivered.")
                notifyUserPermissionDenied()
                return
            }
        }

        val initialDelay = calculateInitialDelay(hour, minute)

        val inputData = Data.Builder()
            .putString(ReminderWorker.KEY_MESSAGE, message)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

        // Req 12.3 — persist active state
        prefs.edit().putBoolean(KEY_REMINDER_ACTIVE, true).apply()
        Log.d(TAG, "Daily reminder scheduled for $hour:$minute with delay ${initialDelay}ms")
    }

    // -------------------------------------------------------------------------
    // Req 12.2 — cancel the scheduled task and set isReminderActive = false
    // -------------------------------------------------------------------------
    override fun cancelReminder() {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
        prefs.edit().putBoolean(KEY_REMINDER_ACTIVE, false).apply()
        Log.d(TAG, "Daily reminder cancelled")
    }

    // -------------------------------------------------------------------------
    // Req 12.3 / 12.4 — return current scheduling state from SharedPreferences
    // -------------------------------------------------------------------------
    override fun isReminderActive(): Boolean =
        prefs.getBoolean(KEY_REMINDER_ACTIVE, false)

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Calculates the milliseconds until the next occurrence of [hour]:[minute].
     * If the target time has already passed today, schedules for tomorrow.
     */
    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // If the target time is in the past (or right now), push to tomorrow
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }

    /**
     * Req 12.5 — returns true if POST_NOTIFICATIONS permission is denied on Android 13+.
     * The View should call this after scheduleDaily() to decide whether to show a rationale.
     */
    fun isPermissionDenied(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    /**
     * Req 12.5 — returns an Intent that opens the app's notification settings page,
     * allowing the user to re-enable notifications.
     */
    fun buildNotificationSettingsIntent(): Intent =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

    /**
     * Req 12.5 — inform the user that reminders won't work and provide a
     * shortcut to system notification settings.
     */
    private fun notifyUserPermissionDenied() {
        Log.i(TAG, "POST_NOTIFICATIONS permission denied — View should show rationale and settings shortcut.")
    }
}
