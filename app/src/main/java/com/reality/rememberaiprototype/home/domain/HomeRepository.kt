package com.reality.rememberaiprototype.home.domain

import java.io.File


interface HomeRepository {
    suspend fun fetchSavedImages(): Result<List<String>>

    suspend fun toggleScreenshotRecord(): Result<Boolean>
    suspend fun isScreenshotServiceRunning(): Boolean

    suspend fun getParsedText(bitmapPath: String): String
    suspend fun parseImagesFromDirectory(directory: File)

}