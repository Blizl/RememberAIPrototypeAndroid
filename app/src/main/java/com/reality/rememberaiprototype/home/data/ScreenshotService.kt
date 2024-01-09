package com.reality.rememberaiprototype.home.data

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer


class ScreenshotService : Service() {

    companion object {
        private const val SCREENSHOT_INTERVAL_SECONDS = 10
        private const val SCREENSHOT_INTERVAL = SCREENSHOT_INTERVAL_SECONDS * 1000L
        private const val NOTIFICATION_ID = 11
        private const val CHANNEL_ID = "SCREENSHOT_CHANNEL_ID"
        const val RECORD_SCREEN_RESULT_CODE = "RECORD_SCREEN_RESULT_CODE"
        const val RECORD_SCREEN_DATA = "RECORD_SCREEN_RESULT_DATA"
        const val MEMORY_DIRECTORY = "TEST_Screenshots"
        const val IMAGE_QUALITY = 100
        val SCREEN_RESOLUTION = Resolution.SD_480
    }

    private var handler = Handler(Looper.getMainLooper())
    private val screenShotRunnable = object : Runnable {
        override fun run() {
            Timber.e("Checking")
            if (!keyguardManager.isKeyguardLocked) {
                Timber.e("Taking screenshot")
                val image = imageReader.acquireLatestImage()
                // store in external directory
                image?.let {
                    try {
                        storeExternally(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        it.close()
                    }
                }
            }

            // Schedule the next work after 10 seconds
            handler.postDelayed(this, SCREENSHOT_INTERVAL)

        }
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
        val handler = Handler(Looper.getMainLooper())
        mediaProjection.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                // Handle the onStop event of the MediaProjection
                // This method is called when the MediaProjection is stopped or released
                handler.removeCallbacks(screenShotRunnable)
                Timber.e("MediaProjection is stopped")
            }
        }, null)
        imageReader.setOnImageAvailableListener({ reader ->
        }, handler)
        mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            screenWidth,
            screenHeight,
            screenDensity.toInt(),
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            object : VirtualDisplay.Callback() {
                override fun onResumed() {
                    Timber.e("VirtualDisplay Resumed - Start capturing frames here")
                }

                override fun onStopped() {
                    Timber.e("VirtualDisplay Stopped")
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
                Timber.e("onCreate(): starting foreground service as foregournd sertice type media projection")
                startForeground(
                    NOTIFICATION_ID, notification,
                    FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                )
            }
        }
    }


    private fun takeScreenshot() {
        // Start capturing frames from the virtual display
        handler.postDelayed(screenShotRunnable, SCREENSHOT_INTERVAL)
    }

    private fun storeExternally(image: Image) {
        Timber.e("Storing externally")
        val externalFilesDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val directory = File(externalFilesDir, MEMORY_DIRECTORY)

        if (!directory.exists()) {
            Timber.e("Directory doesn't exist")
            directory.mkdirs() // Create the directory if it doesn't exist
        }
        val extension = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            "webp"
        } else {
            "jpeg"
        }
        val file = File(directory, "screenshot_${System.currentTimeMillis()}.$extension")
        val planes = image.planes
        val buffer: ByteBuffer = planes[0].buffer
        val pixelStride: Int = planes[0].pixelStride
        val rowStride: Int = planes[0].rowStride
        val rowPadding: Int = rowStride - pixelStride * screenWidth
        val bitmap = Bitmap.createBitmap(
            screenWidth + rowPadding / pixelStride,
            screenHeight,
            Bitmap.Config.ARGB_8888
        );
        bitmap.copyPixelsFromBuffer(buffer)
        val resized = Bitmap.createScaledBitmap(
            bitmap,
            SCREEN_RESOLUTION.width,
            SCREEN_RESOLUTION.height,
            true
        )
        val fileOutputStream = FileOutputStream(file)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            resized.compress(Bitmap.CompressFormat.WEBP_LOSSY, 0, fileOutputStream)
        } else {
            resized.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, fileOutputStream)
        }
        fileOutputStream.close()

        Timber.e("File saved at: ${file.absolutePath}")
    }
}

sealed class Resolution(val width: Int, val height: Int) {
    object LOW_RES_144 : Resolution(width = 144, height = 256)
    object SD_240 : Resolution(width = 240, height = 426)
    object SD_480 : Resolution(width = 480, height = 854)
    object HD_720 : Resolution(720, 1280)
    object HD_1080 : Resolution(width = 1080, height = 1920)
}