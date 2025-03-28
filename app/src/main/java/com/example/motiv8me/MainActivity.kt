package com.example.motiv8me // Using a standard package structure

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// Make sure this import path is correct for YOUR project structure
import com.example.motiv8me.ui.theme.Motiv8MeTheme

// Import the data class (assuming it's in the same package)
// If Habit.kt is in a different package, adjust this import.
import com.example.motiv8me.Habit


class MainActivity : ComponentActivity() {

    companion object {
        // Use package name in constant for better uniqueness
        const val EXTRA_SELECTED_HABIT = "com.example.motiv8me.SELECTED_HABIT"
    }

    private val habits = listOf(
        // Ensure Habit class is accessible here (correct package/import)
        Habit(name = "Quit Smoking", folderName = "quit_smoking"),
        Habit(name = "Stop Procrastinating", folderName = "stop_procrastinating"),
        Habit(name = "Less Screen Time", folderName = "less_screen_time"),
        Habit(name = "Quit Snacking", folderName = "quit_snacking")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // This requires Motiv8MeTheme to be correctly defined and imported
            Motiv8MeTheme {
                HabitPickerScreen(
                    habits = habits,
                    onNavigateToSettings = { selectedHabit ->
                        navigateToSettings(selectedHabit)
                    }
                )
            }
        }
    }

    private fun navigateToSettings(habit: Habit) {
        // This line will show an error until SettingsActivity.kt is created
        val intent = Intent(this, SettingsActivity::class.java).apply {
            putExtra(EXTRA_SELECTED_HABIT, habit)
        }
        startActivity(intent)
    }
}

// --- Composables ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitPickerScreen(
    habits: List<Habit>,
    onNavigateToSettings: (Habit) -> Unit
) {
    var selectedHabit by remember { mutableStateOf<Habit?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.habit_picker_title),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(habits) { habit ->
                        HabitItem(
                            habit = habit,
                            isSelected = habit == selectedHabit,
                            onHabitSelected = { selectedHabit = habit }
                        )
                        // Use HorizontalDivider instead of Divider
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp), // Optional padding
                            thickness = 1.dp, // Default thickness
                            color = MaterialTheme.colorScheme.outlineVariant // Default color
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (selectedHabit != null) {
                            onNavigateToSettings(selectedHabit!!)
                        } else {
                            Toast.makeText(
                                context,
                                R.string.error_no_habit_selected,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedHabit != null // Optionally disable button if nothing selected
                ) {
                    Text(stringResource(id = R.string.next_button_text))
                }
            }
        }
    )
}

@Composable
fun HabitItem(
    habit: Habit,
    isSelected: Boolean,
    onHabitSelected: (Habit) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHabitSelected(habit) }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onHabitSelected(habit) },
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = habit.name,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// --- Preview ---

@Preview(showBackground = true)
@Composable
fun DefaultPreviewMainActivity() {
    val previewHabits = listOf(
        Habit(name = "Quit Smoking", folderName = "quit_smoking"),
        Habit(name = "Stop Procrastinating", folderName = "stop_procrastinating")
    )
    // This requires Motiv8MeTheme to be correctly defined and imported
    Motiv8MeTheme {
        HabitPickerScreen(habits = previewHabits, onNavigateToSettings = {})
    }
}