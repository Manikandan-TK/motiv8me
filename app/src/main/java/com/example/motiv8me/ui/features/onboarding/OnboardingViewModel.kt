package com.example.motiv8me.ui.features.onboarding

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motiv8me.domain.repository.SettingsRepository // Assuming this interface exists
import com.example.motiv8me.util.Constants // Assuming Constants.kt exists with habits/frequencies
import com.example.motiv8me.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents the state of the Onboarding screen.
 *
 * @param selectedHabit The currently selected habit identifier (e.g., name or ID). Null if none selected.
 * @param selectedFrequencyMillis The currently selected frequency in milliseconds. Null if none selected.
 * @param availableHabits List of predefined habits the user can choose from.
 * @param availableFrequencies Map of display names (e.g., "1 hour") to frequency values in milliseconds.
 * @param isWallpaperPermissionGranted Current status of the SET_WALLPAPER permission.
 * @param isNotificationPermissionGranted Current status of the POST_NOTIFICATIONS permission.
 */
data class OnboardingUiState(
    val selectedHabit: String? = null,
    val selectedFrequencyMillis: Long? = null,
    val availableHabits: List<String> = emptyList(), // Use String for now, replace with Habit model later
    val availableFrequencies: Map<String, Long> = emptyMap(),
    val isWallpaperPermissionGranted: Boolean = false,
    val isNotificationPermissionGranted: Boolean = true, // Default true for pre-Tiramisu
    val isLoading: Boolean = true // Indicate initial loading/permission checks
) {
    /**
     * Determines if all necessary selections have been made and permissions granted
     * to allow the user to complete onboarding.
     */
    val canCompleteOnboarding: Boolean
        get() {
            // Check if notification permission is required and granted for the current OS version
            val notificationOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isNotificationPermissionGranted
            } else {
                true // Not needed below Tiramisu
            }
            // Wallpaper permission is assumed granted via manifest for now
            return selectedHabit != null &&
                    selectedFrequencyMillis != null &&
                    isWallpaperPermissionGranted &&
                    notificationOk &&
                    !isLoading // Ensure loading/checks are done
        }
}

/**
 * ViewModel for the Onboarding screen.
 * Manages the state (habit/frequency selection, permissions) and handles saving
 * the selections upon completion.
 *
 * @param settingsRepository Repository for saving user preferences.
 * @param applicationContext Context used for permission checks.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository, // TODO: Create this repository interface/impl
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        checkInitialPermissions()
    }

    /**
     * Loads predefined habits and frequencies from Constants.
     */
    private fun loadInitialData() {
        _uiState.update {
            it.copy(
                availableHabits = Constants.PREDEFINED_HABITS, // TODO: Define in Constants.kt
                availableFrequencies = Constants.WALLPAPER_FREQUENCIES // TODO: Define in Constants.kt
            )
        }
    }

    /**
     * Checks the initial status of required permissions.
     */
    fun checkInitialPermissions() {
        val wallpaperGranted = PermissionUtils.hasWallpaperPermission(applicationContext)
        val notificationGranted = PermissionUtils.hasNotificationPermission(applicationContext)

        _uiState.update {
            it.copy(
                isWallpaperPermissionGranted = wallpaperGranted,
                isNotificationPermissionGranted = notificationGranted,
                isLoading = false // Mark initial checks as complete
            )
        }
    }

    /**
     * Updates the state when the user selects a habit.
     * @param habit The identifier (e.g., name) of the selected habit.
     */
    fun onHabitSelected(habit: String) {
        _uiState.update { it.copy(selectedHabit = habit) }
    }

    /**
     * Updates the state when the user selects a frequency.
     * @param frequencyMillis The selected frequency value in milliseconds.
     */
    fun onFrequencySelected(frequencyMillis: Long) {
        _uiState.update { it.copy(selectedFrequencyMillis = frequencyMillis) }
    }

    /**
     * Updates the notification permission status based on the user's response
     * to the runtime permission request dialog.
     * @param isGranted True if the user granted the permission, false otherwise.
     */
    fun onNotificationPermissionResult(isGranted: Boolean) {
        // Only update if the OS requires the permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            _uiState.update { it.copy(isNotificationPermissionGranted = isGranted) }
        }
    }

    /**
     * Saves the selected habit and frequency settings using the repository.
     * This should be called when the user clicks the "Finish" button.
     * Assumes validation (checking if selections are non-null) happens before calling.
     */
    fun saveOnboardingSelections() {
        val currentState = _uiState.value
        val habit = currentState.selectedHabit
        val frequency = currentState.selectedFrequencyMillis

        if (habit != null && frequency != null) {
            viewModelScope.launch {
                try {
                    // TODO: Implement these methods in SettingsRepository
                    settingsRepository.saveHabitSetting(habit)
                    settingsRepository.saveWallpaperFrequency(frequency)
                    settingsRepository.saveOnboardingComplete(true) // Mark onboarding as done
                } catch (e: Exception) {
                    // TODO: Handle potential errors during saving (e.g., show a message)
                    // Log.e("OnboardingViewModel", "Error saving settings", e)
                }
            }
        } else {
            // This should ideally not happen if the finish button is correctly enabled/disabled
            // Log.w("OnboardingViewModel", "Attempted to save incomplete selections")
        }
    }
}