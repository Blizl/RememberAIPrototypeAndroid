package com.reality.rememberaiprototype.imagetotext.data

import android.app.Application
import com.reality.rememberaiprototype.home.data.Memory
import com.reality.rememberaiprototype.imagetotext.domain.ImageToTextRepository
import com.reality.rememberaiprototype.home.domain.LocalRepository
import com.reality.rememberaiprototype.imagetotext.ImageTextRecognitionService
import com.reality.rememberaiprototype.processors.domain.TextRecognitionProcessor
import com.reality.rememberaiprototype.utils.isServiceRunning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultImageToTextRepository @Inject constructor(
    private val application: Application,
    private val textRecognitionProcessor: TextRecognitionProcessor,
    private val localRepository: LocalRepository
): ImageToTextRepository {

    private val _parsing = MutableStateFlow(isServiceRunning(application.applicationContext, ImageTextRecognitionService::class.java))
    override val parsingState: StateFlow<Boolean> =  _parsing

    override suspend fun parseImageToText(bitmapPath: String): String {
        return textRecognitionProcessor.parseText(bitmapPath)
    }

    override suspend fun saveMemory(memory: Memory) {
        return localRepository.saveMemory(memory)
    }

    override fun completeParsing() {
        _parsing.value = false
        Timber.e("complete parsing! parsing.value is ${parsingState.value}")
        Timber.e("complete parsing! parsing is ${parsingState}")
    }
}