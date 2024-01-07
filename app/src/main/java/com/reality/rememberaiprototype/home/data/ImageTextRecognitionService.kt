package com.reality.rememberaiprototype.home.data

import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.provider.MediaStore
import com.reality.rememberaiprototype.home.data.DefaultHomeRepository.Companion.DIRECTORY_PATH_KEY
import com.reality.rememberaiprototype.home.data.ScreenshotService.Companion.MEMORY_DIRECTORY
import com.reality.rememberaiprototype.home.di.DaggerImageTextRecognitionComponent
import com.reality.rememberaiprototype.home.domain.HomeRepository
import com.reality.rememberaiprototype.home.domain.TextRecognitionProcessor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ImageTextRecognitionService: Service(), CoroutineScope by MainScope() {

    @Inject
    lateinit var homeRepository: HomeRepository


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        DaggerImageTextRecognitionComponent.builder().application(application).build().inject(this)
        Timber.e("We just created the image text recogintion service to parse images")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val filePath = intent?.extras?.getString(DIRECTORY_PATH_KEY)
        Timber.e("we got the intent filepath is ${filePath}")
        val directory = filePath?.let { File(it) }
        Timber.e("directory exists: ${directory?.exists()}")
        directory?.let {
            processImagesToText(directory)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun processImagesToText(directory: File) {
        val screenShots = queryScreenshots(directory.name, application.contentResolver).map { it.toString() }
        Timber.e("Screenshots size is ${screenShots.size}")
        for (screenshot in screenShots) {
            launch(Dispatchers.IO) {
                val text = homeRepository.parseImageToText(screenshot)
                Timber.e("Parsed text is ${text}")
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
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
            return screenshots
        }

        return emptyList()
    }
}