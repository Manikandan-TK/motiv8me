package com.example.motiv8me.ui.features.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motiv8me.domain.permission.PermissionManager
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.domain.usecase.CancelAllWorkersUseCase
import com.example.motiv8me.domain.usecase.ScheduleNotificationWorkerUseCase
import com.example.motiv8me.domain.usecase.ScheduleWallpaperWorkerUseCase
import com.example.motiv8me.domain.usecase.SetWallpaperOnceUseCase // CORRECTED: Import the new use case
import com.example.motiv8me.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val currentHabit: String = "Not Set",
    val wallpaperFrequencyMinutes: Long? = null,
    val wallpaperFrequencyDisplayName: String = "Not Set",
    val notificationFrequencyDisplayName: String = "Disabled",
    val hasWallpaperPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val availableWallpaperFrequencies: List<Pair<String, Long>> = Constants.SHARED_APP_FREQUENCIES,
    val selectedTheme: String = "System",
    val availableThemes: List<String> = listOf("Light", "Dark", "System")
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val scheduleWallpaperWorkerUseCase: ScheduleWallpaperWorkerUseCase,
    private val scheduleNotificationWorkerUseCase: ScheduleNotificationWorkerUseCase,
    private val cancelAllWorkersUseCase: CancelAllWorkersUseCase,
    private val permissionManager: PermissionManager,
    private val setWallpaperOnceUseCase: SetWallpaperOnceUseCase // CORRECTED: Inject the new use case
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadAndCheckSettings()
    }

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
                        currentHabit = getHabitDisplayName(appSettings.selectedHabit) ?: "Not Set",
                        wallpaperFrequencyMinutes = appSettings.wallpaperFrequencyMinutes,
                        wallpaperFrequencyDisplayName = getFrequencyDisplayName(
                            appSettings.wallpaperFrequencyMinutes,
                            Constants.SHARED_APP_FREQUENCIES
                        ) ?: "Not Set",
                        notificationFrequencyDisplayName = getFrequencyDisplayName(
                            appSettings.notificationFrequencyMinutes,
                            Constants.SHARED_APP_FREQUENCIES
                        ) ?: "Disabled",
                        selectedTheme = theme,
                        hasWallpaperPermission = true, // Simplified check
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

    fun refreshPermissions() {
        _uiState.update {
            it.copy(hasNotificationPermission = checkNotificationPermission())
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun onHabitChanged(newHabitKey: String) {
        viewModelScope.launch {
            // Save the new habit setting
            settingsRepository.saveHabitSetting(newHabitKey)

            // CORRECTED: Immediately set the wallpaper to give the user instant feedback.
            setWallpaperOnceUseCase()

            // Reschedule the periodic worker for all future wallpaper changes.
            scheduleWallpaperWorkerUseCase()
        }
    }

    fun onWallpaperFrequencyChanged(newFrequencyMinutes: Long) {
        viewModelScope.launch {
            settingsRepository.saveWallpaperFrequency(newFrequencyMinutes)
            scheduleWallpaperWorkerUseCase()
        }
    }

    fun onThemeChanged(theme: String) {
        viewModelScope.launch {
            settingsRepository.saveThemePreference(theme)
        }
    }

    private fun getFrequencyDisplayName(frequencyMinutes: Long?, availableFrequencies: List<Pair<String, Long>>): String? {
        return availableFrequencies.find { it.second == frequencyMinutes }?.first
    }

    private fun getHabitDisplayName(habitKey: String?): String? {
        return Constants.HABIT_OPTIONS.find { it.second == habitKey }?.first
    }
}