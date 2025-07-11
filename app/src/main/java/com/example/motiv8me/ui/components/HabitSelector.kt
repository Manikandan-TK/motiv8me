package com.example.motiv8me.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.motiv8me.ui.theme.Motiv8MeTheme

/**
 * A dropdown menu composable for selecting a predefined habit.
 *
 * @param availableHabits The list of habit pairs (Display Name, Key) to show.
 * @param selectedHabitKey The key of the currently selected habit, or null.
 * @param onHabitSelected Callback invoked with the selected habit's key.
 * @param modifier Optional Modifier for this composable.
 * @param placeholder Text to display when no habit is selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitSelector(
    availableHabits: List<Pair<String, String>>,
    selectedHabitKey: String?,
    onHabitSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select Habit"
) {
    var expanded by remember { mutableStateOf(false) }

    // Find the display name for the currently selected key
    val selectedDisplayName = remember(selectedHabitKey, availableHabits) {
        availableHabits.find { it.second == selectedHabitKey }?.first
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedDisplayName ?: placeholder,
            onValueChange = {},
            readOnly = true,
            label = { Text("Habit") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableHabits.forEach { (displayName, key) ->
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        onHabitSelected(key) // Return the key
                        expanded = false
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 300)
@Composable
private fun HabitSelectorPreview() {
    var selectedKey by remember { mutableStateOf<String?>(null) }
    val habits = listOf(
        Pair("Stop Smoking", "stop_smoking"),
        Pair("Eat Healthier", "eat_healthier")
    )
    Motiv8MeTheme {
        HabitSelector(
            availableHabits = habits,
            selectedHabitKey = selectedKey,
            onHabitSelected = { selectedKey = it }
        )
    }
}