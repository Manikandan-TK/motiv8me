package com.example.motiv8me.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("BootCompletedReceiver", "Device booted. Receiver triggered.")
            // TODO: Add logic to reschedule notifications or other startup tasks if needed
        }
    }
}
