package com.reality.rememberaiprototype.home.data

import android.app.KeyguardManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager


class ScreenshotService : Service() {

    companion object {
        private const val SCREENSHOT_INTERVAL_SECONDS = 10
        private const val SCREENSHOT_INTERVAL = SCREENSHOT_INTERVAL_SECONDS * 1000L
        private const val REQUEST_CODE = 102
    }

    private var mediaProjection: MediaProjection?
    private var imageReader: ImageReader
    private val mediaProjectionManager: MediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    private val windowManager: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val handler: Handler? = null
    private lateinit var keyguardManager: KeyguardManager
//    init {
//        mediaProjection = mediaProjectionManager
//            .getMediaProjection(result.resultCode, result.data!!)
//    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!keyguardManager.isKeyguardLocked) {
            // Use MediaProjection API to capture screenshots here
            // Remember to handle permissions and screen capture logic
            startScreenshotHandler()
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        val displayMetrics = DisplayMetrics()
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        imageReader = ImageReader.newInstance(
            screenWidth, screenHeight,
            PixelFormat.RGBA_8888, 2
        )
    }

    private fun startScreenshotHandler() {
        val handler = Handler(Looper.myLooper()!!)
        handler.postDelayed(object : Runnable {
            override fun run() {
                takeScreenshot()
                handler.postDelayed(this, SCREENSHOT_INTERVAL)
            }
        }, SCREENSHOT_INTERVAL)
    }

    private fun takeScreenshot() {

        val startMediaProjection = registerForActivityResult(
            StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {

            }
        }
        // Start the media projection
        if (mediaProjection == null) {
            val virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenCapture",
                screenWidth,
                screenHeight,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface,
                null,
                null
            )
            // Start capturing frames from the virtual display
            imageReader.setOnImageAvailableListener({ reader ->
                val image = reader?.acquireLatestImage()
                // Process the acquired image (convert to Bitmap, save to file, etc.)
                // Remember to release the image when done: image?.close()
            }, handler)
        }
    }

}