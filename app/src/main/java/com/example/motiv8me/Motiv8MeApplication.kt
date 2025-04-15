package com.example.motiv8me // Ensure this matches your base package

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // Crucial for Hilt setup
class Motiv8MeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize anything needed application-wide here
        // e.g., Timber logging library if you add it later
    }
}