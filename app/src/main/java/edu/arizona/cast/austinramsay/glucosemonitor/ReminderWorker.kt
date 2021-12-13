package edu.arizona.cast.austinramsay.glucosemonitor

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.*
import java.util.logging.Handler

private const val TAG = "ReminderWorker"

class ReminderWorker(val context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    val glucoseRepository = GlucoseRepository.get()

    override fun doWork(): Result {

        // Check if entry for today exists in database when this is called.
        // Notify the user if there is no data.
        // This worker should be the one to send the notification if it finds no data for today
        // If the user clicks on the notification, it should open a new input fragment for input

        val now = GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).time

        GlobalScope.launch(Dispatchers.Main) {
            glucoseRepository.checkExists(now).observe(ProcessLifecycleOwner.get(), { exists ->
                if (exists) {
                    Log.d(TAG, "Entry does exist for today.")
                } else {
                    Log.d(TAG, "No entry exists for today.")
                    makeNotification()
                }
            })
        }

        return Result.success()
    }

    private fun makeNotification() {
        val intent = MainActivity.newIntent(context)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val resources = context.resources
        val notification = NotificationCompat
            .Builder(context, NOTIFICATION_CHANNEL_ID)
            .setTicker(resources.getString(R.string.missing_entry_title))
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .setContentTitle(resources.getString(R.string.missing_entry_title))
            .setContentText(resources.getString(R.string.missing_entry_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        showBackgroundNotification(0, notification)
    }

    private fun showBackgroundNotification(requestCode: Int, notification: Notification) {
        val intent = Intent(ACTION_SHOW_NOTIFICATION).apply {
            putExtra(REQUEST_CODE, requestCode)
            putExtra(NOTIFICATION, notification)
        }

        context.sendOrderedBroadcast(intent, PERM_PRIVATE)
    }

    companion object {
        const val ACTION_SHOW_NOTIFICATION = "edu.arizona.cast.austinramsay.glucosemonitor.SHOW_NOTIFICATION"
        const val PERM_PRIVATE = "edu.arizona.cast.austinramsay.glucosemonitor.PRIVATE"
        const val REQUEST_CODE = "REQUEST_CODE"
        const val NOTIFICATION = "NOTIFICATION"
    }
}