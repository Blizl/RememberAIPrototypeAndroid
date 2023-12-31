package com.reality.rememberaiprototype.home.data

import android.Manifest
import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.reality.rememberaiprototype.MainActivity
import com.reality.rememberaiprototype.R
import timber.log.Timber


class ScreenshotService : Service() {

    companion object {
        private const val SCREENSHOT_INTERVAL_SECONDS = 10
        private const val SCREENSHOT_INTERVAL = SCREENSHOT_INTERVAL_SECONDS * 1000L
        private const val REQUEST_CODE = 102
        private const val NOTIFICATION_ID = 11
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
            val notification = createNotification(this) // Create your foreground notification
            startForeground(10, notification)
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

//    fun createNotification(context: Context): Notification? {
//        // Create an intent for the notification to open an activity when clicked
//        val notificationIntent = Intent(context, ScreenshotService::class.java)
//        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, FLAG_IMMUTABLE)
//
//        // Build the notification
//        val builder = NotificationCompat.Builder(context, "Remember_Channel_id")
//            .setSmallIcon(R.drawable.ic_launcher_background)
//            .setContentTitle("Remember AI Screenshot")
//            .setContentText("Screenshot in progress!")
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true) // Dismiss the notification when clicked
//
//        // Show the notification
//        val notificationManager = NotificationManagerCompat.from(context)
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return null
//        }
//        notificationManager.notify(NOTIFICATION_ID, builder.build())
//
//        return builder.build()
//    }


    fun createNotification(context: Context): Notification? {
        // Create an intent for the notification to open an activity when clicked
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "Remember_Channel_id"
            val channelName = "Remember AI Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Description of the channel"
                // Customize other channel settings if needed
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
        val notificationIntent = Intent(context, ScreenshotService::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        // Build the notification
        val builder = NotificationCompat.Builder(context, "Remember_Channel_id")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Remember AI Screenshot")
            .setContentText("Screenshot in progress!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss the notification when clicked

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(NOTIFICATION_ID, builder.build())

        return builder.build()
    }


}