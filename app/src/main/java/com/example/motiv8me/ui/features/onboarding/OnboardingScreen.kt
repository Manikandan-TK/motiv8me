package com.example.motiv8me.ui.features.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.motiv8me.R
import com.example.motiv8me.ui.components.FrequencySelector
import com.example.motiv8me.ui.components.HabitSelector
import com.example.motiv8me.ui.navigation.ThemeToggleActions
import com.example.motiv8me.ui.theme.Motiv8MeTheme
import com.example.motiv8me.util.PermissionUtils
import com.example.motiv8me.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.onNotificationPermissionResult(isGranted)
        }
    )
    var showNotificationPermissionRationale by remember { mutableStateOf(false) }
    var showWallpaperPermissionRationale by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.onboarding_title)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                actions = {
                    ThemeToggleActions()
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (uiState.isLoading) {
                Spacer(Modifier.height(32.dp))
                CircularProgressIndicator()
                Spacer(Modifier.weight(1f))
            } else {
                // --- Welcome & Illustration ---
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp)
                )
                Text(
                    text = stringResource(id = R.string.onboarding_welcome),
                    style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.secondary),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(id = R.string.onboarding_explanation),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                // --- Step 1: Habit Selection ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(id = R.string.onboarding_select_habit_title),
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary)
                        )
                        Spacer(Modifier.height(8.dp))
                        HabitSelector(
                            availableHabits = uiState.availableHabits,
                            selectedHabit = uiState.selectedHabit,
                            onHabitSelected = viewModel::onHabitSelected,
                            placeholder = stringResource(id = R.string.onboarding_select_habit_button)
                        )
                    }
                }
                // --- Step 2: Wallpaper Frequency Selection ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(id = R.string.onboarding_select_frequency_title),
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary)
                        )
                        Spacer(Modifier.height(8.dp))
                        FrequencySelector(
                            availableFrequencies = uiState.availableFrequencies,
                            selectedFrequencyMillis = uiState.selectedFrequencyMillis,
                            onFrequencySelected = viewModel::onFrequencySelected,
                            placeholder = stringResource(id = R.string.onboarding_select_frequency_button)
                        )
                    }
                }
                // --- Step 3: Notification Frequency Selection ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(id = R.string.onboarding_select_notification_frequency_title),
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.tertiary)
                        )
                        Spacer(Modifier.height(8.dp))
                        FrequencySelector(
                            availableFrequencies = Constants.NOTIFICATION_FREQUENCIES,
                            selectedFrequencyMillis = uiState.selectedNotificationFrequencyMillis,
                            onFrequencySelected = viewModel::onNotificationFrequencySelected,
                            placeholder = stringResource(id = R.string.onboarding_select_notification_frequency_button)
                        )
                    }
                }
                // --- Step 4: Permissions ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(id = R.string.onboarding_permissions_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(id = R.string.onboarding_permissions_explanation),
                            style = MaterialTheme.typography.bodySmall
                        )
                        PermissionStatus(
                            permissionName = stringResource(id = R.string.permission_wallpaper),
                            isGranted = uiState.isWallpaperPermissionGranted
                        ) {
                            showWallpaperPermissionRationale = true
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            PermissionStatus(
                                permissionName = stringResource(id = R.string.permission_notifications),
                                isGranted = uiState.isNotificationPermissionGranted
                            ) {
                                if (!uiState.isNotificationPermissionGranted) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        }
                    }
                }
                // --- Permission Error Dialogs ---
                if (showNotificationPermissionRationale) {
                    AlertDialog(
                        onDismissRequest = { showNotificationPermissionRationale = false },
                        title = { Text(stringResource(id = R.string.permission_notifications_denied_title)) },
                        text = { Text(stringResource(id = R.string.permission_notifications_denied_message)) },
                        confirmButton = {
                            TextButton(onClick = {
                                showNotificationPermissionRationale = false
                                PermissionUtils.openAppSettings(context)
                            }) { Text(stringResource(id = R.string.open_settings)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showNotificationPermissionRationale = false }) {
                                Text(stringResource(id = R.string.dismiss))
                            }
                        }
                    )
                }
                if (showWallpaperPermissionRationale) {
                    AlertDialog(
                        onDismissRequest = { showWallpaperPermissionRationale = false },
                        title = { Text(stringResource(id = R.string.permission_wallpaper_denied_title)) },
                        text = { Text(stringResource(id = R.string.permission_wallpaper_denied_message)) },
                        confirmButton = {
                            TextButton(onClick = {
                                showWallpaperPermissionRationale = false
                                PermissionUtils.openAppSettings(context)
                            }) { Text(stringResource(id = R.string.open_settings)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showWallpaperPermissionRationale = false }) {
                                Text(stringResource(id = R.string.dismiss))
                            }
                        }
                    )
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        viewModel.saveOnboardingSelections()
                        onOnboardingComplete()
                    },
                    enabled = uiState.canCompleteOnboarding,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.onboarding_finish_button))
                }
            }
        }
    }
}

/**
 * Simple composable to display the status of a permission.
 */
@Composable
private fun PermissionStatus(
    permissionName: String,
    isGranted: Boolean,
    onRequestPermission: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(permissionName, style = MaterialTheme.typography.bodyMedium)
        if (isGranted) {
            Text(
                stringResource(id = R.string.permission_granted),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Button(onClick = onRequestPermission) {
                Text(stringResource(id = R.string.permission_grant_button))
            }
        }
    }
}

// --- Preview ---
@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    Motiv8MeTheme {
        OnboardingScreen(onOnboardingComplete = {})
    }
}