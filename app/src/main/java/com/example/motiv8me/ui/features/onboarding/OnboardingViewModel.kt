package com.example.motiv8me.ui.features.onboarding

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motiv8me.R
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.motiv8me.service.NotificationWorker
import com.example.motiv8me.service.WallpaperWorker

data class OnboardingUiState(
    // Page state
    val currentPage: Int = 0,
    val totalPages: Int = 5, // Welcome, Habit, Wallpaper Freq, Notif Freq, Permissions

    // Selections
    val selectedHabit: String? = null,
    val selectedWallpaperFrequency: Long? = null,
    val selectedNotificationFrequencyMillis: Long? = null,
    val availableHabits: List<String> = emptyList(),
    val availableFrequencies: List<Pair<String, Long>> = emptyList(),

    // Permissions
    val isWallpaperPermissionGranted: Boolean = false,
    val isNotificationPermissionGranted: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) false else true,
    val isLoading: Boolean = false
) {
    val canCompleteOnboarding: Boolean
        get() = selectedHabit != null &&
                selectedWallpaperFrequency != null &&
                selectedNotificationFrequencyMillis != null &&
                isWallpaperPermissionGranted &&
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) isNotificationPermissionGranted else true)
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _uiState.update {
            it.copy(
                availableHabits = Constants.PREDEFINED_HABITS,
                availableFrequencies = Constants.SHARED_APP_FREQUENCIES
            )
        }
    }

    fun onHabitSelected(habit: String) {
        _uiState.update { it.copy(selectedHabit = habit) }
    }

    fun onWallpaperFrequencySelected(frequencyMillis: Long) {
        _uiState.update { it.copy(selectedWallpaperFrequency = frequencyMillis) }
    }

    fun onNotificationFrequencySelected(frequencyMillis: Long) {
        _uiState.update { it.copy(selectedNotificationFrequencyMillis = frequencyMillis) }
    }

    fun onNotificationPermissionResult(isGranted: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            _uiState.update { it.copy(isNotificationPermissionGranted = isGranted) }
        }
    }

    fun setInitialWallpaper() {
        viewModelScope.launch {
            val habit = _uiState.value.selectedHabit
            if (habit == null) {
                Log.e("OnboardingViewModel", "Cannot set wallpaper, habit not selected.")
                // Optionally update UI to inform user they need to select a habit first
                // _uiState.update { it.copy(wallpaperSettingError = "Please select a habit first.") }
                return@launch
            }
            Log.d("OnboardingViewModel", "Attempting to set wallpaper for habit: $habit")

            try {
                val imageList = Constants.HABIT_TO_IMAGE_MAP[habit] ?: run {
                    Log.e("OnboardingViewModel", "No wallpaper list defined for habit: $habit")
                    emptyList()
                }

                if (imageList.isNotEmpty()) {
                    val wallpaperResourceId = imageList.random()
                    Log.d("OnboardingViewModel", "Selected wallpaper resource ID: $wallpaperResourceId")

                    val bitmap = BitmapFactory.decodeResource(applicationContext.resources, wallpaperResourceId)
                    if (bitmap == null) {
                        Log.e("OnboardingViewModel", "Failed to decode bitmap for resource ID: $wallpaperResourceId")
                        // Optionally update UI with error
                        // _uiState.update { it.copy(wallpaperSettingError = "Error loading wallpaper image.") }
                        return@launch
                    }
                    Log.d("OnboardingViewModel", "Bitmap decoded successfully.")

                    WallpaperManager.getInstance(applicationContext).setBitmap(bitmap)
                    Log.i("OnboardingViewModel", "Wallpaper successfully set for habit: $habit")

                    // Update the state to reflect that the wallpaper has been set.
                    _uiState.update { it.copy(isWallpaperPermissionGranted = true) }
                } else {
                    Log.e("OnboardingViewModel", "No wallpapers found for habit: $habit after when block.")
                    // Optionally update UI with error
                    // _uiState.update { it.copy(wallpaperSettingError = "No wallpapers for selected habit.") }
                }
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Failed to set initial wallpaper due to an exception.", e)
                // Optionally update UI with error
                // _uiState.update { it.copy(isWallpaperPermissionGranted = false, wallpaperSettingError = "Could not set wallpaper.") }
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
                settingsRepository.saveHabitSetting(currentState.selectedHabit!!)
                settingsRepository.saveWallpaperFrequency(currentState.selectedWallpaperFrequency!!)
                settingsRepository.saveNotificationFrequency(currentState.selectedNotificationFrequencyMillis!!)
                settingsRepository.saveOnboardingComplete(true)
                // Enqueue the workers to start their work based on the saved settings
                WorkManager.getInstance(applicationContext).enqueue(OneTimeWorkRequestBuilder<WallpaperWorker>().build())
                WorkManager.getInstance(applicationContext).enqueue(OneTimeWorkRequestBuilder<NotificationWorker>().build())
            }
        }
    }
}
