package com.reality.rememberaiprototype.home.data

import android.app.ActivityManager
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.reality.rememberaiprototype.MainActivity
import com.reality.rememberaiprototype.home.domain.HomeRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DefaultHomeRepository(
    val contentResolver: ContentResolver,
    val application: Application,
) : HomeRepository {

    override suspend fun fetchSavedImages(): List<String> {
        // return a list of images saved locally
        return queryScreenshots("Screenshots", contentResolver).map { it.toString() }
    }

    override suspend fun toggleScreenshotRecord(): Result<Boolean> {
        Timber.e("Starting foreground service")
        return try {
            val serviceIntent = Intent(application.applicationContext, ScreenshotService::class.java)
            if (isServiceRunning(application, ScreenshotService::class.java)) {
                application.stopService(serviceIntent)
                Result.success(false)
            } else {
                val mainActivityIntent = Intent(application, MainActivity::class.java)
                mainActivityIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                mainActivityIntent.putExtra("RECORD_SCREEN_PERMISSION", true)
                application.startActivity(mainActivityIntent)
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isScreenshotServiceRunning(): Boolean {
        return isServiceRunning(application, ScreenshotService::class.java)
    }

    override suspend fun getParsedText(bitmapPath: String): String {
        return suspendCancellableCoroutine { continuation ->
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images
                    .Media.getBitmap(contentResolver, bitmapPath.toUri())

            } else {
                val source = ImageDecoder
                    .createSource(contentResolver, bitmapPath.toUri())
                ImageDecoder.decodeBitmap(source)
            }
            val image = InputImage.fromBitmap(bitmap, 0)
            val options = TextRecognizerOptions.Builder().build()
            val recognizer = TextRecognition.getClient(options)
            recognizer.process(image)
                .addOnSuccessListener { text ->
                    continuation.resume(text.text)
            }.addOnFailureListener {
                    continuation.resumeWithException(it)
            }
        }
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

    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = manager.getRunningServices(Integer.MAX_VALUE)

        for (serviceInfo in runningServices) {
            if (serviceClass.name == serviceInfo.service.className) {
                return true
            }
        }
        return false
    }
}