package com.example.motiv8me.util

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.motiv8me.R // Import R for drawable resources
import java.util.concurrent.TimeUnit

object Constants {

    // --- Predefined Content ---

    /** List of habits available for selection in the free tier. */
    val PREDEFINED_HABITS: List<String> = listOf(
        "Stop Smoking",
        "Reduce Alcohol",
        "Eat Healthier",
        "Exercise More",
        "Reduce Screen Time",
        "Stop Procrastinating",
        "Improve Sleep",
        "Manage Stress"
        // Add more predefined habits as desired
    )

    val SHARED_APP_FREQUENCIES: List<Pair<String, Long>> = listOf(
        Pair("15 minutes", 15L),
        Pair("30 minutes", 30L),
        Pair("1 hour", 60L),
        Pair("3 hours", 180L),
        Pair("6 hours", 360L),
        Pair("12 hours", 720L),
        Pair("24 hours", 1440L)
    )

    /** Map of display names for wallpaper frequencies to their values in milliseconds. */
    val WALLPAPER_FREQUENCIES: List<Pair<String, Long>> = SHARED_APP_FREQUENCIES

    /** Map of display names for notification frequencies to their values in milliseconds (or special values). */
    val NOTIFICATION_FREQUENCY_OPTIONS: List<Pair<String, Long>> = SHARED_APP_FREQUENCIES

    const val NOTIFICATION_FREQUENCY_OFF = 0L

    /** List of motivational quotes for notifications. */
    val MOTIVATIONAL_QUOTES: List<String> = listOf(
        "Believe you can and you're halfway there. - Theodore Roosevelt",
        "The only way to do great work is to love what you do. - Steve Jobs",
        "Success is not final, failure is not fatal: It is the courage to continue that counts. - Winston Churchill",
        "It does not matter how slowly you go as long as you do not stop. - Confucius",
        "Your limitation—it's only your imagination.",
        "Push yourself, because no one else is going to do it for you.",
        "Great things never come from comfort zones.",
        "Dream it. Wish it. Do it.",
        "Success doesn’t just find you. You have to go out and get it.",
        "The harder you work for something, the greater you’ll feel when you achieve it.",
        "Don’t stop when you’re tired. Stop when you’re done.",
        "Wake up with determination. Go to bed with satisfaction.",
        "Do something today that your future self will thank you for.",
        "Little things make big days.",
        "It’s going to be hard, but hard does not mean impossible.",
        "Don’t wait for opportunity. Create it.",
        "Sometimes we’re tested not to show our weaknesses, but to discover our strengths.",
        "The key to success is to focus on goals, not obstacles.",
        "You don’t have to be great to start, but you have to start to be great."
        // Add many more quotes
    )

    /**
     * Map linking predefined habit names to a list of drawable resource IDs for wallpapers.
     * Ensure these drawable resources exist in `res/drawable-nodpi/`.
     * Use descriptive names like `wallpaper_stop_smoking_1`, `wallpaper_stop_smoking_2`, etc.
     */
    val HABIT_TO_IMAGE_MAP: Map<String, List<Int>> = mapOf(
        "Stop Smoking" to listOf(
            R.drawable.wallpaper_stop_smoking_1, // Replace with actual drawable IDs
            R.drawable.wallpaper_stop_smoking_2,
            R.drawable.wallpaper_stop_smoking_3
        ),
        "Reduce Alcohol" to listOf(
            R.drawable.wallpaper_reduce_alcohol_1,
            R.drawable.wallpaper_reduce_alcohol_2,
            R.drawable.wallpaper_reduce_alcohol_3
        ),
        "Eat Healthier" to listOf(
            R.drawable.wallpaper_eat_healthier_1,
            R.drawable.wallpaper_eat_healthier_2,
            R.drawable.wallpaper_eat_healthier_3
        ),
        "Exercise More" to listOf(
            R.drawable.wallpaper_exercise_more_1,
            R.drawable.wallpaper_exercise_more_2,
            R.drawable.wallpaper_exercise_more_3
        ),
        "Reduce Screen Time" to listOf(
            R.drawable.wallpaper_reduce_screen_time_1,
            R.drawable.wallpaper_reduce_screen_time_2,
            R.drawable.wallpaper_reduce_screen_time_3
        ),
        "Stop Procrastinating" to listOf(
            R.drawable.wallpaper_stop_procrastinating_1,
            R.drawable.wallpaper_stop_procrastinating_2,
            R.drawable.wallpaper_stop_procrastinating_3
        ),
        "Improve Sleep" to listOf(
            R.drawable.wallpaper_improve_sleep_1,
            R.drawable.wallpaper_improve_sleep_2,
            R.drawable.wallpaper_improve_sleep_3
        ),
        "Manage Stress" to listOf(
            R.drawable.wallpaper_manage_stress_1,
            R.drawable.wallpaper_manage_stress_2,
            R.drawable.wallpaper_manage_stress_3
        )
        // Add entries for all habits in PREDEFINED_HABITS
    )

    // --- DataStore Keys ---
    val PREF_KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    val PREF_KEY_SELECTED_HABIT = stringPreferencesKey("selected_habit")
    val PREF_KEY_WALLPAPER_FREQUENCY = longPreferencesKey("wallpaper_frequency_millis")
    val PREF_KEY_NOTIFICATION_FREQUENCY = longPreferencesKey("notification_frequency_millis") // 0 means off
    val PREF_KEY_THEME_PREFERENCE = stringPreferencesKey("theme_preference")

    // --- WorkManager ---
    const val WALLPAPER_WORKER_TAG = "WallpaperWorkerTag"
    const val WALLPAPER_WORKER_NAME = "Motiv8MeWallpaperChanger" // Unique name for periodic work

    const val NOTIFICATION_WORKER_TAG = "NotificationWorkerTag"
    const val NOTIFICATION_WORKER_NAME = "Motiv8MeNotifier" // Unique name for periodic work

    // --- Notifications ---
    const val NOTIFICATION_CHANNEL_ID = "motiv8me_quotes"
    const val NOTIFICATION_CHANNEL_NAME = "Motivational Quotes"
    const val NOTIFICATION_ID = 1001 // Unique ID for the quote notification itself
}