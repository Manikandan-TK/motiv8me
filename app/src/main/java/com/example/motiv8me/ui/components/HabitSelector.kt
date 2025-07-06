package com.example.motiv8me.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
// Required imports for the preview and component
import androidx.compose.foundation.layout.fillMaxWidth
import com.example.motiv8me.ui.theme.Motiv8MeTheme
import androidx.compose.material3.MenuAnchorType

/**
 * A dropdown menu composable for selecting a predefined habit.
 *
 * @param availableHabits The list of habit names (Strings) to display in the dropdown.
 * @param selectedHabit The currently selected habit name, or null if none is selected.
 * @param onHabitSelected Callback invoked when a habit is selected from the dropdown.
 * @param modifier Optional Modifier for this composable.
 * @param placeholder Text to display when no habit is selected.
 */
@OptIn(ExperimentalMaterial3Api::class) // For ExposedDropdownMenuBox
@Composable
fun HabitSelector(
    availableHabits: List<String>,
    selectedHabit: String?,
    onHabitSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select Habit" // Can be overridden or use stringResource
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField( // Using OutlinedTextField for better visual distinction
            value = selectedHabit ?: placeholder,
            onValueChange = {}, // No-op for read-only
            readOnly = true,
            label = { Text("Habit") }, // Optional label
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable) // Important: Anchor the dropdown menu to this text field
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableHabits.forEach { habit ->
                DropdownMenuItem(
                    text = { Text(habit) },
                    onClick = {
                        onHabitSelected(habit)
                        expanded = false
                    }
                )
            }
        }
    }
}

// --- Preview ---
@Preview(showBackground = true, widthDp = 300)
@Composable
private fun HabitSelectorPreview() {
    var selected by remember { mutableStateOf<String?>(null) }
    val habits = listOf("Stop Smoking", "Eat Healthier", "Exercise More", "Reduce Screen Time")
    Motiv8MeTheme {
        HabitSelector(
            availableHabits = habits,
            selectedHabit = selected,
            onHabitSelected = { selected = it }
        )
    }
}

@Preview(showBackground = true, widthDp = 300)
@Composable
private fun HabitSelectorSelectedPreview() {
    var selected by remember { mutableStateOf<String?>("Eat Healthier") }
    val habits = listOf("Stop Smoking", "Eat Healthier", "Exercise More", "Reduce Screen Time")
    Motiv8MeTheme {
        HabitSelector(
            availableHabits = habits,
            selectedHabit = selected,
            onHabitSelected = { selected = it }
        )
    }
}
