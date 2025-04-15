package com.example.motiv8me.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.motiv8me.MainActivity // Import your main activity
import com.example.motiv8me.R // Import R for icon resources
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlin.random.Random

/**
 * A WorkManager Worker responsible for fetching a random motivational quote
 * and displaying it as a system notification based on user settings.
 *
 * Injected with Hilt dependencies.
 */
@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository // Injected repository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "NotificationWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "NotificationWorker started.")

        return try {
            // Fetch the latest settings when the worker runs
            val settings = settingsRepository.getSettings().first()

            // Check if onboarding is complete
            if (!settings.isOnboardingComplete) {
                Log.w(TAG, "Onboarding not complete. Skipping notification.")
                return Result.success() // Not a failure, just nothing to do yet
            }

            // Check if notifications are enabled (frequency > 0)
            val notificationFrequency = settings.notificationFrequencyMillis
            if (notificationFrequency == null || notificationFrequency <= 0L) {
                Log.i(TAG, "Notifications are disabled (frequency is $notificationFrequency). Skipping.")
                return Result.success() // Notifications are off, work is successful
            }

            // Check for notification permission (Android 13+) BEFORE proceeding
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        appContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(TAG, "POST_NOTIFICATIONS permission denied. Cannot show notification.")
                    // Returning failure might cause retries, which isn't helpful if permission
                    // is permanently denied. Success might be better to avoid spamming logs,
                    // but failure indicates the task couldn't complete as intended. Let's use failure.
                    return Result.failure()
                }
            }

            // Get a random quote
            val quotes = Constants.MOTIVATIONAL_QUOTES
            if (quotes.isEmpty()) {
                Log.e(TAG, "Motivational quotes list is empty in Constants.")
                return Result.failure()
            }
            val randomQuote = quotes.random(Random.Default)
            Log.d(TAG, "Selected quote: $randomQuote")

            // Create notification channel (required for Android 8.0+)
            createNotificationChannel()

            // Show the notification
            showNotification(randomQuote)

            Log.i(TAG, "Notification successfully shown.")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "NotificationWorker failed.", e)
            Result.failure() // General failure
        }
    }

    /**
     * Creates the notification channel required for Android 8.0 (API 26) and above.
     * If the channel already exists, no action is taken.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = Constants.NOTIFICATION_CHANNEL_NAME
            val descriptionText = "Channel for Motiv8Me motivational quotes" // TODO: Move to strings.xml
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                // Configure other channel properties if needed (e.g., sound, vibration)
                // enableLights(true)
                // lightColor = Color.CYAN // Example
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created or already exists.")
        }
    }

    /**
     * Builds and displays the notification with the given quote.
     *
     * @param quote The motivational quote to display.
     */
    private fun showNotification(quote: String) {
        // Intent to launch MainActivity when the notification is tapped
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // You could add extras here if needed to navigate to a specific screen
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            appContext,
            0, // Request code
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Use IMMUTABLE
        )

        // Build the notification
        val builder = NotificationCompat.Builder(appContext, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_placeholder) // TODO: Create a proper notification icon
            .setContentTitle(appContext.getString(R.string.app_name)) // App name as title
            .setContentText(quote) // The quote as the main text
            .setStyle(NotificationCompat.BigTextStyle().bigText(quote)) // Allow longer quotes
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Set the intent to trigger on tap
            .setAutoCancel(true) // Dismiss notification when tapped

        // Get NotificationManagerCompat and display the notification
        with(NotificationManagerCompat.from(appContext)) {
            // notificationId is a unique int for this notification that allows
            // you to update or cancel it later.
            val canPost = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
            if (canPost) {
                try {
                    notify(Constants.NOTIFICATION_ID, builder.build())
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException: Notification permission denied at runtime.", e)
                }
            } else {
                Log.e(TAG, "Notification permission not granted. Skipping notify().")
            }
        }
    }
}