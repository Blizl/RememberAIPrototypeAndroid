package com.reality.rememberaiprototype.home.imagetotext.domain

import com.reality.rememberaiprototype.home.data.Memory

interface ImageToTextRepository {
    suspend fun parseImageToText(bitmapPath: String): String
    suspend fun saveMemory(memory: Memory)

}