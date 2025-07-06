package com.example.motiv8me.domain.usecase

import android.content.Context
import androidx.work.*
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.service.WallpaperWorker
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleWallpaperWorkerUseCase @Inject constructor(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    // The 'invoke' function makes the class callable like a function.
    suspend operator fun invoke() {
        // 1. Get the user's saved settings from DataStore.
        //    'first()' gets the most recent value from the Flow.
        val appSettings = settingsRepository.getSettings().first()
        val frequency = appSettings.wallpaperChangeFrequencyMinutes
        val habit = appSettings.selectedHabit

        // 2. Create Data to pass the habit category to the worker.
        val inputData = Data.Builder()
            .putString(WallpaperWorker.KEY_HABIT_CATEGORY, habit) // We'll need to update the worker to use this
            .build()

        // 3. Build the request using the frequency from the user's settings.
        val wallpaperRequest =
            PeriodicWorkRequestBuilder<WallpaperWorker>(frequency, TimeUnit.MINUTES)
                .setInputData(inputData) // THIS WAS THE MISSING PIECE!
                // Add a constraint to only run when the battery is not low.
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()

        // 4. Enqueue the work. Use REPLACE to ensure that if the user changes
        //    the frequency in settings, the old job is replaced with the new one.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "unique_wallpaper_changer",
            ExistingPeriodicWorkPolicy.REPLACE,
            wallpaperRequest
        )
    }
}