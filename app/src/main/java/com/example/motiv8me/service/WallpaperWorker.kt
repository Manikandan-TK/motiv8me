package com.example.motiv8me.service

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.io.IOException

@HiltWorker
class WallpaperWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    // ADDED: Inject the repository
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "WallpaperWorker"
        // REMOVED: KEY_HABIT_CATEGORY as we now fetch it directly
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "WallpaperWorker started. Attempt: $runAttemptCount")

        return try {
            // CHANGED: Get the habit directly from the repository
            val habitKey = settingsRepository.getSettings().first().selectedHabit
            if (habitKey.isNullOrEmpty()) {
                Log.e(TAG, "Failed - Habit key is missing from settings!")
                return Result.failure() // Stop work if no habit is set
            }

            Log.d(TAG, "Found habit key: $habitKey")

            // CHANGED: Use the habit key to get the image list from the map
            val imageList = Constants.HABIT_TO_IMAGE_MAP[habitKey]
            if (imageList.isNullOrEmpty()) {
                Log.e(TAG, "Failed - No wallpapers found for habit key: $habitKey")
                return Result.failure()
            }

            val wallpaperResourceId = imageList.random()
            Log.i(TAG, "Selected wallpaper ID: $wallpaperResourceId for habit: $habitKey")

            val wallpaperManager = WallpaperManager.getInstance(applicationContext)
            val bitmap = BitmapFactory.decodeResource(applicationContext.resources, wallpaperResourceId)

            if (bitmap == null) {
                Log.e(TAG, "Failed - Could not decode bitmap for resource ID: $wallpaperResourceId")
                return Result.failure()
            }

            wallpaperManager.setBitmap(bitmap)
            Log.i(TAG, "Success - Wallpaper changed for habit $habitKey (ID: $wallpaperResourceId)")
            Result.success()

        } catch (e: IOException) {
            Log.e(TAG, "Failed - IOException while setting wallpaper", e)
            Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Failed - Unexpected error in WallpaperWorker", e)
            Result.failure()
        }
    }
}