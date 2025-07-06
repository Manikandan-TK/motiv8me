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
     * Checks if the SET_WALLPAPER permission is declared in the Manifest.
     * This is not a true runtime permission check, but confirms the app *can* set it.
     * The user grants the permission by completing the Intent.ACTION_SET_WALLPAPER flow.
     *
     * @param context The application context.
     * @return `true` if the permission is declared in the manifest, `false` otherwise.
     */
    fun hasWallpaperPermission(context: Context): Boolean {
        // This is an install-time permission. The most useful check is to see if it's
        // in the manifest, confirming the app has the *capability*. The user "grants"
        // it by successfully setting a wallpaper via the system intent.
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            val requestedPermissions = packageInfo.requestedPermissions
            requestedPermissions?.contains(Manifest.permission.SET_WALLPAPER) == true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
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
}