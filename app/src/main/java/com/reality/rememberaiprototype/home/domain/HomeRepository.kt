package com.reality.rememberaiprototype.home.domain

import com.reality.rememberaiprototype.home.data.Image
import java.io.File


interface HomeRepository {
    suspend fun fetchSavedImages(): Result<List<Image>>

    suspend fun toggleScreenshotRecord(): Result<Boolean>
    suspend fun isScreenshotServiceRunning(): Boolean

    suspend fun parseImageToText(bitmapPath: String): String
    suspend fun parseImagesFromDirectory(directory: File)

}