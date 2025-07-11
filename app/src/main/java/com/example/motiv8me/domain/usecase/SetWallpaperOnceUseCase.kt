package com.example.motiv8me.domain.usecase

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * A use case dedicated to setting the wallpaper a single time, immediately.
 * This is used when a user changes their habit and expects an instant visual update,
 * separate from the periodic background worker.
 */
class SetWallpaperOnceUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    /**
     * Executes the use case.
     * It fetches the current habit, finds a corresponding wallpaper, and sets it.
     */
    suspend operator fun invoke() {
        try {
            // Fetch the currently selected habit key from settings.
            val habitKey = settingsRepository.getSettings().first().selectedHabit
            if (habitKey.isNullOrEmpty()) {
                Log.e("SetWallpaperOnceUseCase", "Cannot set wallpaper, habit key is null or empty.")
                return
            }

            // Find the list of wallpapers associated with the habit.
            val imageList = Constants.HABIT_TO_IMAGE_MAP[habitKey]
            if (imageList.isNullOrEmpty()) {
                Log.e("SetWallpaperOnceUseCase", "No wallpapers found for habit: $habitKey")
                return
            }

            // Select a random wallpaper and set it.
            val wallpaperResourceId = imageList.random()
            val bitmap = BitmapFactory.decodeResource(context.resources, wallpaperResourceId)
            WallpaperManager.getInstance(context).setBitmap(bitmap)
            Log.i("SetWallpaperOnceUseCase", "Successfully set one-time wallpaper for habit: $habitKey")

        } catch (e: Exception) {
            Log.e("SetWallpaperOnceUseCase", "Failed to set one-time wallpaper", e)
        }
    }
}