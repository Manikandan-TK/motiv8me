package com.example.motiv8me.domain.repository

import com.example.motiv8me.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun saveHabitSetting(habitKey: String)
    suspend fun saveWallpaperFrequency(frequencyMinutes: Long)
    suspend fun saveNotificationFrequency(frequencyMinutes: Long)
    suspend fun saveOnboardingComplete(isComplete: Boolean)
    val themePreference: Flow<String>
    suspend fun saveThemePreference(theme: String)
}