package com.example.motiv8me.service

import android.app.WallpaperManager
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.random.Random

/**
 * A WorkManager Worker responsible for changing the device's home screen wallpaper
 * based on the user's selected habit stored in SettingsRepository.
 *
 * Injected with Hilt dependencies.
 */
@HiltWorker
class WallpaperWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository // Injected repository
) : CoroutineWorker(context, workerParams) {

    // Fallback constructor for WorkManager reflection
    constructor(context: Context, workerParams: WorkerParameters) : this(
        context,
        workerParams,
        settingsRepository = throw IllegalStateException("Hilt injection failed for WallpaperWorker")
    )

    companion object {
        private const val TAG = "WallpaperWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "WallpaperWorker started. App context: ${applicationContext.packageName}, User: ${android.os.Process.myUserHandle()}");

        return try {
            // Fetch the latest settings when the worker runs
            val settings = settingsRepository.getSettings().first() // Get the current settings state

            // Check if onboarding is complete
            if (!settings.isOnboardingComplete) {
                Log.w(TAG, "Onboarding not complete. Skipping wallpaper change.")
                return Result.success() // Not a failure, just nothing to do yet
            }

            // Get the selected habit
            val selectedHabit = settings.selectedHabit
            if (selectedHabit == null) {
                Log.e(TAG, "No habit selected in settings. Cannot change wallpaper.")
                return Result.failure() // Cannot proceed without a habit
            }

            Log.d(TAG, "Selected habit: $selectedHabit")

            // Find the list of images for the selected habit
            val imageResourceIds = Constants.HABIT_TO_IMAGE_MAP[selectedHabit]
            if (imageResourceIds.isNullOrEmpty()) {
                Log.e(TAG, "No images found for habit '$selectedHabit' in Constants.HABIT_TO_IMAGE_MAP.")
                return Result.failure() // Cannot proceed without images
            }

            // Select a random image from the list
            val randomImageResId = imageResourceIds.random(Random.Default)
            Log.d(TAG, "Selected image resource ID: $randomImageResId")

            var wallpaperSet = false
            var exceptionMessage: String? = null
            var exceptionStack: String? = null

            withContext(Dispatchers.IO) {
                try {
                    val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                    wallpaperManager.setResource(randomImageResId, WallpaperManager.FLAG_SYSTEM)
                    Log.i(TAG, "Wallpaper successfully set for habit '$selectedHabit'.")
                    wallpaperSet = true
                } catch (e: IOException) {
                    Log.e(TAG, "IOException while setting wallpaper: ${e.message}", e)
                    exceptionMessage = e.message
                    exceptionStack = Log.getStackTraceString(e)
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException: Missing SET_WALLPAPER permission: ${e.message}", e)
                    exceptionMessage = e.message
                    exceptionStack = Log.getStackTraceString(e)
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error setting wallpaper: ${e.message}", e)
                    exceptionMessage = e.message
                    exceptionStack = Log.getStackTraceString(e)
                }
            }

            if (!wallpaperSet) {
                Log.e(TAG, "Wallpaper was NOT set. Exception: $exceptionMessage\nStack: $exceptionStack")
                return Result.failure()
            }

            Result.success() // Wallpaper set successfully

        } catch (e: Exception) {
            Log.e(TAG, "WallpaperWorker failed: ${e.message}", e)
            Result.failure()
        }
    }

    private fun hasWallpaperPermission(): Boolean {
        val pm = applicationContext.packageManager
        val granted = pm.checkPermission("android.permission.SET_WALLPAPER", applicationContext.packageName) == android.content.pm.PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "SET_WALLPAPER permission granted: $granted")
        return granted
    }
}