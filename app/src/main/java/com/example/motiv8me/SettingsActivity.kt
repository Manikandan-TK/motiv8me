package com.example.motiv8me

// import android.content.Context // Removed (Unused import directive)
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.motiv8me.ui.theme.Motiv8MeTheme // Ensure this import is correct
// Ensure Habit is imported if it's in a different package (e.g., import com.example.motiv8me.Habit)
import com.example.motiv8me.Habit // Assuming Habit is in the same package

class SettingsActivity : ComponentActivity() {

    // Data class to represent interval options
    data class IntervalOption(val labelResId: Int, val durationMillis: Long)

    // Define interval options
    private val intervalOptions = listOf(
        IntervalOption(R.string.interval_1_hour, 60_000L),
        IntervalOption(R.string.interval_3_hours, 10_800_000L),
        IntervalOption(R.string.interval_6_hours, 21_600_000L),
        IntervalOption(R.string.interval_12_hours, 43_200_000L),
        IntervalOption(R.string.interval_24_hours, 86_400_000L)
    )

    private var selectedHabit: Habit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the selected Habit from the Intent
        // FIX: Call extension function on the 'intent' object
        selectedHabit = intent.getParcelableExtraCompat(MainActivity.EXTRA_SELECTED_HABIT, Habit::class.java)

        if (selectedHabit == null) {
            Toast.makeText(this, "Error: Habit data missing. Returning.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            Motiv8MeTheme {
                SettingsScreen(
                    habitName = selectedHabit!!.name,
                    intervalOptions = intervalOptions,
                    onStartClicked = { selectedIntervalMillis ->
                        startServiceAndNavigate(selectedHabit!!, selectedIntervalMillis)
                    }
                )
            }
        }
    }

    // Helper function - This function definition itself is fine
    private fun <T : android.os.Parcelable> Intent.getParcelableExtraCompat(key: String, clazz: Class<T>): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, clazz)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(key) as? T
        }
    }

    private fun startServiceAndNavigate(habit: Habit, intervalMillis: Long) {
        val serviceIntent = Intent(this, WallpaperService::class.java).apply {
            action = WallpaperService.ACTION_START
            putExtra(WallpaperService.EXTRA_HABIT_FOLDER, habit.folderName)
            putExtra(WallpaperService.EXTRA_INTERVAL_MILLIS, intervalMillis)
        }

        // FIX: Remove unnecessary SDK_INT check as minSdk is 26 (Oreo)
        startForegroundService(serviceIntent)
        // Removed the else block

        val activeModeIntent = Intent(this, ActiveModeActivity::class.java).apply {
            putExtra(ActiveModeActivity.EXTRA_CURRENT_HABIT, habit)
            putExtra(ActiveModeActivity.EXTRA_INTERVAL_MILLIS, intervalMillis)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
        }
        startActivity(activeModeIntent)
        // Optional: finish() // Consider if you want SettingsActivity off the back stack
    }

    companion object {
        // No constants needed here for receiving data
    }
}


// --- Composables for the Settings Screen --- (No changes needed below this line for these errors)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    habitName: String,
    intervalOptions: List<SettingsActivity.IntervalOption>,
    onStartClicked: (Long) -> Unit
) {
    var selectedIntervalMillis by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_title),
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
                Text(
                    text = stringResource(R.string.current_habit_label) + " $habitName",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(id = R.string.interval_selection_label),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    intervalOptions.forEach { option ->
                        IntervalItem(
                            option = option,
                            isSelected = selectedIntervalMillis == option.durationMillis,
                            onSelected = { selectedIntervalMillis = option.durationMillis }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (selectedIntervalMillis != null) {
                            onStartClicked(selectedIntervalMillis!!)
                        } else {
                            Toast.makeText(
                                context,
                                R.string.error_no_interval_selected,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedIntervalMillis != null
                ) {
                    Text(stringResource(id = R.string.start_button_text))
                }
            }
        }
    )
}

@Composable
fun IntervalItem(
    option: SettingsActivity.IntervalOption,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelected() },
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = option.labelResId),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreviewSettingsActivity() {
    val previewIntervalOptions = listOf(
        SettingsActivity.IntervalOption(R.string.interval_1_hour, 3600000L),
        SettingsActivity.IntervalOption(R.string.interval_3_hours, 10800000L)
    )
    Motiv8MeTheme {
        SettingsScreen(
            habitName = "Preview Habit",
            intervalOptions = previewIntervalOptions,
            onStartClicked = { }
        )
    }
}