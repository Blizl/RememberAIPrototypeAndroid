package com.reality.rememberaiprototype.home.data

import com.reality.rememberaiprototype.home.domain.ImageRepository

class DefaultImageRepository : ImageRepository {

    override suspend fun fetchSavedImages(): List<String> {
        // return a list of images saved locally
        return listOf("/sdcard/Pictures/ScreenShots/Screenshot_20231229-131738.png")
    }
}