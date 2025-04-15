package com.example.motiv8me.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.motiv8me.ui.features.habit_selection.HabitSelectionScreen
import com.example.motiv8me.ui.features.home.HomeScreen
import com.example.motiv8me.ui.features.notification_settings.NotificationSettingsScreen
import com.example.motiv8me.ui.features.onboarding.OnboardingScreen
import com.example.motiv8me.ui.features.settings.SettingsScreen
import com.example.motiv8me.ui.features.settings.SettingsViewModel // Import SettingsViewModel

/**
 * Sets up the navigation graph for the application using Jetpack Compose Navigation.
 *
 * It defines all possible screens (destinations) and the transitions between them.
 * Start destination is dynamically determined by AppNavigationViewModel.
 *
 * @param modifier Modifier to be applied to the NavHost container.
 * @param navController The navigation controller used to manage navigation state.
 * @param appNavigationViewModel ViewModel to determine the start destination.
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    // Inject the ViewModel responsible for determining the start destination
    appNavigationViewModel: AppNavigationViewModel = hiltViewModel()
) {
    // Observe the start destination state from the AppNavigationViewModel
    val startDestination by appNavigationViewModel.startDestination.collectAsState()

    // Display content based on whether the start destination is determined
    if (startDestination == null) {
        // Show a loading indicator while determining the start destination
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // Build the NavHost once the start destination is known
        NavHost(
            navController = navController,
            startDestination = startDestination!!, // Use non-null assertion as checked above
            modifier = modifier
        ) {
            // --- Destinations ---

            // Onboarding Screen
            composable(route = ScreenDestinations.Onboarding.route) {
                OnboardingScreen(
                    onOnboardingComplete = {
                        navController.navigate(ScreenDestinations.Settings.route) {
                            popUpTo(ScreenDestinations.Onboarding.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Home Screen (Placeholder)
            composable(route = ScreenDestinations.Home.route) {
                HomeScreen(
                    onNavigateToSettings = {
                        navController.navigate(ScreenDestinations.Settings.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Settings Screen
            composable(route = ScreenDestinations.Settings.route) { backStackEntry ->
                // Obtain SettingsViewModel scoped to this destination
                val settingsViewModel: SettingsViewModel = hiltViewModel(backStackEntry)

                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToHabitSelection = {
                        navController.navigate(ScreenDestinations.HabitSelection.route)
                    },
                    onNavigateToNotificationSettings = {
                        navController.navigate(ScreenDestinations.NotificationSettings.route)
                    }
                )
            }

            // Habit Selection Screen
            composable(route = ScreenDestinations.HabitSelection.route) {
                // Get SettingsViewModel scoped to the previous entry (SettingsScreen)
                val settingsViewModel: SettingsViewModel = hiltViewModel(
                    navController.previousBackStackEntry
                        ?: throw IllegalStateException("Cannot access previous back stack entry for SettingsViewModel")
                )

                HabitSelectionScreen(
                    onHabitSelected = { selectedHabit ->
                        settingsViewModel.onHabitChanged(selectedHabit)
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Notification Settings Screen
            composable(route = ScreenDestinations.NotificationSettings.route) {
                // Uses its own ViewModel (NotificationSettingsViewModel) via hiltViewModel()
                NotificationSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        } // End NavHost
    } // End else (startDestination != null)
}

// Ensure temporary placeholder screens are removed from the bottom of this file.