package com.reality.rememberaiprototype

import android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION
import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.reality.rememberaiprototype.home.data.ScreenshotService
import com.reality.rememberaiprototype.home.data.ScreenshotWorker
import com.reality.rememberaiprototype.home.presentation.HomeScreen
import com.reality.rememberaiprototype.ui.theme.RememberAIPrototypeTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val PERMISSION_CODE = 101
    private val REQUEST_CODE_SCREEN_CAPTURE = 102
    private val legacyPermissions = arrayOf(
        READ_EXTERNAL_STORAGE,
        WRITE_EXTERNAL_STORAGE,
        MEDIA_PROJECTION_SERVICE,
        POST_NOTIFICATIONS,
        FOREGROUND_SERVICE_MEDIA_PROJECTION,
    )
    private val permissions =
        arrayOf(READ_MEDIA_IMAGES, MEDIA_PROJECTION_SERVICE, POST_NOTIFICATIONS, FOREGROUND_SERVICE_MEDIA_PROJECTION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.e("MainActivity onCreate was called")
        setContent {
            RememberAIPrototypeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()

        startActivityForResult(permissionIntent, REQUEST_CODE_SCREEN_CAPTURE)
    }

//    private fun scheduleWork() {
//        Log.e("Test", "setting up work manager")
//        val workManager = WorkManager.getInstance(applicationContext)
//
//        val oneTimeRequest = OneTimeWorkRequest.Builder(ScreenshotWorker::class.java)
//            .build()
//
//
//        val handler = Handler(Looper.getMainLooper())
//        val runnable = object : Runnable {
//            override fun run() {
//                // Enqueue the work after a delay
//                Log.e("Test", "enqueuing work manager")
//                workManager.enqueue(oneTimeRequest)
//                // Schedule the next work after 10 seconds
//                handler.postDelayed(this, 10 * 1000)
//            }
//        }
//
//        // 10 seconds
//        Log.e("Test", "posting delayed")
//        handler.postDelayed(runnable, 10 * 1000)
//    }


    private fun checkPermissions() {
        val permissionsToCheck =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissions else legacyPermissions

        var isAllPermissionsGranted = true
        for (permission in permissionsToCheck) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                isAllPermissionsGranted = false
                break
            }
        }

        if (!isAllPermissionsGranted) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToCheck,
                PERMISSION_CODE
            )
        } else {
            // Permissions are already granted, proceed with your logic
            // Access external storage here
            Timber.e("Scheduling work since permissions already granted")
            val serviceIntent = Intent(applicationContext, ScreenshotService::class.java)
            ContextCompat.startForegroundService(applicationContext, serviceIntent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted by the user
                // Proceed with your logic after permission granted
                Timber.e( "Permission has been granted by user")
                Timber.e("Starting foreground service")
                val serviceIntent = Intent(applicationContext, ScreenshotService::class.java)
                ContextCompat.startForegroundService(applicationContext, serviceIntent)
            } else {
                // Permission is denied by the user
                // Handle this scenario, show a message, or disable functionality

                Timber.e( "Permission denied by user")
            }
        }
    }

}