package com.reality.rememberaiprototype.home.data

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.core.content.ContextCompat
import com.reality.rememberaiprototype.MainActivity
import com.reality.rememberaiprototype.home.domain.HomeRepository
import com.reality.rememberaiprototype.home.domain.LocalRepository
import com.reality.rememberaiprototype.imagetotext.ImageTextRecognitionService
import com.reality.rememberaiprototype.imagetotext.domain.ImageToTextRepository
import com.reality.rememberaiprototype.utils.isServiceRunning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.io.File
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class DefaultHomeRepository(
    private val application: Application,
    private val localRepo: LocalRepository,
    @Singleton private val imageToTextRepository: ImageToTextRepository,
) : HomeRepository, CoroutineScope {
    companion object {
        const val DIRECTORY_PATH_KEY = "directory_path"
    }
    override val isParsingMemories: StateFlow<Boolean> = imageToTextRepository.parsingState

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO


    override suspend fun fetchSavedImages(): Result<List<Image>> {
        return try {
            val memories = localRepo.fetchAllMemories()
            Timber.e("We got all the memories! size is ${memories.size}")
            Result.success(memories.map { it.toImage() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleScreenCapture(): Result<Boolean> {
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
        Timber.e("in defualt home repository, checking if service is running")
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
        } else {
            Timber.e("Service is running, will not parse images")
        }

    }

    override fun startScreenCapture(data: Intent, resultCode: Int) {
        val serviceIntent =
            Intent(application.applicationContext, ScreenshotService::class.java)
        serviceIntent.putExtra(ScreenshotService.RECORD_SCREEN_RESULT_CODE, resultCode)
        serviceIntent.putExtra(ScreenshotService.RECORD_SCREEN_DATA, data)
        ContextCompat.startForegroundService(application.applicationContext, serviceIntent)
    }

}

