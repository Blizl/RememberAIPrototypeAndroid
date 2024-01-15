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
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
//    val _isParsingMemories: MutableStateFlow<Boolean> = imageToTextRepository.parsingState
    override val isParsingMemories: StateFlow<Boolean> = imageToTextRepository.parsingState

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO


    init {
        Timber.e("we are in init of defaultHomerepository, imageTextrepo is $imageToTextRepository")
//        launch {
//            imageToTextRepository.parsingState.collect {
//                Timber.e("We got a new parsing state from iamgeToTextRepo, setting to $it")
//                isParsingMemories.value = it
//            }
//        }
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

}

