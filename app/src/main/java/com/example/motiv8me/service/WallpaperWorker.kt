package com.example.motiv8me.service

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.motiv8me.R
import java.io.IOException

class WallpaperWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "WallpaperWorker"
        const val KEY_HABIT_CATEGORY = "KEY_HABIT_CATEGORY"
        private const val MAX_WALLPAPERS_PER_CATEGORY = 20
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "doWork: Triggered. Attempt: $runAttemptCount") // Log run attempt

        val habitCategory = inputData.getString(KEY_HABIT_CATEGORY)

        if (habitCategory.isNullOrEmpty()) {
            Log.e(TAG, "doWork: Failed - Habit category is missing!")
            return Result.failure()
        }

        Log.d(TAG, "doWork: Category: $habitCategory")

        return try {
            val resources = applicationContext.resources
            val packageName = applicationContext.packageName
            val dynamicImageList = mutableListOf<Int>()
            val namePrefix = "wallpaper_${habitCategory}_"

            Log.d(TAG, "doWork: Searching for drawables with prefix: $namePrefix")
            for (i in 1..MAX_WALLPAPERS_PER_CATEGORY) {
                val resourceName = "$namePrefix$i"
                val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
                if (resourceId != 0) {
                    dynamicImageList.add(resourceId)
                    Log.d(TAG, "doWork: Found wallpaper - Name: $resourceName, ID: $resourceId")
                } else {
                     Log.v(TAG, "doWork: No wallpaper found for name: $resourceName (this might be expected if not all numbers up to MAX are used)")
                }
            }

            if (dynamicImageList.isEmpty()) {
                Log.e(TAG, "doWork: Failed - No wallpapers found for category: $habitCategory with prefix: $namePrefix")
                return Result.failure()
            }

            val wallpaperResourceId = dynamicImageList.random()
            Log.i(TAG, "doWork: Selected wallpaper ID: $wallpaperResourceId for category: $habitCategory")

            val wallpaperManager = WallpaperManager.getInstance(applicationContext)
            val bitmap: Bitmap? = BitmapFactory.decodeResource(resources, wallpaperResourceId)

            if (bitmap == null) {
                Log.e(TAG, "doWork: Failed - Could not decode bitmap for resource ID: $wallpaperResourceId")
                return Result.failure()
            }

            try {
                wallpaperManager.setBitmap(bitmap)
                Log.i(TAG, "doWork: Success - Wallpaper changed (ID: $wallpaperResourceId)")
                Result.success()
            } catch (e: IOException) {
                Log.e(TAG, "doWork: Failed - IOException while setting wallpaper (ID: $wallpaperResourceId)", e)
                Result.failure()
            }

        } catch (e: Exception) {
            Log.e(TAG, "doWork: Failed - Unexpected error for category: $habitCategory", e)
            Result.failure()
        }
    }
}
