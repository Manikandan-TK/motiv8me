package com.example.motiv8me.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.example.motiv8me.domain.model.AppSettings
import com.example.motiv8me.domain.repository.SettingsRepository
import com.example.motiv8me.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the SettingsRepository using Jetpack Preferences DataStore.
 * Handles reading and writing application settings.
 *
 * Marked as Singleton to ensure only one instance interacts with the DataStore.
 *
 * @param dataStore The DataStore<Preferences> instance provided by Hilt.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences> // Injected via Hilt (Module needed)
) : SettingsRepository {

    /**
     * Retrieves all application settings as a Flow from DataStore.
     * Maps the raw Preferences object to the AppSettings data class.
     * Includes error handling for initial reads or corrupted data.
     */
    override fun getSettings(): Flow<AppSettings> {
        return dataStore.data
            .catch { exception ->
                // Handle errors reading DataStore (e.g., file corruption)
                if (exception is IOException) {
                    // Log the error or inform the user
                    emit(emptyPreferences()) // Emit empty preferences to recover
                } else {
                    throw exception // Rethrow other exceptions
                }
            }
            .map { preferences ->
                // Read each setting using keys from Constants, providing defaults
                val onboardingComplete = preferences[Constants.PREF_KEY_ONBOARDING_COMPLETE] ?: false
                val selectedHabit = preferences[Constants.PREF_KEY_SELECTED_HABIT] // Nullable by default
                val wallpaperFrequency = preferences[Constants.PREF_KEY_WALLPAPER_FREQUENCY] // Nullable by default
                val notificationFrequency = preferences[Constants.PREF_KEY_NOTIFICATION_FREQUENCY] // Nullable by default

                AppSettings(
                    isOnboardingComplete = onboardingComplete,
                    selectedHabit = selectedHabit,
                    wallpaperFrequencyMillis = wallpaperFrequency,
                    notificationFrequencyMillis = notificationFrequency
                )
            }
    }

    /**
     * Saves the selected habit identifier to DataStore.
     */
    override suspend fun saveHabitSetting(habit: String) {
        try {
            dataStore.edit { preferences ->
                preferences[Constants.PREF_KEY_SELECTED_HABIT] = habit
            }
        } catch (e: Exception) {
            // Defensive: log and rethrow for ViewModel to handle
            // Log.e("SettingsRepositoryImpl", "Error saving habit setting", e)
            throw e
        }
    }

    /**
     * Saves the selected wallpaper frequency (in milliseconds) to DataStore.
     */
    override suspend fun saveWallpaperFrequency(frequencyMillis: Long) {
        try {
            dataStore.edit { preferences ->
                preferences[Constants.PREF_KEY_WALLPAPER_FREQUENCY] = frequencyMillis
            }
        } catch (e: Exception) {
            // Defensive: log and rethrow for ViewModel to handle
            // Log.e("SettingsRepositoryImpl", "Error saving wallpaper frequency", e)
            throw e
        }
    }

    /**
     * Saves the selected notification frequency (in milliseconds) to DataStore.
     * 0L indicates notifications are off.
     */
    override suspend fun saveNotificationFrequency(frequencyMillis: Long) {
        try {
            dataStore.edit { preferences ->
                preferences[Constants.PREF_KEY_NOTIFICATION_FREQUENCY] = frequencyMillis
            }
        } catch (e: Exception) {
            // Defensive: log and rethrow for ViewModel to handle
            // Log.e("SettingsRepositoryImpl", "Error saving notification frequency", e)
            throw e
        }
    }

    /**
     * Saves the onboarding completion status to DataStore.
     */
    override suspend fun saveOnboardingComplete(isComplete: Boolean) {
        try {
            dataStore.edit { preferences ->
                preferences[Constants.PREF_KEY_ONBOARDING_COMPLETE] = isComplete
            }
        } catch (e: Exception) {
            // Defensive: log and rethrow for ViewModel to handle
            // Log.e("SettingsRepositoryImpl", "Error saving onboarding complete", e)
            throw e
        }
    }
}