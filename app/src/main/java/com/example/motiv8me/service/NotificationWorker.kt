package com.example.motiv8me.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.motiv8me.R

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "NotificationWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Notification worker has started.")

        return try {
            // For this test, we'll use a simple list of quotes.
            val quotes = listOf(
                "The only way to do great work is to love what you do.",
                "Believe you can and you're halfway there.",
                "The future belongs to those who believe in the beauty of their dreams.",
                "Success is not final, failure is not fatal: it is the courage to continue that counts."
            )

            // Pick a random quote from our list.
            val randomQuote = quotes.random()

            // A unique ID for this specific notification. If you post another
            // notification with the same ID, it will update the existing one.
            val notificationId = 1

            // Build the notification using the channel we created earlier.
            val builder = NotificationCompat.Builder(applicationContext, "MOTIVATION_CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // IMPORTANT: Use your app's icon
                .setContentTitle("Your Daily Motivation!")
                .setContentText(randomQuote)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(NotificationCompat.BigTextStyle().bigText(randomQuote)) // Allows for longer text

            // Show the notification.
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                with(NotificationManagerCompat.from(applicationContext)) {
                    // The POST_NOTIFICATIONS permission is checked by the system automatically here.
                    // If the user denied it, this line will do nothing, and no crash will occur.
                    notify(notificationId, builder.build())
                }
                Log.d(TAG, "Notification sent successfully!")
            } else {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Notification not sent.")
                // Optionally, you could return Result.failure() here if the notification is critical
            }
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification", e)
            Result.failure()
        }
    }
}