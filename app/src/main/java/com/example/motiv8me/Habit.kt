package com.example.motiv8me

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a habit the user wants to track or change.
 *
 * @property name The user-facing name of the habit (e.g., "Quit Smoking").
 * @property folderName The name of the corresponding drawable folder
 *                      containing the wallpapers for this habit (e.g., "quit_smoking").
 */
@Parcelize
data class Habit(
    val name: String,
    val folderName: String
) : Parcelable