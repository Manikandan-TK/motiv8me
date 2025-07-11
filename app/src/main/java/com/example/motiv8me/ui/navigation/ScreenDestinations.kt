package com.example.motiv8me.ui.navigation

/**
 * Defines the available navigation destinations (screens) in the Motiv8Me app.
 * Using a sealed interface ensures type safety and allows for easy management of routes.
 * Each object represents a distinct screen with a unique route string.
 */
sealed interface ScreenDestinations {
    val route: String // Each destination must have a route string

    data object Onboarding : ScreenDestinations {
        override val route: String = "onboarding"
    }

    data object Home : ScreenDestinations {
        override val route: String = "home"
    }

    data object Settings : ScreenDestinations {
        override val route: String = "settings"
    }

    data object HabitSelection : ScreenDestinations {
        override val route: String = "habit_selection"
    }

    data object NotificationSettings : ScreenDestinations {
        override val route: String = "notification_settings"
    }

    data object Upgrade : ScreenDestinations {
        override val route: String = "upgrade"
    }
}