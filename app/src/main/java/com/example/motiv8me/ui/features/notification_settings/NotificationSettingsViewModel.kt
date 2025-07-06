package com.example.motiv8me.ui.features.notification_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.domain.usecase.ScheduleNotificationWorkerUseCase
import com.example.motiv8me.util.Constants.NOTIFICATION_FREQUENCY_OFF
import com.example.motiv8me.util.Constants.NOTIFICATION_FREQUENCY_OPTIONS
import com.example.motiv8me.util.Constants.SHARED_APP_FREQUENCIES
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
    val availableFrequencies: List<Pair<String, Long>> = SHARED_APP_FREQUENCIES
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
                .catch { e -> _uiState.update { it.copy(isLoading = false) } }
                .collect { settings ->
                    val frequency = settings.notificationFrequencyMillis
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notificationsEnabled = frequency != NOTIFICATION_FREQUENCY_OFF,
                            selectedFrequencyValue = frequency
                        )
                    }
                }
        }
    }

    fun onFrequencySelected(newFrequencyMillis: Long) {
        _uiState.update { it.copy(selectedFrequencyValue = newFrequencyMillis, notificationsEnabled = true) }
        viewModelScope.launch {
            settingsRepository.saveNotificationFrequency(newFrequencyMillis)
            scheduleNotificationWorkerUseCase()
        }
    }

    fun onToggleNotifications(enabled: Boolean) {
        val newFrequency = if (enabled) {
            // Restore the last known frequency or default to the first option in the list
            _uiState.value.selectedFrequencyValue.takeIf { it != NOTIFICATION_FREQUENCY_OFF } 
                ?: NOTIFICATION_FREQUENCY_OPTIONS.first().second
        } else {
            NOTIFICATION_FREQUENCY_OFF
        }
        _uiState.update { it.copy(notificationsEnabled = enabled, selectedFrequencyValue = newFrequency) }

        viewModelScope.launch {
            settingsRepository.saveNotificationFrequency(newFrequency)
            scheduleNotificationWorkerUseCase()
        }
    }
}