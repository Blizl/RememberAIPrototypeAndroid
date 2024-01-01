package com.reality.rememberaiprototype.home.data

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.reality.rememberaiprototype.MainActivity
import com.reality.rememberaiprototype.R
import timber.log.Timber


class ScreenshotService : Service() {

    companion object {
        private const val SCREENSHOT_INTERVAL_SECONDS = 10
        private const val SCREENSHOT_INTERVAL = SCREENSHOT_INTERVAL_SECONDS * 1000L
        private const val REQUEST_CODE = 102
        private const val NOTIFICATION_ID = 11
        private const val CHANNEL_ID = "SCREENSHOT_CHANNEL_ID"
    }

    //    private var mediaProjection: MediaProjection?
    private lateinit var imageReader: ImageReader
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var keyguardManager: KeyguardManager
    private lateinit var windowManager: WindowManager

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
//            val notification = createNotification(this) // Create your foreground notification
//            startForeground(10, notification)
        }
        takeScreenshot()
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        val displayMetrics = DisplayMetrics()
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
//        imageReader = ImageReader.newInstance(
//            screenWidth, screenHeight,
//            PixelFormat.RGBA_8888, 2
//        )
        mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
//        val notification = createNotification(this) // Create your foreground notification

//        startForeground(NOTIFICATION_ID, notification)


        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Remember AI")
                .setContentText("Took Screenshot!").build()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                startForeground(NOTIFICATION_ID, notification)
            } else {
                startForeground(
                    NOTIFICATION_ID, notification,
                    FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
            }
        }
    }



    private fun takeScreenshot() {

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                // Enqueue the work after a delay
                Timber.e("Taking screenshot!")
                // Schedule the next work after 10 seconds
                handler.postDelayed(this, SCREENSHOT_INTERVAL)
            }
        }
        handler.postDelayed(runnable, SCREENSHOT_INTERVAL)
//
//        if (!keyguardManager.isKeyguardLocked) {
//
//            Timber.e("Taking screenshot!")
//        }
//        val startMediaProjection = registerForActivityResult(
//            StartActivityForResult()
//        ) { result ->
//            if (result.resultCode == RESULT_OK) {
//
//            }
//        }
//        // Start the media projection
//        if (mediaProjection == null) {
//            val virtualDisplay = mediaProjection.createVirtualDisplay(
//                "ScreenCapture",
//                screenWidth,
//                screenHeight,
//                screenDensity,
//                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
//                imageReader.surface,
//                null,
//                null
//            )
//            // Start capturing frames from the virtual display
//            imageReader.setOnImageAvailableListener({ reader ->
//                val image = reader?.acquireLatestImage()
//                // Process the acquired image (convert to Bitmap, save to file, etc.)
//                // Remember to release the image when done: image?.close()
//            }, handler)
//        }
    }



}