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
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.io.IOException

// 1. DEFINE AN ENTRYPOINT INTERFACE
// This tells Hilt how to give us a SettingsRepository on demand.
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WallpaperWorkerEntryPoint {
    fun settingsRepository(): SettingsRepository
}

@HiltWorker
// 2. SIMPLIFY THE CONSTRUCTOR
// Remove the SettingsRepository from the constructor to avoid the instantiation error.
class WallpaperWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "WallpaperWorker"
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "WallpaperWorker started. Attempt: $runAttemptCount")

        // 3. GET THE DEPENDENCY MANUALLY VIA THE ENTRYPOINT
        // This is the new, robust way to get our repository.
        val entryPoint = EntryPointAccessors.fromApplication(appContext, WallpaperWorkerEntryPoint::class.java)
        val settingsRepository = entryPoint.settingsRepository()

        return try {
            // The rest of the logic is exactly the same.
            val habitKey = settingsRepository.getSettings().first().selectedHabit
            if (habitKey.isNullOrEmpty()) {
                Log.e(TAG, "Failed - Habit key is missing from settings!")
                return Result.failure()
            }

            Log.d(TAG, "Found habit key: $habitKey")

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