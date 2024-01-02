package com.reality.rememberaiprototype.home.data

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import timber.log.Timber


class ScreenshotService : Service() {

    companion object {
        private const val SCREENSHOT_INTERVAL_SECONDS = 10
        private const val SCREENSHOT_INTERVAL = SCREENSHOT_INTERVAL_SECONDS * 1000L
        private const val NOTIFICATION_ID = 11
        private const val CHANNEL_ID = "SCREENSHOT_CHANNEL_ID"
        const val RECORD_SCREEN_RESULT_CODE = "RECORD_SCREEN_RESULT_CODE"
        const val RECORD_SCREEN_DATA = "RECORD_SCREEN_RESULT_DATA"
    }

    private var screenDensity: Float = 0f
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var recordScreenResultCode: Int = 0
    private lateinit var mediaProjection: MediaProjection
    private lateinit var imageReader: ImageReader
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var keyguardManager: KeyguardManager
    private lateinit var windowManager: WindowManager

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.e("Starting screenshot service")
        mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        recordScreenResultCode = intent?.getIntExtra(RECORD_SCREEN_RESULT_CODE, 0) ?: 0
        val recordScreenData: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(RECORD_SCREEN_DATA, Intent::class.java)
        } else {
            intent?.getParcelableExtra(RECORD_SCREEN_DATA)
        }
        mediaProjection = mediaProjectionManager.getMediaProjection(
            recordScreenResultCode,
            recordScreenData!!
        )
        mediaProjection.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                // Handle the onStop event of the MediaProjection
                // This method is called when the MediaProjection is stopped or released
                Timber.e("MediaProjection is stopped")
            }
        }, null)
        val handler = Handler(Looper.getMainLooper())
        imageReader.setOnImageAvailableListener({ reader ->
        }, handler)
        val virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            screenWidth,
            screenHeight,
            screenDensity.toInt(),
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            object : VirtualDisplay.Callback() {
                override fun onResumed() {
                    Timber.e("VirtualDisplay Resumed - Start capturing frames here")
                    // Start capturing frames or handle capture in this callback
                }

                override fun onStopped() {
                    Timber.e("VirtualDisplay Stopped")
                    // Clean up resources when capturing stops
                }
            },
            handler
        )
        takeScreenshot()
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        val displayMetrics = DisplayMetrics()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
        screenDensity = displayMetrics.density
        Timber.e("screenWidth is $screenWidth, screenHeight is $screenHeight, screenDensity is $screenDensity")
        keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        imageReader = ImageReader.newInstance(
            screenWidth, screenHeight,
            PixelFormat.RGBA_8888, 2
        )


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
                Timber.e("onCreate(): starting foreground service")
                startForeground(NOTIFICATION_ID, notification)
            } else {
                Timber.e("onCreaete(): starting foreground service as foregournd sertice type media projection")
                startForeground(
                    NOTIFICATION_ID, notification,
                    FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                )
            }
        }
    }


    private fun takeScreenshot() {
        val handler = Handler(Looper.getMainLooper())
        // Start capturing frames from the virtual display
        val runnable = object : Runnable {
            override fun run() {
                Timber.e("Taking screenshot")
//                val image = imageReader.acquireLatestImage()
//                image.close()

                // Schedule the next work after 10 seconds
                handler.postDelayed(this, SCREENSHOT_INTERVAL)
            }
        }
        handler.postDelayed(runnable, SCREENSHOT_INTERVAL)
    }
}