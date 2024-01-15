package com.reality.rememberaiprototype

import android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION
import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reality.rememberaiprototype.home.data.ScreenshotService
import com.reality.rememberaiprototype.home.data.ScreenshotService.Companion.RECORD_SCREEN_DATA
import com.reality.rememberaiprototype.home.data.ScreenshotService.Companion.RECORD_SCREEN_RESULT_CODE
import com.reality.rememberaiprototype.home.presentation.HomeScreen
import com.reality.rememberaiprototype.home.presentation.HomeUIEvent
import com.reality.rememberaiprototype.home.presentation.HomeViewModel
import com.reality.rememberaiprototype.imagetotext.domain.ImageToTextRepository
import com.reality.rememberaiprototype.ui.theme.RememberAIPrototypeTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels()

    private val PERMISSION_CODE = 101
    private val REQUEST_CODE_SCREEN_CAPTURE = 102
    private val legacyPermissions = arrayOf(
        READ_EXTERNAL_STORAGE,
        WRITE_EXTERNAL_STORAGE,
        MEDIA_PROJECTION_SERVICE,
        POST_NOTIFICATIONS,
        FOREGROUND_SERVICE_MEDIA_PROJECTION,
        MANAGE_EXTERNAL_STORAGE
    )
    private val permissions =
        arrayOf(
            READ_MEDIA_IMAGES,
            MEDIA_PROJECTION_SERVICE,
            POST_NOTIFICATIONS,
            FOREGROUND_SERVICE_MEDIA_PROJECTION,
            MANAGE_EXTERNAL_STORAGE
        )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            val serviceIntent =
                Intent(application.applicationContext, ScreenshotService::class.java)
            serviceIntent.putExtra(RECORD_SCREEN_RESULT_CODE, resultCode)
            serviceIntent.putExtra(RECORD_SCREEN_DATA, data)
            ContextCompat.startForegroundService(application.applicationContext, serviceIntent)
        }
    }

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
                viewModel.initialize()
            } else {
                // Permission is denied by the user
                // Handle this scenario, show a message, or disable functionality
                viewModel.dispatchEvent(HomeUIEvent.PermissionsDenied)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            val askToRecordScreen = it.extras?.getBoolean("RECORD_SCREEN_PERMISSION", false)
            if (askToRecordScreen == true) {
                requestRecordScreenPermission()
            }
        }
    }

    private fun requestRecordScreenPermission() {
        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val mediaProjectionPermission = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(mediaProjectionPermission, REQUEST_CODE_SCREEN_CAPTURE)
    }

}