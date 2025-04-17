package com.example.motiv8me.ui.features.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.motiv8me.domain.model.AppSettings
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.domain.usecase.ScheduleNotificationWorkerUseCase
import com.example.motiv8me.domain.usecase.ScheduleWallpaperWorkerUseCase
import com.example.motiv8me.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
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
    private val scheduleNotificationWorkerUseCase: ScheduleNotificationWorkerUseCase, // Needed if notifications are changed here or to display status
    @ApplicationContext private val appContext: Context
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
        Log.d("SettingsViewModel", "onHabitChanged called with habit: $newHabit")
        viewModelScope.launch {
            settingsRepository.saveHabitSetting(newHabit)
            // Re-enabled: Immediately trigger a one-time wallpaper change on habit change for testing.
            WorkManager.getInstance(appContext).enqueue(
                OneTimeWorkRequestBuilder<com.example.motiv8me.service.WallpaperWorker>().build()
            )
            _uiState.update { it.copy(currentHabit = newHabit) }
        }
    }

    /**
     * Called when the user selects a new wallpaper frequency.
     * Saves the new frequency and reschedules the WallpaperWorker.
     */
    fun onWallpaperFrequencyChanged(newFrequencyMillis: Long) {
        Log.d("SettingsViewModel", "onWallpaperFrequencyChanged called with frequency: $newFrequencyMillis")
        viewModelScope.launch {
            settingsRepository.saveWallpaperFrequency(newFrequencyMillis)
            scheduleWallpaperWorkerUseCase(newFrequencyMillis)
            // Immediately trigger a one-time wallpaper change
            WorkManager.getInstance(appContext).enqueue(
                OneTimeWorkRequestBuilder<com.example.motiv8me.service.WallpaperWorker>().build()
            )
            _uiState.update { it.copy(currentWallpaperFrequencyMillis = newFrequencyMillis) }
        }
    }

    /**
     * Called when the user selects a new notification frequency (likely from NotificationSettingsScreen).
     * Saves the new frequency and reschedules the NotificationWorker.
     */
    fun onNotificationFrequencyChanged(newFrequencyMillis: Long) {
        Log.d("SettingsViewModel", "onNotificationFrequencyChanged called with frequency: $newFrequencyMillis")
        viewModelScope.launch {
            settingsRepository.saveNotificationFrequency(newFrequencyMillis)
            scheduleNotificationWorkerUseCase(newFrequencyMillis)
            // Immediately trigger a one-time notification
            WorkManager.getInstance(appContext).enqueue(
                OneTimeWorkRequestBuilder<com.example.motiv8me.service.NotificationWorker>().build()
            )
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