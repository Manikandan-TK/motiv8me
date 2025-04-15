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
    val currentFrequencyMillis: Long? = null, // Null initially or if not set
    val availableFrequencies: Map<String, Long> = Constants.NOTIFICATION_FREQUENCIES
)

/**
 * ViewModel for the Notification Settings screen.
 * Handles fetching the current notification frequency and updating it.
 */
@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val scheduleNotificationWorkerUseCase: ScheduleNotificationWorkerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentFrequency()
    }

    /**
     * Loads the current notification frequency setting from the repository.
     */
    private fun loadCurrentFrequency() {
        viewModelScope.launch {
            settingsRepository.getSettings()
                .map { it.notificationFrequencyMillis } // Only need this specific setting
                .distinctUntilChanged() // Only emit when the value actually changes
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    // Handle error loading settings
                    _uiState.update { it.copy(isLoading = false) }
                    // Log.e("NotifSettingsVM", "Error loading notification frequency", e)
                }
                .collect { frequency ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            // Ensure 0L is treated as a valid selection ("Off")
                            currentFrequencyMillis = frequency ?: Constants.NOTIFICATION_FREQUENCIES["Off"]
                        )
                    }
                }
        }
    }

    /**
     * Called when the user selects a new notification frequency.
     * Saves the new frequency and reschedules the NotificationWorker.
     */
    fun onFrequencySelected(newFrequencyMillis: Long) {
        // Update UI state immediately for responsiveness (optional, flow should update anyway)
        // _uiState.update { it.copy(currentFrequencyMillis = newFrequencyMillis) }

        viewModelScope.launch {
            settingsRepository.saveNotificationFrequency(newFrequencyMillis)
            scheduleNotificationWorkerUseCase(newFrequencyMillis) // Reschedule worker
            // The flow collection in init should automatically update the UI state
        }
    }
}