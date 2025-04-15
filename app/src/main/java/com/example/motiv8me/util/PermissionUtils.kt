package com.example.motiv8me.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Utility object for handling Android permissions required by the Motiv8Me app.
 */
object PermissionUtils {

    /**
     * Checks if the app has permission to set the device wallpaper.
     *
     * Note: `SET_WALLPAPER` is typically a signature/install-time permission.
     * While `checkSelfPermission` might return GRANTED if declared in the Manifest,
     * it's not a standard runtime permission. A more definitive check might involve
     * attempting to use the WallpaperManager, but this simple check is often sufficient.
     * Assume granted if declared in Manifest for most practical purposes.
     *
     * @param context The application context.
     * @return `true` if the permission appears to be granted, `false` otherwise.
     */
    fun hasWallpaperPermission(context: Context): Boolean {
        // For SET_WALLPAPER, simply checking the manifest declaration is often the
        // most practical approach, as runtime checks can be unreliable or unnecessary.
        // However, using checkSelfPermission provides some level of check.
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SET_WALLPAPER
        ) == PackageManager.PERMISSION_GRANTED
        // Alternatively, could just return true if the permission is in the Manifest,
        // assuming install-time grant.
    }

    /**
     * Checks if the app has permission to post notifications.
     * This is a runtime permission required on Android 13 (API 33) and above.
     *
     * @param context The application context.
     * @return `true` if the permission is granted or if the Android version is below 13,
     *         `false` otherwise.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        // Runtime permission only needed for Android 13 (Tiramisu) and higher.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // On older versions, notification permission is implicitly granted.
            true
        }
    }

    /**
     * Checks if the app has permission to receive boot completed broadcast.
     * This is typically granted at install time if declared in the Manifest.
     *
     * @param context The application context.
     * @return `true` if the permission appears to be granted, `false` otherwise.
     */
    fun hasBootCompletedPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
        ) == PackageManager.PERMISSION_GRANTED
    }


    /**
     * Opens the application's settings screen in the system settings app.
     * Useful for directing the user to manually grant permissions if they were denied.
     *
     * @param context The application context.
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // Add checks for other permissions like SCHEDULE_EXACT_ALARM or
    // REQUEST_IGNORE_BATTERY_OPTIMIZATIONS if needed later.
    // fun hasExactAlarmPermission(context: Context): Boolean { ... }
    // fun isIgnoringBatteryOptimizations(context: Context): Boolean { ... }

}