package com.example.motiv8me.ui.features.onboarding

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.domain.usecase.ScheduleNotificationWorkerUseCase
import com.example.motiv8me.domain.usecase.ScheduleWallpaperWorkerUseCase
import com.example.motiv8me.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentPage: Int = 0,
    val totalPages: Int = 5,
    val selectedHabitKey: String? = null,
    val selectedWallpaperFrequencyMinutes: Long? = null,
    val selectedNotificationFrequencyMinutes: Long? = null,
    val availableHabits: List<Pair<String, String>> = emptyList(), // Display Name, Key
    val availableFrequencies: List<Pair<String, Long>> = emptyList(),
    val isWallpaperPermissionGranted: Boolean = false,
    val isNotificationPermissionGranted: Boolean = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) true else false,
    val isLoading: Boolean = false
) {
    val canCompleteOnboarding: Boolean
        get() = selectedHabitKey != null &&
                selectedWallpaperFrequencyMinutes != null &&
                selectedNotificationFrequencyMinutes != null &&
                isWallpaperPermissionGranted &&
                isNotificationPermissionGranted
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val applicationContext: Context,
    private val scheduleWallpaperWorkerUseCase: ScheduleWallpaperWorkerUseCase,
    private val scheduleNotificationWorkerUseCase: ScheduleNotificationWorkerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                availableHabits = Constants.HABIT_OPTIONS,
                availableFrequencies = Constants.SHARED_APP_FREQUENCIES
            )
        }
    }

    fun onHabitSelected(habitKey: String) {
        _uiState.update { it.copy(selectedHabitKey = habitKey) }
    }

    fun onWallpaperFrequencySelected(frequencyMinutes: Long) {
        _uiState.update { it.copy(selectedWallpaperFrequencyMinutes = frequencyMinutes) }
    }

    fun onNotificationFrequencySelected(frequencyMinutes: Long) {
        _uiState.update { it.copy(selectedNotificationFrequencyMinutes = frequencyMinutes) }
    }

    fun onNotificationPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(isNotificationPermissionGranted = isGranted) }
    }

    fun setInitialWallpaper() {
        viewModelScope.launch {
            val habitKey = _uiState.value.selectedHabitKey
            if (habitKey == null) {
                Log.e("OnboardingViewModel", "Cannot set wallpaper, habit not selected.")
                return@launch
            }
            try {
                val imageList = Constants.HABIT_TO_IMAGE_MAP[habitKey]
                if (!imageList.isNullOrEmpty()) {
                    val wallpaperResourceId = imageList.random()
                    val bitmap = BitmapFactory.decodeResource(applicationContext.resources, wallpaperResourceId)
                    WallpaperManager.getInstance(applicationContext).setBitmap(bitmap)
                    _uiState.update { it.copy(isWallpaperPermissionGranted = true) }
                    Log.i("OnboardingViewModel", "Initial wallpaper set for $habitKey")
                }
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Failed to set initial wallpaper", e)
            }
        }
    }

    fun onNextClicked() {
        _uiState.update {
            val nextPage = (it.currentPage + 1).coerceAtMost(it.totalPages - 1)
            it.copy(currentPage = nextPage)
        }
    }

    fun onBackClicked() {
        _uiState.update {
            val prevPage = (it.currentPage - 1).coerceAtLeast(0)
            it.copy(currentPage = prevPage)
        }
    }

    fun setCurrentPage(page: Int) {
        _uiState.update { it.copy(currentPage = page.coerceIn(0, it.totalPages - 1)) }
    }

    fun saveOnboardingSelections() {
        val currentState = _uiState.value
        if (currentState.canCompleteOnboarding) {
            viewModelScope.launch {
                settingsRepository.saveHabitSetting(currentState.selectedHabitKey!!)
                settingsRepository.saveWallpaperFrequency(currentState.selectedWallpaperFrequencyMinutes!!)
                settingsRepository.saveNotificationFrequency(currentState.selectedNotificationFrequencyMinutes!!)
                settingsRepository.saveOnboardingComplete(true)

                // CORRECTLY schedule the periodic workers using the use cases
                scheduleWallpaperWorkerUseCase()
                scheduleNotificationWorkerUseCase()
            }
        }
    }
}