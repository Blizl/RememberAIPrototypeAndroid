package com.reality.rememberaiprototype.home.domain


interface ImageRepository {
    suspend fun fetchSavedImages(): List<String>

    suspend fun toggleScreenshotRecord(): Result<Boolean>
    suspend fun isScreenshotServiceRunning(): Boolean
}