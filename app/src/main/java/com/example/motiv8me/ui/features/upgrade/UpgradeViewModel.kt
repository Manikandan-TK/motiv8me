package com.example.motiv8me.ui.features.upgrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motiv8me.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Upgrade screen.
 * @param isProUser Flag indicating if the user already has premium features.
 * @param isLoading Flag to show a loading indicator while fetching the status.
 */
data class UpgradeUiState(
    val isLoading: Boolean = true,
    val isProUser: Boolean = false
)

/**
 * Sealed interface for events sent from the ViewModel to the UI.
 */
sealed interface UpgradeEvent {
    data object UpgradeSuccessful : UpgradeEvent
}

@HiltViewModel
class UpgradeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpgradeUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UpgradeEvent>()
    val events = _events.asSharedFlow()

    init {
        // Observe the settings to know the current pro status
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isProUser = settings.isProUser
                    )
                }
            }
        }
    }

    /**
     * Sets the user's status to "Pro" and triggers a success event.
     */
    fun unlockProFeatures() {
        viewModelScope.launch {
            settingsRepository.saveIsProUser(true)
            // Send an event to the UI to notify it of the successful upgrade
            _events.emit(UpgradeEvent.UpgradeSuccessful)
        }
    }
}