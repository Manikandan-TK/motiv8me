package com.example.motiv8me

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class ActiveModeActivity : ComponentActivity() {

    private var currentHabit: Habit? = null
    private var intervalMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve data from Intent
        currentHabit = intent.getParcelableExtraCompat(EXTRA_CURRENT_HABIT, Habit::class.java)
        intervalMillis = intent.getLongExtra(EXTRA_INTERVAL_MILLIS, 0L)

        if (currentHabit == null || intervalMillis <= 0) {
            Log.e(TAG, "Error: Missing data in Intent. Finishing Activity.")
            Toast.makeText(this, "Error starting active mode.", Toast.LENGTH_SHORT).show()
            finish() // Cannot operate without the data
            return
        }

        setContent {
            Motiv8MeTheme {
                ActiveModeScreen(
                    habitName = currentHabit!!.name, // Use !! because null check passed
                    intervalMillis = intervalMillis,
                    onChangeSettings = {
                        navigateToSettings(currentHabit!!) // Pass habit back to settings
                    },
                    onStop = {
                        stopWallpaperService()
                        // Navigate back to the main screen after stopping
                        navigateToMain()
                    }
                )
            }
        }
        Log.d(TAG, "ActiveModeActivity created for habit: ${currentHabit?.name}, interval: $intervalMillis ms")
    }

    // Helper function for retrieving Parcelable extras safely across API levels
    // (Same as in SettingsActivity, could be moved to a common Util file)
    private fun <T : android.os.Parcelable> Intent.getParcelableExtraCompat(key: String, clazz: Class<T>): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, clazz)
        } else {
            @Suppress("DEPRECATION") // Required for older APIs
            getParcelableExtra(key) as? T
        }
    }

    private fun stopWallpaperService() {
        Log.d(TAG, "Sending STOP action to WallpaperService")
        val serviceIntent = Intent(this, WallpaperService::class.java).apply {
            action = WallpaperService.ACTION_STOP
        }
        // Need to use startService or startForegroundService even to stop it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        // Service might take a moment to stop, but we initiate the stop command.
    }

    private fun navigateToSettings(habit: Habit) {
        Log.d(TAG, "Navigating back to Settings")
        val intent = Intent(this, SettingsActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_SELECTED_HABIT, habit) // Pass Habit back
            // Clear the ActiveModeActivity and bring SettingsActivity to top if it exists
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish() // Close ActiveModeActivity
    }

    private fun navigateToMain() {
        Log.d(TAG, "Navigating back to MainActivity after stopping service")
        val intent = Intent(this, MainActivity::class.java).apply {
            // Clear the task stack and start MainActivity as a new task
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // Close ActiveModeActivity
    }


    companion object {
        private const val TAG = "ActiveModeActivity"
        // Keys for Intent Extras RECEIVED by this Activity
        const val EXTRA_CURRENT_HABIT = "motiv8me.EXTRA_CURRENT_HABIT" // Changed to receive Habit object
        const val EXTRA_INTERVAL_MILLIS = "motiv8me.EXTRA_INTERVAL_MILLIS" // Reusing key from WallpaperService for consistency
    }
}

// --- Composables for the Active Mode Screen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveModeScreen(
    habitName: String,
    intervalMillis: Long,
    onChangeSettings: () -> Unit,
    onStop: () -> Unit
) {
    val context = LocalContext.current
    // State for the next swap time string
    var nextSwapTimeString by remember { mutableStateOf(context.getString(R.string.next_swap_time_calculating)) }

    // Calculate and update the next swap time string periodically
    // Note: This is an *approximation* based on interval only, doesn't sync perfectly with service
    LaunchedEffect(key1 = intervalMillis) { // Recalculate if intervalMillis changes (though unlikely here)
        while (true) {
            val currentTime = System.currentTimeMillis()
            // Find the last interval boundary before or at the current time
            val remainder = currentTime % intervalMillis
            val lastBoundary = currentTime - remainder
            // Next boundary is one interval after the last one
            val nextSwapTime = lastBoundary + intervalMillis

            val formattedTime = DateFormat.getTimeFormat(context).format(nextSwapTime)
            nextSwapTimeString = context.getString(R.string.next_swap_time_label) + " " + formattedTime

            // Delay until roughly the next minute to update the time string
            // Or adjust delay logic as needed for accuracy vs performance
            delay(60_000L - (System.currentTimeMillis() % 60_000L)) // Delay until start of next minute
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.active_mode_title),
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween // Push buttons to bottom
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Motivation Active Message
                    Text(
                        text = stringResource(id = R.string.motivation_active_message),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )

                    // Current Habit Display
                    Text(
                        text = stringResource(id = R.string.current_habit_label) + " $habitName",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Next Swap Time Display
                    Text(
                        text = nextSwapTimeString, // Display the calculated time string
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }


                // Buttons at the bottom
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = onChangeSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(stringResource(id = R.string.change_settings_button_text))
                    }

                    Button(
                        onClick = onStop,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // Use error color for stop
                    ) {
                        Text(stringResource(id = R.string.stop_button_text))
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreviewActiveModeActivity() {
    Motiv8MeTheme {
        ActiveModeScreen(
            habitName = "Preview Habit",
            intervalMillis = 3_600_000L, // 1 hour
            onChangeSettings = {},
            onStop = {}
        )
    }
}