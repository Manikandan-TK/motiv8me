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
import com.example.motiv8me.ui.features.settings.SettingsViewModel
import com.example.motiv8me.ui.features.upgrade.UpgradeScreen // IMPORT THE NEW SCREEN
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.setValue

// Shared theme settings across all screens
object SharedThemeSettings {
    var useSystemTheme = mutableStateOf(true)
    var isDarkTheme = mutableStateOf(false)
}

@Composable
fun ThemeToggleActions() {
    var showThemeMenu by remember { mutableStateOf(false) }
    val useSystemTheme by SharedThemeSettings.useSystemTheme
    val isDarkTheme by SharedThemeSettings.isDarkTheme

    Box {
        IconButton(onClick = { showThemeMenu = true }) {
            Icon(
                imageVector = if (useSystemTheme) Icons.Default.DarkMode
                else if (isDarkTheme) Icons.Default.DarkMode
                else Icons.Default.LightMode,
                contentDescription = "Theme settings"
            )
        }

        DropdownMenu(
            expanded = showThemeMenu,
            onDismissRequest = { showThemeMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("System theme") },
                onClick = {
                    SharedThemeSettings.useSystemTheme.value = true
                    showThemeMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Light mode") },
                onClick = {
                    SharedThemeSettings.useSystemTheme.value = false
                    SharedThemeSettings.isDarkTheme.value = false
                    showThemeMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Dark mode") },
                onClick = {
                    SharedThemeSettings.useSystemTheme.value = false
                    SharedThemeSettings.isDarkTheme.value = true
                    showThemeMenu = false
                }
            )
        }
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    appNavigationViewModel: AppNavigationViewModel = hiltViewModel()
) {
    val startDestination by appNavigationViewModel.startDestination.collectAsState()

    if (startDestination == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination!!,
            modifier = modifier
        ) {
            composable(route = ScreenDestinations.Onboarding.route) {
                OnboardingScreen(
                    onOnboardingComplete = {
                        navController.navigate(ScreenDestinations.Home.route) {
                            popUpTo(ScreenDestinations.Onboarding.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(route = ScreenDestinations.Home.route) {
                HomeScreen(
                    onNavigateToSettings = {
                        navController.navigate(ScreenDestinations.Settings.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(route = ScreenDestinations.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHabitSelection = {
                        navController.navigate(ScreenDestinations.HabitSelection.route)
                    },
                    onNavigateToNotificationSettings = {
                        navController.navigate(ScreenDestinations.NotificationSettings.route)
                    },
                    // UPDATE THIS LAMBDA
                    onNavigateToPro = {
                        navController.navigate(ScreenDestinations.Upgrade.route)
                    }
                )
            }

            composable(route = ScreenDestinations.HabitSelection.route) {
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

            composable(route = ScreenDestinations.NotificationSettings.route) {
                NotificationSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ADD THIS NEW COMPOSABLE DESTINATION
            composable(route = ScreenDestinations.Upgrade.route) {
                UpgradeScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}