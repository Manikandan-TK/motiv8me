package com.example.motiv8me.ui.navigation

/**
 * Defines the available navigation destinations (screens) in the Motiv8Me app.
 * Using a sealed interface ensures type safety and allows for easy management of routes.
 * Each object represents a distinct screen with a unique route string.
 */
sealed interface ScreenDestinations {
    val route: String // Each destination must have a route string

    /**
     * Represents the initial onboarding flow screens.
     * For simplicity in the free tier, this might be a single screen or a very short sequence.
     * We use a base route "onboarding" which might be expanded later if needed.
     */
    data object Onboarding : ScreenDestinations {
        override val route: String = "onboarding"
    }

    /**
     * Represents the main "home" screen displayed after onboarding/setup.
     * In this app's concept, the "home" might be implicit (just the wallpaper changing),
     * but we might need a screen to confirm setup is complete or show minimal info.
     * Alternatively, this could navigate directly to Settings after onboarding.
     */
    data object Home : ScreenDestinations {
        override val route: String = "home"
    }

    /**
     * Represents the main settings screen where users can change habit, frequency, etc.
     */
    data object Settings : ScreenDestinations {
        override val route: String = "settings"
    }

    /**
     * Represents the screen specifically for selecting a predefined habit.
     * This might be part of onboarding or accessed from Settings.
     */
    data object HabitSelection : ScreenDestinations {
        override val route: String = "habit_selection"
        // Example if arguments were needed later:
        // const val routeArg = "isFromOnboarding"
        // val routeWithArgs = "$route/{$routeArg}"
        // fun createRoute(isFromOnboarding: Boolean) = "$route/$isFromOnboarding"
    }

    /**
     * Represents the screen specifically for configuring notification preferences.
     * Likely accessed from the main Settings screen.
     */
    data object NotificationSettings : ScreenDestinations {
        override val route: String = "notification_settings"
    }

    // Add other destinations here as needed in the future (e.g., Pro features info)
}

// Helper list for potential use cases like bottom navigation (though not planned for free tier)
// val bottomNavScreens = listOf(ScreenDestinations.Home, ScreenDestinations.Settings)