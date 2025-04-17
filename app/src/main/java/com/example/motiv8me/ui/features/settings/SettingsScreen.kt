package com.example.motiv8me.ui.features.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.motiv8me.R
import com.example.motiv8me.ui.components.FrequencySelector
import com.example.motiv8me.ui.navigation.ThemeToggleActions
import com.example.motiv8me.ui.theme.Motiv8MeTheme
import com.example.motiv8me.util.PermissionUtils

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
    val context = LocalContext.current
    // Defensive: Check permissions
    val wallpaperPermissionGranted = PermissionUtils.hasWallpaperPermission(context)
    val notificationPermissionGranted = PermissionUtils.hasNotificationPermission(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                actions = {
                    ThemeToggleActions()
                }
            )
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
                // --- Permission Status Section ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_permissions_status_title),
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.tertiary)
                        )
                        Spacer(Modifier.height(8.dp))
                        PermissionStatusRow(
                            label = stringResource(R.string.permission_wallpaper),
                            granted = wallpaperPermissionGranted,
                            onFix = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = android.net.Uri.parse("package:" + context.packageName)
                                context.startActivity(intent)
                            }
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            PermissionStatusRow(
                                label = stringResource(R.string.permission_notifications),
                                granted = notificationPermissionGranted,
                                onFix = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    intent.data = android.net.Uri.parse("package:" + context.packageName)
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }

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

@Composable
private fun PermissionStatusRow(label: String, granted: Boolean, onFix: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        if (granted) {
            Text(stringResource(id = R.string.permission_granted), color = Color(0xFF388E3C))
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                TextButton(onClick = onFix) {
                    Text(stringResource(id = R.string.open_settings), color = Color(0xFFD32F2F))
                }
            }
        }
    }
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
