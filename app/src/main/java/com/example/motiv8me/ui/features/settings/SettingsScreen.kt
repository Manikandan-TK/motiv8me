package com.example.motiv8me.ui.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.ArrowForwardIos // Remove this import
import androidx.compose.material.icons.filled.KeyboardArrowRight // Replace ChevronRight with KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
// import androidx.compose.runtime.LaunchedEffect // Keep if using SavedStateHandle approach later
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.motiv8me.R
import com.example.motiv8me.ui.components.FrequencySelector
import com.example.motiv8me.ui.theme.Motiv8MeTheme

/**
 * Main settings screen composable. Allows users to view and modify app preferences.
 *
 * @param onNavigateToHabitSelection Callback to navigate to the habit selection screen.
 * @param onNavigateToNotificationSettings Callback to navigate to the notification settings screen.
 * @param viewModel The ViewModel associated with this screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToHabitSelection: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) })
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- Habit Setting ---
                SettingItemRow(
                    title = stringResource(R.string.settings_habit_title),
                    currentValue = uiState.currentHabit ?: stringResource(R.string.settings_habit_not_set),
                    onClick = onNavigateToHabitSelection
                )

                Divider()

                // --- Wallpaper Frequency Setting ---
                SettingItemRow(
                    title = stringResource(R.string.settings_wallpaper_frequency_title),
                    onClick = null // Click handled by selector
                ) { // Content slot for the selector
                    FrequencySelector(
                        availableFrequencies = uiState.availableWallpaperFrequencies,
                        selectedFrequencyMillis = uiState.currentWallpaperFrequencyMillis,
                        onFrequencySelected = viewModel::onWallpaperFrequencyChanged,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp)
                    )
                }

                Divider()

                // --- Notification Settings Navigation ---
                SettingItemRow(
                    title = stringResource(R.string.settings_notification_title),
                    currentValue = viewModel.getFrequencyDisplayName(
                        uiState.currentNotificationFrequencyMillis,
                        uiState.availableNotificationFrequencies
                    ) ?: stringResource(R.string.settings_notification_off),
                    onClick = onNavigateToNotificationSettings
                )

                Divider()

                // --- Optional: Placeholder for Pro Features ---
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.settings_pro_features_info),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

            } // End Column
        } // End else (not loading)
    } // End Scaffold
}

/**
 * Reusable composable for displaying a setting item row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingItemRow(
    title: String,
    currentValue: String? = null,
    onClick: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    val rowModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = if (currentValue != null && content == null) {
            { Text(currentValue) }
        } else null,
        trailingContent = if (onClick != null && content == null) {
            // Use KeyboardArrowRight instead of ChevronRight
            { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null,
        modifier = rowModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )

    if (content != null) {
        Box(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

// --- Preview ---
@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    Motiv8MeTheme {
        SettingsScreen(
            onNavigateToHabitSelection = {},
            onNavigateToNotificationSettings = {}
        )
    }
}
