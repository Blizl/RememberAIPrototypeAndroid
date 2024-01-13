package com.reality.rememberaiprototype.imagetotext

import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import com.reality.rememberaiprototype.home.data.DefaultHomeRepository.Companion.DIRECTORY_PATH_KEY
import com.reality.rememberaiprototype.home.data.Memory
import com.reality.rememberaiprototype.imagetotext.di.DaggerImageTextRecognitionComponent
import com.reality.rememberaiprototype.imagetotext.domain.ImageToTextRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import javax.inject.Inject

@AndroidEntryPoint
class ImageTextRecognitionService: Service(), CoroutineScope by MainScope() {

    @Inject
    lateinit var repository: ImageToTextRepository

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        DaggerImageTextRecognitionComponent.builder().application(application).build().inject(this)
        Timber.e("We just created the image text recogintion service to parse images")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            // Service is being recreated, perform cleanup if needed
            repository.completeParsing()
//            repository.parsingState.value = false
            stopSelf(startId)
            return START_NOT_STICKY
        }
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
        val screenShots = queryScreenshots(directory.name, application.contentResolver)
        Timber.e("Screenshots size is ${screenShots.size}")
        launch(Dispatchers.IO) {
            val deferredResults = screenShots.map { screenshot ->
                val imageUri: String = screenshot.first.toString()
                val filePath: String = screenshot.second
                async(Dispatchers.IO) {
                    val text = repository.parseImageToText(imageUri)
                    val creationTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val path = Paths.get(filePath)
                        val attrs: BasicFileAttributes = Files.readAttributes(path, BasicFileAttributes::class.java)
                        attrs.creationTime().toMillis()
                    } else {
                        getCreationDateFromUri(application, screenshot.first)
                    }
                    Timber.e("Going to try to save")
                    repository.saveMemory(Memory(path = imageUri, content = text, creationDate = creationTime ?: 0))
                }
            }
            deferredResults.awaitAll()
            Timber.e("Completed parsing everything")
            repository.completeParsing()
            stopSelf()
        }



    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
        Timber.e("destorying ImageTextRecognition service ")
    }

    private fun queryScreenshots(folderName: String, contentResolver: ContentResolver): List<Pair<Uri, String>> {
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
            val screenshots = mutableListOf<Pair<Uri, String>>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val imageUri = contentUri.buildUpon().appendPath(id.toString()).build()
                val filePath = getPathFromContentUri(this, imageUri)
                screenshots.add(Pair(imageUri, filePath))
            }
            return screenshots
        }

        return emptyList()
    }

    private fun getPathFromContentUri(context: Context, contentUri: Uri): String {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor? = null

        try {
            cursor = context.contentResolver.query(contentUri, projection, null, null, null)
            cursor?.let {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (it.moveToFirst()) {
                    filePath = it.getString(columnIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return filePath ?: ""
    }

    private fun getCreationDateFromUri(context: Context, uri: Uri): Long? {
        var creationDate: Long? = null
        val projection = arrayOf(MediaStore.Images.Media.DATE_ADDED)
        var cursor: Cursor? = null

        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.let {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                if (it.moveToFirst()) {
                    val seconds = it.getLong(columnIndex)
                    creationDate = seconds * 1000 // Convert seconds to milliseconds
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return creationDate
    }
}