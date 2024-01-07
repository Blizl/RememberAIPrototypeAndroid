package com.reality.rememberaiprototype.home.imagetotext.data

import com.reality.rememberaiprototype.home.data.Memory
import com.reality.rememberaiprototype.home.imagetotext.domain.ImageToTextRepository
import com.reality.rememberaiprototype.home.domain.LocalRepository
import com.reality.rememberaiprototype.processors.domain.TextRecognitionProcessor
import javax.inject.Inject

class DefaultImageToTextRepository @Inject constructor(
    private val textRecognitionProcessor: TextRecognitionProcessor,
    private val localRepository: LocalRepository
): ImageToTextRepository {
    override suspend fun parseImageToText(bitmapPath: String): String {
        return textRecognitionProcessor.parseText(bitmapPath)
    }

    override suspend fun saveMemory(memory: Memory) {
        return localRepository.saveMemory(memory)
    }
}