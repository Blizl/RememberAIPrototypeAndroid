package com.reality.rememberaiprototype.home.domain


interface ImageRepository {
    suspend fun fetchSavedImages(): List<String>
}