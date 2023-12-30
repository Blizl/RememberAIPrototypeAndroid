package com.reality.rememberaiprototype

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.reality.rememberaiprototype.home.data.ScreenshotWorker
import com.reality.rememberaiprototype.home.presentation.HomeScreen
import com.reality.rememberaiprototype.ui.theme.RememberAIPrototypeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val STORAGE_PERMISSION_CODE = 101
    private val legacyPermissions = arrayOf(
        READ_EXTERNAL_STORAGE,
        WRITE_EXTERNAL_STORAGE,
        MEDIA_PROJECTION_SERVICE
    )
    private val permissions =
        arrayOf(READ_MEDIA_IMAGES, MEDIA_PROJECTION_SERVICE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }

    private fun scheduleWork() {
        val workManager = WorkManager.getInstance(applicationContext)

        val periodicWorkRequest = OneTimeWorkRequest.Builder(ScreenshotWorker::class.java)
            .build()

        workManager.enqueue(periodicWorkRequest)

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                // Enqueue the work after a delay
                workManager.enqueue(periodicWorkRequest)
                // Schedule the next work after 10 seconds
                handler.postDelayed(this, 10 * 1000)
            }
        }

        // 10 seconds
        handler.postDelayed(runnable, 10 * 1000)

    }


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
                permissions,
                STORAGE_PERMISSION_CODE
            )
        } else {
            // Permissions are already granted, proceed with your logic
            // Access external storage here
            scheduleWork()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted by the user
                // Proceed with your logic after permission granted
            } else {
                // Permission is denied by the user
                // Handle this scenario, show a message, or disable functionality
            }
        }
    }
}