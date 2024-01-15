package com.reality.rememberaiprototype.home.domain

import android.content.Intent
import com.reality.rememberaiprototype.home.data.Image
import kotlinx.coroutines.flow.StateFlow
import java.io.File


interface HomeRepository {
    suspend fun fetchSavedImages(): Result<List<Image>>

    suspend fun toggleScreenCapture(): Result<Boolean>
    suspend fun isScreenshotServiceRunning(): Boolean

    suspend fun parseImagesFromDirectory(directory: File)
    fun startScreenCapture(data: Intent, resultCode: Int)

    val isParsingMemories: StateFlow<Boolean>

}