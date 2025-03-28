package com.example.motiv8me

// import android.annotation.SuppressLint // No longer needed
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager // Import AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.IOException
import java.io.InputStream // Import InputStream
import java.util.Random

class WallpaperService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var wallpaperManager: WallpaperManager
    private var currentHabitFolder: String? = null
    private var currentIntervalMillis: Long = 0L
    private val random = Random()
    private var wallpaperRunnable: Runnable? = null
    private var lastSetTimeMillis: Long = 0L

    private val expectedWallpaperCount = 5

    private val notificationChannelId = "Motiv8MeChannel"
    private val notificationId = 18

    override fun onCreate() {
        super.onCreate()
        wallpaperManager = WallpaperManager.getInstance(this)
        Log.d(TAG, "WallpaperService created.")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand received action: ${intent?.action}")

        when (intent?.action) {
            ACTION_START -> {
                val habitFolder = intent.getStringExtra(EXTRA_HABIT_FOLDER)
                val intervalMillis = intent.getLongExtra(EXTRA_INTERVAL_MILLIS, 0L)

                if (habitFolder.isNullOrEmpty() || intervalMillis <= 0) {
                    Log.e(TAG, "Invalid start parameters. Stopping service.")
                    stopService()
                    return START_NOT_STICKY
                }

                if (habitFolder != currentHabitFolder || intervalMillis != currentIntervalMillis || wallpaperRunnable == null) {
                    currentHabitFolder = habitFolder
                    currentIntervalMillis = intervalMillis
                    Log.i(TAG, "Starting wallpaper changes for '$currentHabitFolder' every ${currentIntervalMillis / 1000} seconds.")
                    startWallpaperChanges()
                } else {
                    Log.d(TAG, "Service already running with same parameters.")
                    startForeground(notificationId, createNotification())
                }
                return START_STICKY
            }
            ACTION_STOP -> {
                Log.i(TAG, "Stopping wallpaper changes.")
                stopService()
                return START_NOT_STICKY
            }
            else -> {
                Log.w(TAG, "Unknown or null action received.")
                if (currentHabitFolder != null && currentIntervalMillis > 0 && wallpaperRunnable != null) {
                    Log.d(TAG, "Service restarted by system. Resuming with previous settings.")
                    startForeground(notificationId, createNotification())
                    handler.removeCallbacks(wallpaperRunnable!!)
                    handler.post(wallpaperRunnable!!)
                } else {
                    Log.w(TAG, "Service restarted by system but no previous state found. Stopping.")
                    stopService()
                }
                return START_STICKY
            }
        }
    }

    private fun startWallpaperChanges() {
        stopWallpaperLoop()
        val notification = createNotification()
        startForeground(notificationId, notification)

        wallpaperRunnable = object : Runnable {
            override fun run() {
                Log.d(TAG, "Executing wallpaper change task...")
                changeWallpaper() // Use the modified version below
                handler.postDelayed(this, currentIntervalMillis)
                Log.d(TAG, "Scheduled next wallpaper change in ${currentIntervalMillis / 1000} seconds.")
            }
        }
        handler.post(wallpaperRunnable!!)
        Log.d(TAG, "Posted initial wallpaper change task.")
    }

    // --- MODIFIED changeWallpaper function using AssetManager ---
    private fun changeWallpaper() {
        if (currentHabitFolder == null) {
            Log.e(TAG, "Cannot change wallpaper, habit folder is null.")
            return
        }

        var inputStream: InputStream? = null // Declare stream variable outside try
        var assetPath: String? = null // Store path for logging

        try {
            val assetManager: AssetManager = applicationContext.assets
            val randomIndex = random.nextInt(expectedWallpaperCount) + 1
            // Assuming images are JPEG, adjust extension if needed (.jpg, .png, etc.)
            val fileName = "${currentHabitFolder}_$randomIndex.jpg"
            // Construct the full path within the assets folder
            assetPath = "wallpapers/$currentHabitFolder/$fileName"

            Log.d(TAG, "Attempting to load asset: $assetPath")

            // Open an input stream to the asset
            inputStream = assetManager.open(assetPath)

            // Decode the bitmap from the stream
            val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)

            if (bitmap != null) {
                Log.d(TAG, "Bitmap decoded successfully for: $assetPath")
                // Requires SET_WALLPAPER permission in Manifest
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)

                lastSetTimeMillis = System.currentTimeMillis()
                Log.i(TAG, "Wallpaper successfully set to: $assetPath")
                updateNotification()
                // bitmap.recycle() // Consider if needed, but be careful with streams
            } else {
                Log.e(TAG, "Failed to decode bitmap from asset stream: $assetPath")
            }

        } catch (e: IOException) {
            // IOException can happen during assetManager.open() or decodeStream()
            Log.e(TAG, "IOException accessing or decoding asset '$assetPath': ${e.message}", e)
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "OutOfMemoryError decoding asset '$assetPath': ${e.message}", e)
        } catch (e: Exception) {
            // Catch unexpected errors during wallpaper setting or asset handling
            Log.e(TAG, "Unexpected error changing wallpaper from asset '$assetPath': ${e.message}", e)
        } finally {
            // IMPORTANT: Always close the input stream in a finally block
            try {
                inputStream?.close()
                Log.d(TAG,"InputStream closed for $assetPath")
            } catch (e: IOException) {
                Log.e(TAG, "IOException closing asset stream for '$assetPath': ${e.message}", e)
            }
        }
    }
    // --- End of MODIFIED changeWallpaper function ---


    private fun stopWallpaperLoop() {
        wallpaperRunnable?.let {
            handler.removeCallbacks(it)
            Log.d(TAG, "Removed pending wallpaper change callbacks.")
        }
        wallpaperRunnable = null
    }

    private fun stopService() {
        stopWallpaperLoop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.i(TAG, "WallpaperService stopped.")
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            notificationChannelId,
            "Motiv8Me Background Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel for Motiv8Me wallpaper service notification"
            enableLights(false)
            enableVibration(false)
            setShowBadge(false)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(serviceChannel)
        Log.d(TAG, "Notification channel created.")
    }


    private fun createNotification(contentText: String? = null): Notification {
        val notificationIntent = Intent(this, ActiveModeActivity::class.java).apply {
            putExtra(ActiveModeActivity.EXTRA_INTERVAL_MILLIS, currentIntervalMillis)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultText = getString(R.string.motivation_active_message) + " Habit: ${currentHabitFolder ?: "N/A"}"
        val notificationText = contentText ?: defaultText

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Motiv8Me Active")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_stat_motiv8me) // Ensure this icon exists
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification() {
        if (lastSetTimeMillis > 0 && currentIntervalMillis > 0) {
            val nextSwapTimeMillis = lastSetTimeMillis + currentIntervalMillis
            val nextSwapTimeStr = android.text.format.DateFormat.getTimeFormat(this).format(nextSwapTimeMillis)
            val notificationText = "Habit: ${currentHabitFolder ?: "N/A"}. Next swap ~${nextSwapTimeStr}"
            val notification = createNotification(notificationText)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, notification)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopWallpaperLoop()
        Log.d(TAG, "WallpaperService destroyed.")
    }

    companion object {
        private const val TAG = "WallpaperService"

        const val ACTION_START = "motiv8me.ACTION_START"
        const val ACTION_STOP = "motiv8me.ACTION_STOP"

        const val EXTRA_HABIT_FOLDER = "motiv8me.EXTRA_HABIT_FOLDER"
        const val EXTRA_INTERVAL_MILLIS = "motiv8me.EXTRA_INTERVAL_MILLIS"
    }
}