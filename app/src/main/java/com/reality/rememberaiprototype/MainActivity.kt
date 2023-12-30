package com.reality.rememberaiprototype

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.reality.rememberaiprototype.home.presentation.HomeScreen
import com.reality.rememberaiprototype.ui.theme.RememberAIPrototypeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val STORAGE_PERMISSION_CODE = 101
    private val permissions = arrayOf(
        READ_EXTERNAL_STORAGE,
        WRITE_EXTERNAL_STORAGE
    )

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(READ_MEDIA_IMAGES),
                STORAGE_PERMISSION_CODE
            )
            return

        }
        var isAllPermissionsGranted = true
        for (permission in permissions) {
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