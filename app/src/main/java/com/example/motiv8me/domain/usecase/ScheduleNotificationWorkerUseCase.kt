package com.example.motiv8me.domain.usecase

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.motiv8me.service.NotificationWorker // Import the correct worker
import com.example.motiv8me.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Use case responsible for scheduling or cancelling the periodic NotificationWorker
 * based on the provided frequency setting.
 */
class ScheduleNotificationWorkerUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)
    private val logTag = "ScheduleNotificationWrk" // Shortened tag

    /**
     * Schedules the NotificationWorker to run periodically with the given frequency,
     * or cancels it if the frequency is null or invalid (<= 0, meaning "Off").
     * Uses REPLACE policy to ensure only one schedule exists for this worker.
     *
     * @param frequencyMillis The desired interval between notifications in milliseconds.
     *                        Null or <= 0 will cancel any existing schedule.
     */
    operator fun invoke(frequencyMillis: Long?) {
        Log.d(logTag, "Invoked with frequency: $frequencyMillis ms")

        if (frequencyMillis == null || frequencyMillis <= 0L) {
            // --- Cancel Worker ---
            // Treat 0L or null as "Off"
            workManager.cancelUniqueWork(Constants.NOTIFICATION_WORKER_NAME)
            Log.i(logTag, "Cancelled unique periodic work: ${Constants.NOTIFICATION_WORKER_NAME}")
        } else {
            // --- Schedule Worker ---
            // Ensure minimum interval if needed (WorkManager enforces minimum 15 mins)
            // val actualInterval = maxOf(frequencyMillis, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS)
            // Using the user value directly for now.

            val notificationWorkRequest =
                PeriodicWorkRequestBuilder<NotificationWorker>( // Specify the NotificationWorker
                    repeatInterval = frequencyMillis,
                    repeatIntervalTimeUnit = TimeUnit.MILLISECONDS
                )
                    // TODO: Consider adding constraints (e.g., battery not low?)
                    // .setConstraints(...)
                    .build()

            // Enqueue the work as unique periodic work
            workManager.enqueueUniquePeriodicWork(
                Constants.NOTIFICATION_WORKER_NAME, // Unique name from Constants
                ExistingPeriodicWorkPolicy.REPLACE, // Replace existing work with the same name
                notificationWorkRequest
            )
            Log.i(
                logTag,
                "Enqueued unique periodic work '${Constants.NOTIFICATION_WORKER_NAME}' with interval $frequencyMillis ms"
            )
        }
    }
}