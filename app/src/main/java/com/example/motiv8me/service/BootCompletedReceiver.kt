package com.example.motiv8me.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.domain.usecase.ScheduleNotificationWorkerUseCase
import com.example.motiv8me.domain.usecase.ScheduleWallpaperWorkerUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver that listens for the BOOT_COMPLETED action to reschedule
 * background workers (WallpaperWorker, NotificationWorker) after a device restart.
 * Uses Hilt EntryPoint to access necessary dependencies.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }

    // Define an EntryPoint interface to access dependencies from Hilt
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootReceiverEntryPoint {
        fun settingsRepository(): SettingsRepository
        fun scheduleWallpaperWorkerUseCase(): ScheduleWallpaperWorkerUseCase
        fun scheduleNotificationWorkerUseCase(): ScheduleNotificationWorkerUseCase
    }

    // Create a CoroutineScope for background tasks initiated by the receiver
    // SupervisorJob ensures that failure of one child job doesn't affect others
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive triggered with action: ${intent.action}")

        // Ensure this receiver only responds to the BOOT_COMPLETED action
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        // Use goAsync to handle work asynchronously outside the main thread
        // and keep the receiver alive until processing is finished.
        val pendingResult: PendingResult = goAsync()

        scope.launch {
            try {
                Log.d(TAG, "Coroutine launched to reschedule workers.")
                // Access Hilt dependencies using the EntryPoint
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext, // Use application context
                    BootReceiverEntryPoint::class.java
                )

                val settingsRepository = entryPoint.settingsRepository()
                val scheduleWallpaperWorker = entryPoint.scheduleWallpaperWorkerUseCase()
                val scheduleNotificationWorker = entryPoint.scheduleNotificationWorkerUseCase()

                // Fetch the latest settings
                val settings = settingsRepository.getSettings().first()
                Log.d(TAG, "Fetched settings: OnboardingComplete=${settings.isOnboardingComplete}, WallpaperFreq=${settings.wallpaperFrequencyMillis}, NotifFreq=${settings.notificationFrequencyMillis}")

                // Reschedule workers only if onboarding was completed
                if (settings.isOnboardingComplete) {
                    // Reschedule Wallpaper Worker based on saved frequency
                    scheduleWallpaperWorker(settings.wallpaperFrequencyMillis)

                    // Reschedule Notification Worker based on saved frequency
                    scheduleNotificationWorker(settings.notificationFrequencyMillis)

                    Log.i(TAG, "Workers reschedule attempt complete based on saved settings.")
                } else {
                    Log.i(TAG, "Onboarding not complete, skipping worker rescheduling.")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling workers on boot", e)
                // Handle exceptions appropriately (e.g., logging)
            } finally {
                // IMPORTANT: Always finish the pending result to release the wake lock
                // and allow the system to consider the receiver finished.
                pendingResult.finish()
                Log.d(TAG, "PendingResult finished.")
            }
        }
        Log.d(TAG, "onReceive finished initial processing.")
    }
}