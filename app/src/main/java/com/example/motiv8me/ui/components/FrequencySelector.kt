package com.example.motiv8me.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
// Required imports for the preview and component
import androidx.compose.foundation.layout.fillMaxWidth
import com.example.motiv8me.ui.theme.Motiv8MeTheme
import java.util.concurrent.TimeUnit

/**
 * A dropdown menu composable for selecting a wallpaper change frequency.
 *
 * @param availableFrequencies A map where keys are user-friendly display names (e.g., "1 hour")
 *                             and values are the frequency duration in milliseconds (Long).
 * @param selectedFrequencyMillis The currently selected frequency in milliseconds, or null if none selected.
 * @param onFrequencySelected Callback invoked with the selected frequency value (Long) in milliseconds.
 * @param modifier Optional Modifier for this composable.
 * @param placeholder Text to display when no frequency is selected.
 */
@OptIn(ExperimentalMaterial3Api::class) // For ExposedDropdownMenuBox
@Composable
fun FrequencySelector(
    availableFrequencies: Map<String, Long>,
    selectedFrequencyMillis: Long?,
    onFrequencySelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select Frequency" // Can be overridden or use stringResource
) {
    var expanded by remember { mutableStateOf(false) }

    // Find the display name for the currently selected millisecond value
    val selectedDisplayName = remember(selectedFrequencyMillis, availableFrequencies) {
        availableFrequencies.entries.find { it.value == selectedFrequencyMillis }?.key
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField( // Using OutlinedTextField
            value = selectedDisplayName ?: placeholder,
            onValueChange = {}, // No-op for read-only
            readOnly = true,
            label = { Text("Frequency") }, // Optional label
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor() // Anchor the dropdown menu
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableFrequencies.entries.forEach { (displayName, frequencyMillis) ->
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        onFrequencySelected(frequencyMillis)
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
private fun FrequencySelectorPreview() {
    var selected by remember { mutableStateOf<Long?>(null) }
    val frequencies = mapOf(
        "30 minutes" to TimeUnit.MINUTES.toMillis(30),
        "1 hour" to TimeUnit.HOURS.toMillis(1),
        "3 hours" to TimeUnit.HOURS.toMillis(3),
        "Daily" to TimeUnit.DAYS.toMillis(1)
    )
    Motiv8MeTheme {
        FrequencySelector(
            availableFrequencies = frequencies,
            selectedFrequencyMillis = selected,
            onFrequencySelected = { selected = it }
        )
    }
}

@Preview(showBackground = true, widthDp = 300)
@Composable
private fun FrequencySelectorSelectedPreview() {
    var selected by remember { mutableStateOf<Long?>(TimeUnit.HOURS.toMillis(3)) }
    val frequencies = mapOf(
        "30 minutes" to TimeUnit.MINUTES.toMillis(30),
        "1 hour" to TimeUnit.HOURS.toMillis(1),
        "3 hours" to TimeUnit.HOURS.toMillis(3),
        "Daily" to TimeUnit.DAYS.toMillis(1)
    )
    Motiv8MeTheme {
        FrequencySelector(
            availableFrequencies = frequencies,
            selectedFrequencyMillis = selected,
            onFrequencySelected = { selected = it }
        )
    }
}

