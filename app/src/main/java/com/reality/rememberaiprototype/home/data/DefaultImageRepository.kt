package com.reality.rememberaiprototype.home.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.reality.rememberaiprototype.home.domain.ImageRepository

class DefaultImageRepository(val contentResolver: ContentResolver): ImageRepository {

    override suspend fun fetchSavedImages(): List<String> {
        // return a list of images saved locally
        return queryScreenshots("Screenshots", contentResolver).map{ it.toString()}
    }

    private fun queryScreenshots(folderName: String, contentResolver: ContentResolver): List<Uri> {
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(folderName)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )

        contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val screenshots = mutableListOf<Uri>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val imageUri = contentUri.buildUpon().appendPath(id.toString()).build()
                screenshots.add(imageUri)
            }
            Log.e("Test", "there are ${screenshots.size} screenshots")
            return screenshots
        }

        return emptyList()
    }
}