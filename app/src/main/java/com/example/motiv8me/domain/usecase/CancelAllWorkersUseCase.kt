package com.example.motiv8me.domain.usecase

import android.content.Context
import androidx.work.WorkManager
import javax.inject.Inject

class CancelAllWorkersUseCase @Inject constructor(
    private val context: Context
) {
    operator fun invoke() {
        WorkManager.getInstance(context).cancelUniqueWork("unique_wallpaper_changer")
        WorkManager.getInstance(context).cancelUniqueWork("unique_notification_job")
    }
}