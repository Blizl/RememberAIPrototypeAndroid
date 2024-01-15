package com.reality.rememberaiprototype.imagetotext.data

import android.app.Application
import com.reality.rememberaiprototype.home.data.Memory
import com.reality.rememberaiprototype.home.domain.LocalRepository
import com.reality.rememberaiprototype.imagetotext.ImageTextRecognitionService
import com.reality.rememberaiprototype.imagetotext.domain.ImageToTextRepository
import com.reality.rememberaiprototype.processors.domain.TextRecognitionProcessor
import com.reality.rememberaiprototype.utils.isServiceRunning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class DefaultImageToTextRepository @Inject constructor(
    application: Application,
    private val textRecognitionProcessor: TextRecognitionProcessor,
    private val localRepository: LocalRepository
) : ImageToTextRepository, CoroutineScope {

    private val _parsing = MutableStateFlow(isServiceRunning(application.applicationContext, ImageTextRecognitionService::class.java))
    override val parsingState: StateFlow<Boolean> = _parsing

    override suspend fun parseImageToText(bitmapPath: String): String {
        return textRecognitionProcessor.parseText(bitmapPath)
    }

    override suspend fun saveMemory(memory: Memory) {
        return localRepository.saveMemory(memory)
    }

    override suspend fun completeParsing() {
        _parsing.value = false
    }

    override suspend fun startParsing() {
        _parsing.value = true
    }

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO
}