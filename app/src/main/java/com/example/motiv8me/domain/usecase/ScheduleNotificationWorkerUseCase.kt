package com.example.motiv8me.domain.usecase

import android.content.Context
import androidx.work.*
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.service.NotificationWorker // Make sure this import is correct
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

        val notificationRequest =
            PeriodicWorkRequestBuilder<NotificationWorker>(frequency, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "unique_notification_job",
            ExistingPeriodicWorkPolicy.REPLACE,
            notificationRequest
        )
    }
}