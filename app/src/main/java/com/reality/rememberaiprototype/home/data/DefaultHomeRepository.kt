package com.reality.rememberaiprototype.home.data

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.reality.rememberaiprototype.MainActivity
import com.reality.rememberaiprototype.home.domain.HomeRepository
import com.reality.rememberaiprototype.home.domain.LocalRepository
import com.reality.rememberaiprototype.imagetotext.ImageTextRecognitionService
import com.reality.rememberaiprototype.imagetotext.domain.ImageToTextRepository
import com.reality.rememberaiprototype.utils.isServiceRunning
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.io.File
import javax.inject.Singleton

@Singleton
class DefaultHomeRepository(
    private val application: Application,
    private val localRepo: LocalRepository,
    private val imageToTextRepository: ImageToTextRepository
) : HomeRepository {
    companion object {
        const val DIRECTORY_PATH_KEY = "directory_path"
    }

    override suspend fun fetchSavedImages(): Result<List<Image>> {
        return try {
            val memories = localRepo.fetchAllMemories()
            Timber.e("We got all the memories! size is ${memories.size}")
            Result.success(memories.map { it.toImage() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleScreenshotRecord(): Result<Boolean> {
        Timber.e("Starting foreground service")
        return try {
            val serviceIntent =
                Intent(application.applicationContext, ScreenshotService::class.java)
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

    override suspend fun parseImagesFromDirectory(directory: File) {
        if (!isServiceRunning(
                application.applicationContext,
                ImageTextRecognitionService::class.java
            )
        ) {
            val serviceIntent =
                Intent(application.applicationContext, ImageTextRecognitionService::class.java)
            serviceIntent.putExtra(DIRECTORY_PATH_KEY, directory.path)
            Timber.e("Starting service to parse images")
            application.startService(serviceIntent)
        }

    }

    override suspend fun isParsingMemories(): StateFlow<Boolean> {
        return imageToTextRepository.parsingState
    }
}

