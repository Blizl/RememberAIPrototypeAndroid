package com.reality.rememberaiprototype.processors.domain

interface TextRecognitionProcessor {
    suspend fun parseText(imagePath: String): String
}