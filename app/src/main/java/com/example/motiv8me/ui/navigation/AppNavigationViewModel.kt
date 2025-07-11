package com.example.motiv8me.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motiv8me.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel specifically used by AppNavigation to determine the correct
 * starting destination based on the onboarding completion status.
 */
@HiltViewModel
class AppNavigationViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository // Inject repository
) : ViewModel() {

    // Holds the calculated start destination route. Starts as null.
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        // Launch a coroutine to check the onboarding status from the repository
        viewModelScope.launch {
            // Get the settings flow, map it to just the onboarding status,
            // and take the first emitted value.
            val isOnboardingComplete = settingsRepository.getSettings()
                .map { it.isOnboardingComplete }
                .first() // We only need the initial status here

            // Update the StateFlow with the determined start route
            _startDestination.value = if (isOnboardingComplete) {
                // CORRECTED: User has completed onboarding, start at the Home screen.
                ScreenDestinations.Home.route
            } else {
                // User needs to complete onboarding first
                ScreenDestinations.Onboarding.route
            }
        }
    }
}