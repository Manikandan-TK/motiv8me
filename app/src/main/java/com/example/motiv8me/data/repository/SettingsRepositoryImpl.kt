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

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override fun getSettings(): Flow<AppSettings> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                // Read each setting using keys from Constants, providing defaults
                val onboardingComplete = preferences[Constants.PREF_KEY_ONBOARDING_COMPLETE] ?: false
                val selectedHabit = preferences[Constants.PREF_KEY_SELECTED_HABIT]
                val wallpaperFrequency = preferences[Constants.PREF_KEY_WALLPAPER_FREQUENCY_MINUTES]
                val notificationFrequency = preferences[Constants.PREF_KEY_NOTIFICATION_FREQUENCY_MINUTES]

                AppSettings(
                    isOnboardingComplete = onboardingComplete,
                    selectedHabit = selectedHabit,
                    wallpaperFrequencyMinutes = wallpaperFrequency,
                    notificationFrequencyMinutes = notificationFrequency
                )
            }
    }

    override suspend fun saveHabitSetting(habitKey: String) {
        dataStore.edit { preferences ->
            preferences[Constants.PREF_KEY_SELECTED_HABIT] = habitKey
        }
    }

    override suspend fun saveWallpaperFrequency(frequencyMinutes: Long) {
        dataStore.edit { preferences ->
            preferences[Constants.PREF_KEY_WALLPAPER_FREQUENCY_MINUTES] = frequencyMinutes
        }
    }

    override suspend fun saveNotificationFrequency(frequencyMinutes: Long) {
        dataStore.edit { preferences ->
            preferences[Constants.PREF_KEY_NOTIFICATION_FREQUENCY_MINUTES] = frequencyMinutes
        }
    }

    override suspend fun saveOnboardingComplete(isComplete: Boolean) {
        dataStore.edit { preferences ->
            preferences[Constants.PREF_KEY_ONBOARDING_COMPLETE] = isComplete
        }
    }

    // --- Theme preference remains unchanged ---
    override val themePreference: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[Constants.PREF_KEY_THEME_PREFERENCE] ?: "System"
        }

    override suspend fun saveThemePreference(theme: String) {
        dataStore.edit { preferences ->
            preferences[Constants.PREF_KEY_THEME_PREFERENCE] = theme
        }
    }
}