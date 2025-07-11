package com.example.motiv8me.ui.features.notification_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.domain.usecase.ScheduleNotificationWorkerUseCase
import com.example.motiv8me.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val settingsRepository: SettingsRepository,
    private val scheduleNotificationWorkerUseCase: ScheduleNotificationWorkerUseCase
) : ViewModel() {

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
                    // CORRECTED: Use the correct field name from AppSettings
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
        }
    }

    fun onToggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val newFrequency = if (enabled) {
                // CORRECTED: Restore the last known frequency or default to the first option from the correct constant
                uiState.value.selectedFrequencyValue.takeIf { it != Constants.NOTIFICATION_FREQUENCY_OFF }
                    ?: Constants.SHARED_APP_FREQUENCIES.first().second
            } else {
                Constants.NOTIFICATION_FREQUENCY_OFF
            }
            _uiState.update { it.copy(notificationsEnabled = enabled, selectedFrequencyValue = newFrequency) }

            settingsRepository.saveNotificationFrequency(newFrequency)
            scheduleNotificationWorkerUseCase()
        }
    }
}