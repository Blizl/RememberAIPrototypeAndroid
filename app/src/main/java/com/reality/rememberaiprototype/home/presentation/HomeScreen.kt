package com.reality.rememberaiprototype.home.presentation

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.reality.rememberaiprototype.R
import com.reality.rememberaiprototype.home.data.ScreenshotService


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel(), requestPermission: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()
    val uiAction by viewModel.uiAction.collectAsState()
    val refreshing by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showParseDirectoryDialog by remember { mutableStateOf(false) }
    val refreshState = rememberPullRefreshState(refreshing, {
        viewModel.dispatchEvent(HomeUIEvent.Refresh)
    })
    when (uiAction) {
        HomeUIAction.ShowParseDirectory -> showParseDirectoryDialog = true
        HomeUIAction.HideParseDirectory -> showParseDirectoryDialog = false
        HomeUIAction.ShowGenericError -> Toast.makeText(
            LocalContext.current,
            stringResource(R.string.something_went_wrong),
            Toast.LENGTH_SHORT
        ).show()

        is HomeUIAction.ShowError -> Toast.makeText(
            LocalContext.current,
            (uiAction as HomeUIAction.ShowError).message,
            Toast.LENGTH_SHORT
        ).show()

        null -> {}
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                stringResource(R.string.history), modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center, fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if (state.showPermissionsDenied) {
                PermissionDeniedScreen(requestPermission)
            } else if (state.parsing) {
                ParsingScreen()
            } else if (state.hasEmptyMemoriesDirectory) {
                Box {
                    EmptyImagesScreen(state.recording) { viewModel.dispatchEvent(HomeUIEvent.PrimaryButtonClick) }
                    ParseDirectoryDialog(showParseDirectoryDialog, {
                        viewModel.dispatchEvent(HomeUIEvent.ParseDirectoryClosed)
                    }, {
                        viewModel.dispatchEvent(HomeUIEvent.ParseMemoriesFromDirectory)
                    })
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = {
                            searchQuery = it
                            viewModel.dispatchEvent(HomeUIEvent.Search(it))
                        },
                        onSearch = { viewModel.dispatchEvent(HomeUIEvent.Search(it)) },
                        active = state.searching,
                        onActiveChange = {
                            viewModel.dispatchEvent(HomeUIEvent.ToggleSearch)
                        },
                        placeholder = { Text(stringResource(R.string.search_here)) },
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        RecordingButton(recording = state.recording) {
                            viewModel.dispatchEvent(HomeUIEvent.PrimaryButtonClick)
                        }
                    }

                    ParseDirectoryDialog(showParseDirectoryDialog, {
                        viewModel.dispatchEvent(HomeUIEvent.ParseDirectoryClosed)
                    }, {
                        viewModel.dispatchEvent(HomeUIEvent.ParseMemoriesFromDirectory)
                    })
                }
            }

        }
    }
}

@Composable
fun PermissionDeniedScreen(requestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.permission_needed),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = {
                requestPermission()
            }
        ) {
            Text(stringResource(R.string.add_permissions))
        }
    }
}

@Composable
fun ParsingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
        )
        Text(
            stringResource(R.string.currently_parsing_screenshots_from_directory_for_searching),
            fontSize = 12.sp,
        )
    }
}

@Composable
fun EmptyImagesScreen(recording: Boolean, onRecordToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "No memories yet! Click on 'Start Recording' to remember history",
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .align(Alignment.BottomCenter)
        ) {
            RecordingButton(recording = recording, onRecordToggle = onRecordToggle)
        }
    }
}

@Composable
fun RecordingButton(recording: Boolean, onRecordToggle: () -> Unit) {
    Button(
        onClick = { onRecordToggle() },
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if (recording) Text(stringResource(R.string.stop_recording)) else Text(
            stringResource(R.string.start_recording)
        )
    }
}

@Composable
fun ParseDirectoryDialog(show: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    if (show) {
        AlertDialog(
            backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
            onDismissRequest = { onDismiss() },
            title = {
                Text(
                    text = stringResource(R.string.no_directory_found),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    stringResource(
                        R.string.would_you_like_to_parse_images_from,
                        ScreenshotService.MEMORY_DIRECTORY
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                    }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        onDismiss()
                    }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }
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