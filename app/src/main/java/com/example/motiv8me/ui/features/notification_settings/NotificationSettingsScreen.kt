package com.example.motiv8me.ui.features.notification_settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.motiv8me.R
import com.example.motiv8me.ui.navigation.ThemeToggleActions
import com.example.motiv8me.ui.theme.Motiv8MeTheme
import java.util.concurrent.TimeUnit

/**
 * Screen for selecting the notification frequency.
 *
 * @param onNavigateBack Callback invoked when the user triggers back navigation.
 * @param viewModel The ViewModel associated with this screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notif_settings_title)) }, // TODO: Add string
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back)
                        )
                    }
                },
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
            // Use selectableGroup for accessibility with radio buttons
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .selectableGroup() // Group radio buttons semantically
            ) {
                Text(
                    text = stringResource(R.string.notif_settings_prompt), // TODO: Add string
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Iterate through the available frequencies from the state
                uiState.availableFrequencies.entries.forEach { (displayName, frequencyMillis) ->
                    FrequencyOptionRow(
                        text = displayName,
                        selected = uiState.currentFrequencyMillis == frequencyMillis,
                        onClick = { viewModel.onFrequencySelected(frequencyMillis) }
                    )
                }
            }
        }
    }
}

/**
 * Composable for a single row containing a RadioButton and a label.
 *
 * @param text The label text for the option.
 * @param selected Whether this option is currently selected.
 * @param onClick Lambda function invoked when the row is clicked.
 */
@Composable
private fun FrequencyOptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp) // Standard height for clickable rows
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton // Accessibility role
            )
            .padding(horizontal = 16.dp), // Padding within the row
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // Click is handled by the Row's selectable modifier
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

// --- Preview ---
@Preview(showBackground = true)
@Composable
private fun NotificationSettingsScreenPreview() {
    Motiv8MeTheme {
        // Preview won't have a real ViewModel, showing default state
        NotificationSettingsScreen(onNavigateBack = {})
    }
}

