package com.example.calmconnect.controller

interface NotificationController {
    fun scheduleDaily(hour: Int, minute: Int, message: String)
    fun cancelReminder()
    fun isReminderActive(): Boolean
}
