package com.example.motiv8me.domain.repository

import com.example.motiv8me.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining the contract for accessing and modifying application settings.
 * This abstracts the data source (e.g., DataStore) from the rest of the application.
 */
interface SettingsRepository {

    /**
     * Retrieves all application settings as a Flow, allowing observation of changes.
     *
     * @return A Flow emitting the current [AppSettings].
     */
    fun getSettings(): Flow<AppSettings>

    /**
     * Saves the user's selected predefined habit.
     *
     * @param habit The name/identifier of the selected habit.
     */
    suspend fun saveHabitSetting(habit: String)

    /**
     * Saves the user's selected wallpaper change frequency.
     *
     * @param frequencyMillis The frequency interval in milliseconds.
     */
    suspend fun saveWallpaperFrequency(frequencyMillis: Long)

    /**
     * Saves the user's selected notification frequency.
     *
     * @param frequencyMillis The frequency interval in milliseconds (0L means Off).
     */
    suspend fun saveNotificationFrequency(frequencyMillis: Long)

    /**
     * Saves the completion status of the onboarding process.
     *
     * @param isComplete True if onboarding is complete, false otherwise.
     */
    suspend fun saveOnboardingComplete(isComplete: Boolean)

    val themePreference: Flow<String>

    suspend fun saveThemePreference(theme: String)

    // Optional: Individual getter methods if needed, though Flow is often preferred.
    // suspend fun getSelectedHabit(): String?
    // suspend fun getWallpaperFrequency(): Long?
    // suspend fun getNotificationFrequency(): Long?
    // suspend fun isOnboardingComplete(): Boolean
}