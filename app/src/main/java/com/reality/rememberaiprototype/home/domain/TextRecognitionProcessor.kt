package com.reality.rememberaiprototype.home.domain

interface TextRecognitionProcessor {
    suspend fun parseText(imagePath: String): String
}