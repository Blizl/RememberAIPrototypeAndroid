package com.reality.rememberaiprototype.home.presentation

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import timber.log.Timber

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val uiAction by viewModel.uiAction.collectAsState()
    var refreshing by remember { mutableStateOf(false) }
    var showParseDirectoryDialog by remember { mutableStateOf(false) }
    val refreshState = rememberPullRefreshState(refreshing, {
        viewModel.dispatchEvent(HomeUIEvent.Refresh)
    })
    when (uiAction) {
        HomeUIAction.ShowParseDirectory -> showParseDirectoryDialog = true
        HomeUIAction.HideParseDirectory -> showParseDirectoryDialog = false
        null -> {}
    }
    Box(modifier = Modifier.fillMaxSize()){
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "History", modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center, fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            if (state.parsing) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                    )
                    Text(
                        "Currently parsing screenshots from directory for searching",
                        fontSize = 12.sp,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = { viewModel.dispatchEvent(HomeUIEvent.Search(it)) },
                        onSearch = { viewModel.dispatchEvent(HomeUIEvent.Search(it)) },
                        active = state.searching,
                        onActiveChange = { viewModel.dispatchEvent(HomeUIEvent.ToggleSearch) },
                        placeholder = { Text("Search here") },
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Box(Modifier.pullRefresh(refreshState)) {
                            LazyColumn {
                                items(state.images.size) {
                                    ImageFromFile(
                                        filePath = state.images[it].imagePath.toUri(),
                                        LocalContext.current.contentResolver
                                    )
                                    Spacer(modifier = Modifier.padding(vertical = 24.dp))
                                }
                            }

                            PullRefreshIndicator(
                                refreshing,
                                refreshState,
                                Modifier.align(Alignment.TopCenter)
                            )
                        }
                    }
                }
                Button(
                    onClick = { viewModel.dispatchEvent(HomeUIEvent.PrimaryButtonClick) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    if (state.recording) Text("Stop Recording") else Text("Start Recording")
                }
            }
        }

        if (showParseDirectoryDialog) {
            ParseDirectoryDialog({
                viewModel.dispatchEvent(HomeUIEvent.HideParseDirectory)
            })
        }
    }
}

@Composable
fun ParseDirectoryDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "No Directory Found")
        },
        text = {
            Text("Would you like t")
        },
        confirmButton = {
            Button(
                onClick = {
//                    openDialog.value = false
                    onDismiss()
                }) {
                Text("Confirm Button")
            }
        },
        dismissButton = {
            Button(

                onClick = {
//                    openDialog.value = false
                    onDismiss()
                }) {
                Text("Dismiss Button")
            }
        }
    )
}


fun showParseDirectoryDialog() {

}

@Composable
fun ImageFromFile(filePath: Uri, contentResolver: ContentResolver) {
    val originalBitmap = loadBitmap(filePath, contentResolver)
    Image(
        bitmap = cropBitmapToHalfHeight(originalBitmap).asImageBitmap(),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            // Divide the height by 2 since we cropped it half
            .aspectRatio(originalBitmap.width.toFloat() / (originalBitmap.height / 2))
            .clip(RoundedCornerShape(8.dp))
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