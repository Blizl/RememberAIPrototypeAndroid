package com.reality.rememberaiprototype.home.presentation

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val uiAction by viewModel.uiAction.collectAsState()

    when (uiAction) {
        else -> {}
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = {viewModel.dispatchEvent(HomeUIEvent.PrimaryButtonClick)}, modifier = Modifier.fillMaxWidth()) {
            if (state.recording) Text("Stop Recording") else Text("Start Recording")
        }
        Text(
            "History", modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center, fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { viewModel.dispatchEvent(HomeUIEvent.Search(it)) },
            onSearch = { viewModel.dispatchEvent(HomeUIEvent.Search(it)) },
            active = state.searching,
            onActiveChange = { viewModel.dispatchEvent(HomeUIEvent.ToggleSearch) },
            placeholder = {Text("Search here")},
            modifier = Modifier
                .fillMaxWidth()
        ) {
            LazyColumn {
                items(state.images.size) {
                    ImageFromFile(
                        filePath = state.images[it].toUri(),
                        LocalContext.current.contentResolver
                    )
                    Spacer(modifier = Modifier.padding(vertical = 24.dp))
                }
            }
        }
    }
}

@Composable
fun ImageFromFile(filePath: Uri, contentResolver: ContentResolver) {
    Image(
        bitmap = cropBitmapToHalfHeight(loadBitmap(filePath, contentResolver)).asImageBitmap(),
        contentDescription = null, // Provide content description if needed
        modifier = Modifier.fillMaxWidth()
    )
}

fun cropBitmapToHalfHeight(originalBitmap: Bitmap): Bitmap {
    val width = originalBitmap.width
    val height = originalBitmap.height

    // Define the new dimensions for the cropped bitmap
    val newHeight = height / 2 // Half of the original height

    // Crop the original bitmap to the new dimensions
    val croppedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, width, newHeight)

    // Ensure the original bitmap and the cropped bitmap are not the same instance
    if (croppedBitmap !== originalBitmap) {
        originalBitmap.recycle() // Release the original bitmap resources if not used anymore
    }

    return croppedBitmap
}


private fun loadBitmap(uri: Uri, contentResolver: ContentResolver): Bitmap {
    return if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images
            .Media.getBitmap(contentResolver, uri)

    } else {
        val source = ImageDecoder
            .createSource(contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}


@Composable
@Preview(showBackground = true)
fun HomeScreenPreview() {
    HomeScreen()
}