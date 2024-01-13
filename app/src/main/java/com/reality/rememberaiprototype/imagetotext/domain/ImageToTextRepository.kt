package com.reality.rememberaiprototype.imagetotext.domain

import com.reality.rememberaiprototype.home.data.Memory
import kotlinx.coroutines.flow.StateFlow

interface ImageToTextRepository {
    suspend fun parseImageToText(bitmapPath: String): String
    suspend fun saveMemory(memory: Memory)

    val parsingState: StateFlow<Boolean>
    fun completeParsing()

}