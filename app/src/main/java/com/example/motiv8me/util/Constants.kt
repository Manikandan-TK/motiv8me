package com.example.motiv8me.util

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.motiv8me.R

object Constants {

    // --- DataStore Keys (Corrected to reflect stored unit) ---
    val PREF_KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    val PREF_KEY_SELECTED_HABIT = stringPreferencesKey("selected_habit_key")
    val PREF_KEY_WALLPAPER_FREQUENCY_MINUTES = longPreferencesKey("wallpaper_frequency_minutes")
    val PREF_KEY_NOTIFICATION_FREQUENCY_MINUTES = longPreferencesKey("notification_frequency_minutes")
    val PREF_KEY_THEME_PREFERENCE = stringPreferencesKey("theme_preference")

    // --- WorkManager ---
    const val WALLPAPER_WORKER_NAME = "Motiv8MeWallpaperChanger"
    const val NOTIFICATION_WORKER_NAME = "Motiv8MeNotifier"

    // --- Notifications ---
    const val NOTIFICATION_CHANNEL_ID = "motiv8me_quotes"
    const val NOTIFICATION_ID = 1001

    // --- App Content ---

    /**
     * List of pairs for habit selection.
     * First: User-facing display name (e.g., "Stop Smoking")
     * Second: Internal key for storage and resource lookup (e.g., "stop_smoking")
     */
    val HABIT_OPTIONS: List<Pair<String, String>> = listOf(
        Pair("Stop Smoking", "stop_smoking"),
        Pair("Reduce Alcohol", "reduce_alcohol"),
        Pair("Eat Healthier", "eat_healthier"),
        Pair("Exercise More", "exercise_more"),
        Pair("Reduce Screen Time", "reduce_screen_time"),
        Pair("Stop Procrastinating", "stop_procrastinating"),
        Pair("Improve Sleep", "improve_sleep"),
        Pair("Manage Stress", "manage_stress")
    )

    /**
     * Shared frequency options for both wallpapers and notifications.
     * First: User-facing display name (e.g., "15 minutes")
     * Second: Value in minutes (Long) for WorkManager.
     */
    val SHARED_APP_FREQUENCIES: List<Pair<String, Long>> = listOf(
        Pair("15 minutes", 15L),
        Pair("30 minutes", 30L),
        Pair("1 hour", 60L),
        Pair("3 hours", 180L),
        Pair("6 hours", 360L),
        Pair("12 hours", 720L),
        Pair("24 hours", 1440L)
    )

    const val NOTIFICATION_FREQUENCY_OFF = 0L

    /**
     * Map linking the internal habit key to a list of drawable resource IDs for wallpapers.
     * This ensures we are not relying on display strings for logic.
     */
    val HABIT_TO_IMAGE_MAP: Map<String, List<Int>> = mapOf(
        "stop_smoking" to listOf(
            R.drawable.wallpaper_stop_smoking_1,
            R.drawable.wallpaper_stop_smoking_2,
            R.drawable.wallpaper_stop_smoking_3
        ),
        "reduce_alcohol" to listOf(
            R.drawable.wallpaper_reduce_alcohol_1,
            R.drawable.wallpaper_reduce_alcohol_2,
            R.drawable.wallpaper_reduce_alcohol_3
        ),
        "eat_healthier" to listOf(
            R.drawable.wallpaper_eat_healthier_1,
            R.drawable.wallpaper_eat_healthier_2,
            R.drawable.wallpaper_eat_healthier_3
        ),
        "exercise_more" to listOf(
            R.drawable.wallpaper_exercise_more_1,
            R.drawable.wallpaper_exercise_more_2,
            R.drawable.wallpaper_exercise_more_3
        ),
        "reduce_screen_time" to listOf(
            R.drawable.wallpaper_reduce_screen_time_1,
            R.drawable.wallpaper_reduce_screen_time_2,
            R.drawable.wallpaper_reduce_screen_time_3
        ),
        "stop_procrastinating" to listOf(
            R.drawable.wallpaper_stop_procrastinating_1,
            R.drawable.wallpaper_stop_procrastinating_2,
            R.drawable.wallpaper_stop_procrastinating_3
        ),
        "improve_sleep" to listOf(
            R.drawable.wallpaper_improve_sleep_1,
            R.drawable.wallpaper_improve_sleep_2,
            R.drawable.wallpaper_improve_sleep_3
        ),
        "manage_stress" to listOf(
            R.drawable.wallpaper_manage_stress_1,
            R.drawable.wallpaper_manage_stress_2,
            R.drawable.wallpaper_manage_stress_3
        )
    )

    val MOTIVATIONAL_QUOTES: List<String> = listOf(
        "Believe you can and you're halfway there. - Theodore Roosevelt",
        "The only way to do great work is to love what you do. - Steve Jobs",
        "Success is not final, failure is not fatal: It is the courage to continue that counts. - Winston Churchill",
        "It does not matter how slowly you go as long as you do not stop. - Confucius",
        "Your limitation—it's only your imagination.",
        "Push yourself, because no one else is going to do it for you."
    )
}