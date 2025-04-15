package com.example.motiv8me.ui.features.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.* // Keep general M3 import
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.platform.LocalContext // Removed unused import
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // Added Hilt VM import
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.motiv8me.R
import com.example.motiv8me.ui.components.FrequencySelector
import com.example.motiv8me.ui.components.HabitSelector
import com.example.motiv8me.ui.theme.Motiv8MeTheme
import com.example.motiv8me.util.PermissionUtils

/**
 * Composable function for the Onboarding screen.
 * Guides the user through initial setup: habit selection, frequency selection,
 * and granting necessary permissions. Now driven by OnboardingViewModel state.
 *
 * @param onOnboardingComplete Lambda function to be invoked when onboarding is successfully finished.
 * @param viewModel The ViewModel associated with this screen, provided by Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class) // Added OptIn for Scaffold/TopAppBar
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel() // Inject ViewModel using Hilt
) {
    // Collect the UI state from the ViewModel in a lifecycle-aware manner
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // val context = LocalContext.current // Removed unused context variable

    // Launcher for Notification Permission (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            // Notify the ViewModel about the permission result
            viewModel.onNotificationPermissionResult(isGranted)
            // TODO: Handle case where permission is permanently denied (show rationale/settings link)
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.onboarding_title)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Allow scrolling if content overflows
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp) // Spacing between elements
        ) {

            // Display loading indicator while initial checks run
            if (uiState.isLoading) {
                Spacer(Modifier.height(32.dp))
                CircularProgressIndicator()
                Spacer(Modifier.weight(1f))
            } else {
                // --- Welcome Text ---
                Text(
                    text = stringResource(id = R.string.onboarding_welcome),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(id = R.string.onboarding_explanation),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                // --- Step 1: Habit Selection ---
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(id = R.string.onboarding_select_habit_title),
                            style = MaterialTheme.typography.titleMedium
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

                // --- Step 2: Frequency Selection ---
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(id = R.string.onboarding_select_frequency_title),
                            style = MaterialTheme.typography.titleMedium
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

                // --- Step 3: Permissions ---
                Card(modifier = Modifier.fillMaxWidth()) {
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
                        ) { /* No action needed */ }

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

                Spacer(Modifier.weight(1f)) // Push button to bottom

                // --- Finish Button ---
                Button(
                    onClick = {
                        // 1. Save selections via ViewModel FIRST
                        viewModel.saveOnboardingSelections()
                        // 2. THEN navigate away
                        onOnboardingComplete()
                    },
                    enabled = uiState.canCompleteOnboarding,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.onboarding_finish_button))
                }
            } // End of else block (when not loading)
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
            // Removed size parameter from Button call
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