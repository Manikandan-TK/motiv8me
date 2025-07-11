package com.example.motiv8me.ui.features.habit_selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.motiv8me.R
import com.example.motiv8me.ui.navigation.ThemeToggleActions
import com.example.motiv8me.ui.theme.Motiv8MeTheme
import com.example.motiv8me.util.Constants // CORRECTED: Import from util

/**
 * Composable screen for selecting a predefined habit from a list.
 *
 * @param onHabitSelected Callback invoked with the selected habit's key (String).
 * @param onNavigateBack Callback invoked when the user triggers back navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitSelectionScreen(
    onHabitSelected: (habitKey: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    // CORRECTED: Retrieve the list of pairs (DisplayName, Key) from Constants
    val habits = Constants.HABIT_OPTIONS

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Habit") },
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
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // CORRECTED: Iterate over the list of pairs, using the key for stability
            items(items = habits, key = { it.second }) { (displayName, habitKey) ->
                HabitListItem(
                    habitName = displayName, // Show the display name
                    onClick = {
                        onHabitSelected(habitKey) // Pass the key back on click
                    }
                )
                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}

/**
 * Composable representing a single item in the habit selection list.
 */
@Composable
private fun HabitListItem(
    habitName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = habitName,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun HabitSelectionScreenPreview() {
    Motiv8MeTheme {
        HabitSelectionScreen(
            onHabitSelected = { },
            onNavigateBack = { }
        )
    }
}