package com.example.motiv8me.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.motiv8me.R
import com.example.motiv8me.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "NotificationWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Notification worker has started.")

        // Permission check is crucial before proceeding
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Aborting work.")
            // This is not a failure of the work itself, but a state of the system.
            // Returning success prevents it from retrying unnecessarily.
            return Result.success()
        }

        return try {
            val randomQuote = Constants.MOTIVATIONAL_QUOTES.random()
            val notificationId = Constants.NOTIFICATION_ID

            val builder = NotificationCompat.Builder(applicationContext, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_placeholder) // A proper monochrome icon is best
                .setContentTitle("Your Daily Motivation!")
                .setContentText(randomQuote)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(NotificationCompat.BigTextStyle().bigText(randomQuote))

            with(NotificationManagerCompat.from(applicationContext)) {
                notify(notificationId, builder.build())
            }

            Log.d(TAG, "Notification sent successfully!")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification", e)
            Result.failure()
        }
    }
}