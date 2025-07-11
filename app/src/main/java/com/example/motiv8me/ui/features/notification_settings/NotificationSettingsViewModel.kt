package com.example.motiv8me.ui.features.notification_settings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.domain.usecase.ScheduleNotificationWorkerUseCase
import com.example.motiv8me.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Notification Settings screen.
 */
data class NotificationSettingsUiState(
    val isLoading: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val selectedFrequencyValue: Long? = null,
    val availableFrequencies: List<Pair<String, Long>> = Constants.SHARED_APP_FREQUENCIES
)

/**
 * ViewModel for the Notification Settings screen.
 */
@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context, // CORRECTED: Inject context
    private val settingsRepository: SettingsRepository,
    private val scheduleNotificationWorkerUseCase: ScheduleNotificationWorkerUseCase
) : ViewModel() {

    // CORRECTED: Get an instance of NotificationManager from the context
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentSettings()
    }

    private fun loadCurrentSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { _ -> _uiState.update { it.copy(isLoading = false) } }
                .collect { settings ->
                    val frequency = settings.notificationFrequencyMinutes
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notificationsEnabled = frequency != Constants.NOTIFICATION_FREQUENCY_OFF,
                            selectedFrequencyValue = frequency
                        )
                    }
                }
        }
    }

    fun onFrequencySelected(newFrequencyMinutes: Long) {
        _uiState.update { it.copy(selectedFrequencyValue = newFrequencyMinutes, notificationsEnabled = true) }
        viewModelScope.launch {
            settingsRepository.saveNotificationFrequency(newFrequencyMinutes)
            scheduleNotificationWorkerUseCase()
            // CORRECTED: Update the system channel description
            updateNotificationChannelDescription(newFrequencyMinutes)
        }
    }

    fun onToggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val newFrequency = if (enabled) {
                uiState.value.selectedFrequencyValue.takeIf { it != Constants.NOTIFICATION_FREQUENCY_OFF }
                    ?: Constants.SHARED_APP_FREQUENCIES.first().second
            } else {
                Constants.NOTIFICATION_FREQUENCY_OFF
            }
            _uiState.update { it.copy(notificationsEnabled = enabled, selectedFrequencyValue = newFrequency) }

            settingsRepository.saveNotificationFrequency(newFrequency)
            scheduleNotificationWorkerUseCase()
            // CORRECTED: Update the system channel description
            updateNotificationChannelDescription(newFrequency)
        }
    }

    /**
     * CORRECTED: New function to update the notification channel's description.
     * This makes the text in the system settings screen accurate.
     */
    private fun updateNotificationChannelDescription(frequencyMinutes: Long) {
        // Find the human-readable label for the selected frequency
        val frequencyLabel = Constants.SHARED_APP_FREQUENCIES
            .find { it.second == frequencyMinutes }?.first?.lowercase()

        val description = if (frequencyMinutes == Constants.NOTIFICATION_FREQUENCY_OFF || frequencyLabel == null) {
            "Notifications are currently disabled."
        } else {
            "Delivers one motivational quote about every $frequencyLabel."
        }

        // Get the existing channel, update its description, and re-apply it.
        // It's safe to call createNotificationChannel multiple times.
        val channel = notificationManager.getNotificationChannel(Constants.NOTIFICATION_CHANNEL_ID)
        channel?.description = description
        if (channel != null) {
            notificationManager.createNotificationChannel(channel)
        }
    }
}