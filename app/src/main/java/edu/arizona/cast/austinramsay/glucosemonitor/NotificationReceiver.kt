package edu.arizona.cast.austinramsay.glucosemonitor

import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.appcompat.app.AppCompatActivity

private const val TAG = "NotificationReceiver"

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Received result: $resultCode" + ", intent action: ${intent.action}")

        if (resultCode != Activity.RESULT_OK) {
            // A foreground activity cancelled the broadcast
            return
        }

        val requestCode = intent.getIntExtra(ReminderWorker.REQUEST_CODE, 0)
        val notification: Notification? = intent.getParcelableExtra(ReminderWorker.NOTIFICATION)
        val notificationManager = NotificationManagerCompat.from(context)

        if (notification != null) {
            Log.i(TAG, "Received notification: $notification")
            notificationManager.notify(requestCode, notification)
        } else {
            Log.i(TAG, "Received null notification")
        }
    }
}