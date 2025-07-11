package com.example.motiv8me.domain.usecase

import android.content.Context
import androidx.work.*
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.service.NotificationWorker
import com.example.motiv8me.util.Constants
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleNotificationWorkerUseCase @Inject constructor(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        val appSettings = settingsRepository.getSettings().first()
        val frequency = appSettings.notificationFrequencyMinutes

        // If frequency is off, null, or invalid, cancel any existing work and do nothing.
        if (frequency == null || frequency < 15) {
            WorkManager.getInstance(context).cancelUniqueWork(Constants.NOTIFICATION_WORKER_NAME)
            return
        }

        val notificationRequest =
            PeriodicWorkRequestBuilder<NotificationWorker>(frequency, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            Constants.NOTIFICATION_WORKER_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            notificationRequest
        )
    }
}