package com.reality.rememberaiprototype.processors.data

import android.content.ContentResolver
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.reality.rememberaiprototype.processors.domain.TextRecognitionProcessor
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MlKitTextRecognition @Inject constructor(private val contentResolver: ContentResolver) :
    TextRecognitionProcessor {
    override suspend fun parseText(imagePath: String): String {
        return suspendCancellableCoroutine { continuation ->
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images
                    .Media.getBitmap(contentResolver, imagePath.toUri())

            } else {
                val source = ImageDecoder
                    .createSource(contentResolver, imagePath.toUri())
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
}