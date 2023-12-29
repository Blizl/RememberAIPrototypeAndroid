package com.reality.rememberaiprototype.home

class DefaultImageRepository : ImageRepository {

    override fun fetchSavedImages(): List<String> {
        // return a list of images saved locally
        return listOf()
    }
}