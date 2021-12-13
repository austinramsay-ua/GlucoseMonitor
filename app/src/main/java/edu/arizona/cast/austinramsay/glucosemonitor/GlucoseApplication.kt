package edu.arizona.cast.austinramsay.glucosemonitor

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

const val NOTIFICATION_CHANNEL_ID = "reminder_channel"

class GlucoseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        GlucoseRepository.initialize(this)

        val name = getString(R.string.notification_channel_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel =
            NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
        val notificationManager: NotificationManager =
            getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}