package com.example.motiv8me

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.motiv8me.ui.navigation.AppNavigation // Will be created later
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Call the superclass implementation

        // Set the content of the activity using Jetpack Compose
        setContent {
            // Apply the custom Material 3 theme defined in ui.theme.Motiv8MeTheme
            // This wrapper provides consistent styling (colors, typography, shapes)
            // across the entire application and handles light/dark theme switching.
            Motiv8MeTheme {
                // A root Surface container using the 'background' color from the theme.
                // Provides a standard background and elevation handling for the app content.
                Surface(
                    modifier = Modifier.fillMaxSize(), // Ensure the Surface fills the entire screen
                    color = MaterialTheme.colorScheme.background // Use the theme's background color
                ) {
                    // Call the main navigation composable which sets up the NavHost
                    // and defines the different screens/destinations of the app.
                    // All screen content will be rendered within this navigation component.
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