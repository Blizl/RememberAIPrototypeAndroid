package com.reality.rememberaiprototype.home


interface ImageRepository {
    fun fetchSavedImages(): List<String>
}