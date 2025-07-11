package com.example.motiv8me.domain.usecase

import android.content.Context
import androidx.work.*
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.service.WallpaperWorker
import com.example.motiv8me.util.Constants
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleWallpaperWorkerUseCase @Inject constructor(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        val appSettings = settingsRepository.getSettings().first()
        val frequency = appSettings.wallpaperFrequencyMinutes

        // If frequency is null or invalid, cancel any existing work and do nothing.
        if (frequency == null || frequency < 15) {
            WorkManager.getInstance(context).cancelUniqueWork(Constants.WALLPAPER_WORKER_NAME)
            return
        }

        // Build the request. No need for inputData anymore.
        val wallpaperRequest =
            PeriodicWorkRequestBuilder<WallpaperWorker>(frequency, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            Constants.WALLPAPER_WORKER_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            wallpaperRequest
        )
    }
}