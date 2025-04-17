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
import com.example.motiv8me.R // For string resources if needed
import com.example.motiv8me.ui.navigation.ThemeToggleActions
import com.example.motiv8me.ui.theme.Motiv8MeTheme
import com.example.motiv8me.util.Constants

/**
 * Composable screen for selecting a predefined habit from a list.
 *
 * @param onHabitSelected Callback invoked with the selected habit name (String) when a habit is chosen.
 * @param onNavigateBack Callback invoked when the user triggers back navigation (e.g., taps the back arrow).
 */
@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar
@Composable
fun HabitSelectionScreen(
    onHabitSelected: (habit: String) -> Unit,
    onNavigateBack: () -> Unit
    // Optional: Inject ViewModel if state becomes more complex
    // viewModel: HabitSelectionViewModel = hiltViewModel()
) {
    // Retrieve the predefined habits directly from Constants
    val habits = Constants.PREDEFINED_HABITS

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Habit") }, // TODO: Use stringResource
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back) // TODO: Add content description string
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
            modifier = Modifier.padding(paddingValues), // Apply padding from Scaffold
            contentPadding = PaddingValues(vertical = 8.dp) // Add some padding around the list items
        ) {
            items(items = habits, key = { it }) { habit ->
                HabitListItem(
                    habitName = habit,
                    onClick = {
                        onHabitSelected(habit)
                        // Optionally navigate back immediately after selection,
                        // or let the calling screen handle it based on the callback.
                        // onNavigateBack() // Uncomment if immediate back navigation is desired
                    }
                )
                Divider(thickness = 0.5.dp) // Add a thin divider between items
            }
        }
    }
}

/**
 * Composable representing a single item in the habit selection list.
 *
 * @param habitName The name of the habit to display.
 * @param onClick Lambda function to be invoked when the item is clicked.
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
            .clickable(onClick = onClick) // Make the whole row clickable
            .padding(horizontal = 16.dp, vertical = 16.dp) // Add padding within the item
    )
}

// --- Preview ---
@Preview(showBackground = true)
@Composable
private fun HabitSelectionScreenPreview() {
    Motiv8MeTheme {
        HabitSelectionScreen(
            onHabitSelected = { println("Selected: $it") }, // Simulate selection
            onNavigateBack = { println("Navigate Back") } // Simulate back navigation
        )
    }
}

// TODO: Add string resource for content description
// In res/values/strings.xml:
// <string name="cd_navigate_back">Navigate back</string>
// <string name="habit_selection_title">Select Habit</string> // Optional title string