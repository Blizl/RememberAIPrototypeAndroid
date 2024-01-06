package com.reality.rememberaiprototype.home.domain



interface HomeRepository {
    suspend fun fetchSavedImages(): List<String>

    suspend fun toggleScreenshotRecord(): Result<Boolean>
    suspend fun isScreenshotServiceRunning(): Boolean

    suspend fun getParsedText(bitmapPath: String): String
}