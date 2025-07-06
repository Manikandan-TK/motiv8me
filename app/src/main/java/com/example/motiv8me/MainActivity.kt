package com.example.motiv8me

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.motiv8me.ui.navigation.AppNavigation // Will be created later
import com.example.motiv8me.domain.repository.SettingsRepository
import javax.inject.Inject
import com.example.motiv8me.ui.theme.Motiv8MeTheme // Will be created later
import dagger.hilt.android.AndroidEntryPoint

/**
 * The main and only Activity for the Motiv8Me application.
 * It sets up the Jetpack Compose UI framework, applies the application theme,
 * and hosts the navigation graph defined in [AppNavigation].
 *
 * Annotated with @AndroidEntryPoint to enable field injection via Hilt.
 * Screen orientation is locked to portrait via AndroidManifest.xml.
 */
@AndroidEntryPoint // Enable Hilt for dependency injection in this Activity
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            

            val theme by settingsRepository.themePreference.collectAsStateWithLifecycle(initialValue = "System")
            val useDarkTheme = when (theme) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            Motiv8MeTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

// Optional: A basic preview function for Android Studio's Compose preview pane.
// This helps visualize the basic structure or theme application without running the app.
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Motiv8MeTheme {
        // In this preview, we just show the themed Surface.
        // The actual content depends on AppNavigation, which isn't defined yet.
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // You could add placeholder text or basic layout here for more detailed previews later.
            // e.g., Text("Previewing Motiv8Me Structure")
        }
    }
}