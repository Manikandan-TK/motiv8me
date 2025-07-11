package com.example.motiv8me.domain.model

/**
 * Represents the collective user settings for the Motiv8Me application.
 *
 * @param selectedHabit The key of the currently selected predefined habit (e.g., "stop_smoking"). Null if not set.
 * @param wallpaperFrequencyMinutes The interval for wallpaper changes in minutes. Null if not set.
 * @param notificationFrequencyMinutes The interval for motivational notifications in minutes.
 *                                     0L indicates notifications are off. Null if not set.
 * @param isOnboardingComplete Flag indicating if the user has completed the initial setup flow.
 * @param isProUser Flag indicating if the user has unlocked premium features.
 */
data class AppSettings(
    val isOnboardingComplete: Boolean,
    val selectedHabit: String?,
    val wallpaperFrequencyMinutes: Long?,
    val notificationFrequencyMinutes: Long?,
    val isProUser: Boolean
)