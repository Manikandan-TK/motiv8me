package com.example.motiv8me.ui.features.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.domain.usecase.CancelAllWorkersUseCase
import com.example.motiv8me.domain.usecase.ScheduleNotificationWorkerUseCase
import com.example.motiv8me.domain.usecase.ScheduleWallpaperWorkerUseCase
import com.example.motiv8me.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.motiv8me.domain.model.AppSettings
import com.example.motiv8me.domain.permission.PermissionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the redesigned Settings screen.
 */
data class SettingsUiState(
    val isLoading: Boolean = true,
    // Personalization
    val currentHabit: String = "Not Set",
    val wallpaperFrequencyMillis: Long? = null,
    val wallpaperFrequencyDisplayName: String = "Not Set",
    val notificationFrequencyDisplayName: String = "Disabled",
    // Permissions
    val hasWallpaperPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    // Available Options
    val availableWallpaperFrequencies: List<Pair<String, Long>> = Constants.SHARED_APP_FREQUENCIES,
    val availableNotificationFrequencies: List<Pair<String, Long>> = Constants.NOTIFICATION_FREQUENCY_OPTIONS,
    val selectedTheme: String = "System", // ADD THIS
    val availableThemes: List<String> = listOf("Light", "Dark", "System") // ADD THIS
)

/**
 * ViewModel for the redesigned Settings screen.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val scheduleWallpaperWorkerUseCase: ScheduleWallpaperWorkerUseCase,
    private val scheduleNotificationWorkerUseCase: ScheduleNotificationWorkerUseCase,
    private val cancelAllWorkersUseCase: CancelAllWorkersUseCase,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadAndCheckSettings()
    }

    /**
     * Load all settings from the repository and check current permission statuses.
     */
    private fun loadAndCheckSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.getSettings(),
                settingsRepository.themePreference,
                permissionManager.permissionStatus
            ) { appSettings, theme, permissions ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentHabit = appSettings.selectedHabit?.formatHabitDisplayName() ?: "Not Set",
                        wallpaperFrequencyMillis = appSettings.wallpaperFrequencyMillis,
                        wallpaperFrequencyDisplayName = getFrequencyDisplayName(
                            appSettings.wallpaperFrequencyMillis,
                            Constants.WALLPAPER_FREQUENCIES.toList()
                        ) ?: "Not Set",
                        notificationFrequencyDisplayName = getFrequencyDisplayName(
                            appSettings.notificationFrequencyMillis,
                            Constants.NOTIFICATION_FREQUENCY_OPTIONS
                        ) ?: "Disabled",
                        selectedTheme = theme,
                        hasWallpaperPermission = permissions[Manifest.permission.SET_WALLPAPER] == true,
                        hasNotificationPermission = checkNotificationPermission()
                    )
                }
            }.onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    Log.e("SettingsViewModel", "Error loading settings", e)
                }
                .collect()
        }
    }

    /**
     * Refresh permission status when the screen is resumed.
     */
    fun refreshPermissions() {
        _uiState.update {
            it.copy(
                hasWallpaperPermission = checkWallpaperPermission(),
                hasNotificationPermission = checkNotificationPermission()
            )
        }
    }

    private fun checkWallpaperPermission(): Boolean {
        // Wallpaper permission is implicitly granted on older APIs.
        // On newer APIs, we don't need a specific runtime permission,
        // but we can check if the user has disabled the app's ability to set it.
        // For this app's purpose, we'll consider it "granted" unless a specific issue arises.
        return true // Simplified for now
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Granted by default on older APIs
        }
    }

    fun onHabitChanged(newHabit: String) {
        Log.d("SettingsViewModel", "Habit changed to: $newHabit")
        viewModelScope.launch {
            settingsRepository.saveHabitSetting(newHabit)
            scheduleWallpaperWorkerUseCase()
            // No need to call loadAndCheckSettings, as the flow will automatically emit the new value
        }
    }

    fun onWallpaperFrequencyChanged(newFrequencyMillis: Long) {
        Log.d("SettingsViewModel", "Wallpaper frequency changed to: $newFrequencyMillis ms")
        viewModelScope.launch {
            settingsRepository.saveWallpaperFrequency(newFrequencyMillis)
            scheduleWallpaperWorkerUseCase()
        }
    }

    fun onNotificationFrequencyChanged(newFrequencyMillis: Long) {
        Log.d("SettingsViewModel", "Notification frequency changed to: $newFrequencyMillis ms")
        viewModelScope.launch {
            settingsRepository.saveNotificationFrequency(newFrequencyMillis)
            scheduleNotificationWorkerUseCase()
        }
    }

    fun onStopAllMotivation() {
        Log.d("SettingsViewModel", "Stopping all workers.")
        cancelAllWorkersUseCase()
    }

    private fun getFrequencyDisplayName(frequencyMillis: Long?, availableFrequencies: List<Pair<String, Long>>): String? {
        return availableFrequencies.find { it.second == frequencyMillis }?.first
    }

    /**
     * Formats the stored habit string (e.g., "stop_smoking") into a display-friendly
     * version (e.g., "Stop Smoking").
     */
    private fun String.formatHabitDisplayName(): String {
        return this.split('_').joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }

    fun onThemeChanged(theme: String) {
        viewModelScope.launch {
            settingsRepository.saveThemePreference(theme)
        }
    }
}