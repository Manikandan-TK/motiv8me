package com.example.motiv8me.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motiv8me.domain.model.AppSettings
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.domain.usecase.ScheduleNotificationWorkerUseCase
import com.example.motiv8me.domain.usecase.ScheduleWallpaperWorkerUseCase
import com.example.motiv8me.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Settings screen.
 */
data class SettingsUiState(
    val isLoading: Boolean = true,
    val currentHabit: String? = null,
    val currentWallpaperFrequencyMillis: Long? = null,
    val currentNotificationFrequencyMillis: Long? = null,
    val availableWallpaperFrequencies: Map<String, Long> = Constants.WALLPAPER_FREQUENCIES,
    val availableNotificationFrequencies: Map<String, Long> = Constants.NOTIFICATION_FREQUENCIES // Needed for display text lookup
)

/**
 * ViewModel for the Settings screen.
 * Handles fetching current settings and updating them based on user interaction.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val scheduleWallpaperWorkerUseCase: ScheduleWallpaperWorkerUseCase,
    private val scheduleNotificationWorkerUseCase: ScheduleNotificationWorkerUseCase // Needed if notifications are changed here or to display status
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * Loads the current settings from the repository and updates the UI state.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    // Handle error loading settings
                    _uiState.update { it.copy(isLoading = false) }
                    // Log.e("SettingsViewModel", "Error loading settings", e)
                }
                .collect { appSettings ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentHabit = appSettings.selectedHabit,
                            currentWallpaperFrequencyMillis = appSettings.wallpaperFrequencyMillis,
                            currentNotificationFrequencyMillis = appSettings.notificationFrequencyMillis
                        )
                    }
                }
        }
    }

    /**
     * Called when the user selects a new habit from the HabitSelectionScreen.
     * Saves the new habit and potentially re-evaluates worker scheduling if needed.
     */
    fun onHabitChanged(newHabit: String) {
        viewModelScope.launch {
            settingsRepository.saveHabitSetting(newHabit)
            // No need to reschedule workers just for habit change,
            // the worker reads the latest habit when it runs.
            // Update UI state explicitly if not relying solely on flow collection
            _uiState.update { it.copy(currentHabit = newHabit) }
        }
    }

    /**
     * Called when the user selects a new wallpaper frequency.
     * Saves the new frequency and reschedules the WallpaperWorker.
     */
    fun onWallpaperFrequencyChanged(newFrequencyMillis: Long) {
        viewModelScope.launch {
            settingsRepository.saveWallpaperFrequency(newFrequencyMillis)
            scheduleWallpaperWorkerUseCase(newFrequencyMillis) // Reschedule worker
            // Update UI state explicitly if not relying solely on flow collection
            _uiState.update { it.copy(currentWallpaperFrequencyMillis = newFrequencyMillis) }
        }
    }

    /**
     * Called when the user selects a new notification frequency (likely from NotificationSettingsScreen).
     * Saves the new frequency and reschedules the NotificationWorker.
     */
    fun onNotificationFrequencyChanged(newFrequencyMillis: Long) {
        viewModelScope.launch {
            settingsRepository.saveNotificationFrequency(newFrequencyMillis)
            scheduleNotificationWorkerUseCase(newFrequencyMillis) // Reschedule worker
            // Update UI state explicitly if not relying solely on flow collection
            _uiState.update { it.copy(currentNotificationFrequencyMillis = newFrequencyMillis) }
        }
    }

    /**
     * Helper function to get the display name for a given frequency value.
     */
    fun getFrequencyDisplayName(frequencyMillis: Long?, availableFrequencies: Map<String, Long>): String? {
        return availableFrequencies.entries.find { it.value == frequencyMillis }?.key
    }
}