package com.reality.rememberaiprototype.home.data

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.reality.rememberaiprototype.MainActivity
import com.reality.rememberaiprototype.home.domain.HomeRepository
import com.reality.rememberaiprototype.home.domain.LocalRepository
import com.reality.rememberaiprototype.processors.domain.TextRecognitionProcessor
import com.reality.rememberaiprototype.imagetotext.ImageTextRecognitionService
import timber.log.Timber
import java.io.File

class DefaultHomeRepository(
    private val application: Application,
    private val localRepo: LocalRepository,
    private val textRecognitionProcessor: TextRecognitionProcessor
) : HomeRepository {
    companion object {
        const val DIRECTORY_PATH_KEY = "directory_path"
    }

    override suspend fun fetchSavedImages(): Result<List<Image>> {
        return try {
            val memories = localRepo.fetchAllMemories()
            Timber.e("We got all the memories! size is ${memories.size}")
            Result.success(memories.map {it.toImage()})
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleScreenshotRecord(): Result<Boolean> {
        Timber.e("Starting foreground service")
        return try {
            val serviceIntent = Intent(application.applicationContext, ScreenshotService::class.java)
            if (isServiceRunning(application, ScreenshotService::class.java)) {
                application.stopService(serviceIntent)
                Result.success(false)
            } else {
                val mainActivityIntent = Intent(application, MainActivity::class.java)
                mainActivityIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                mainActivityIntent.putExtra("RECORD_SCREEN_PERMISSION", true)
                application.startActivity(mainActivityIntent)
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isScreenshotServiceRunning(): Boolean {
        return isServiceRunning(application, ScreenshotService::class.java)
    }

    override suspend fun parseImageToText(bitmapPath: String): String {
        return textRecognitionProcessor.parseText(bitmapPath)
    }

    override suspend fun parseImagesFromDirectory(directory: File) {
        val serviceIntent = Intent(application.applicationContext, ImageTextRecognitionService::class.java)
        serviceIntent.putExtra(DIRECTORY_PATH_KEY, directory.path)
        application.startService(serviceIntent)
    }

    override suspend fun isParsingMemories(): Boolean {
        return isServiceRunning(application.applicationContext, ImageTextRecognitionService::class.java)
    }


    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = manager.getRunningServices(Integer.MAX_VALUE)

        for (serviceInfo in runningServices) {
            if (serviceClass.name == serviceInfo.service.className) {
                return true
            }
        }
        return false
    }
}

