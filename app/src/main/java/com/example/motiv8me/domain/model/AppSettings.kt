package com.example.motiv8me.domain.model

/**
 * Represents the collective user settings for the Motiv8Me application.
 *
 * @param selectedHabit The name/identifier of the currently selected predefined habit. Null if not set.
 * @param wallpaperFrequencyMillis The interval for wallpaper changes in milliseconds. Null if not set.
 * @param notificationFrequencyMillis The interval for motivational notifications in milliseconds.
 *                                    0L indicates notifications are off. Null if not set.
 * @param isOnboardingComplete Flag indicating if the user has completed the initial setup flow.
 */
data class AppSettings(
    val selectedHabit: String?,
    val wallpaperFrequencyMillis: Long?,
    val notificationFrequencyMillis: Long?,
    val isOnboardingComplete: Boolean
)