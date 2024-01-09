package com.reality.rememberaiprototype.home.presentation

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reality.rememberaiprototype.home.data.Image
import com.reality.rememberaiprototype.home.data.ScreenshotService
import com.reality.rememberaiprototype.home.domain.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: HomeRepository) : ViewModel() {

    private val _state: MutableStateFlow<HomeState> =
        MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _uiAction: MutableStateFlow<HomeUIAction?> = MutableStateFlow(
        null
    )
    val uiAction = _uiAction.asStateFlow()
    private var images: Flow<Result<List<Image>>> = flowOf()

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {
            val recording = repository.isScreenshotServiceRunning()
            images = flowOf(repository.fetchSavedImages())
            images.collect {result ->
                if (result.isSuccess) {
                    result.getOrNull()?.let {
                        val externalFilesDir =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        val directory = File(externalFilesDir, ScreenshotService.MEMORY_DIRECTORY)
                        if (it.isEmpty()) {
                            if (directory.exists()) {
                                parseImagesFromDirectory(directory)
                            } else {
                                onDirectoryDoesNotExist(it, recording)
                            }
                        } else {
                            onImagesReceivedFromDatabase(it, recording)
                        }
                    }

                }
            }
        }

    }

    private suspend fun parseImagesFromDirectory(directory: File) {
        repository.parseImagesFromDirectory(directory)
    }

    private fun onDirectoryDoesNotExist(images: List<Image>, recording: Boolean) {
        setState(state.value.copy(images = images, recording = recording))

    }

    private suspend fun onImagesReceivedFromDatabase(images: List<Image>, recording: Boolean) {
        setState(state.value.copy(images = images, recording = recording))
    }

    fun dispatchEvent(event: HomeUIEvent) {
        when (event) {
            is HomeUIEvent.Search -> onSearchTextChange(event.query)
            is HomeUIEvent.ToggleSearch -> onToggleSearch()
            HomeUIEvent.PrimaryButtonClick -> onPrimaryButtonClick()
            HomeUIEvent.Refresh -> onRefresh()
        }
    }

    private fun onRefresh() {
        viewModelScope.launch {
            images = flowOf(repository.fetchSavedImages())
            images.collect { result ->
                if (result.isSuccess) {
                    setState(state.value.copy(images = result.getOrNull() ?: emptyList()))
                }
            }
        }
    }

    private fun onPrimaryButtonClick() {
        viewModelScope.launch {
            val result = repository.toggleScreenshotRecord()
            if (result.isSuccess) {
                setState(state.value.copy(recording = result.getOrNull() == true))
            }
        }
    }


    private fun onSearchTextChange(text: String) {
        val debounceDuration = 500L // Debounce duration in milliseconds

        // Debounce the user input before processing the images
        val debouncedSearch = images
            .debounce(debounceDuration)
            .map { result ->
                if (result.isSuccess) {
                    result.getOrNull()?.let {
                        val filteredImages = it.filter { it.imageText.lowercase().contains(text) }
                        HomeState(searching = true, searchQuery = text, images = filteredImages)
                    }
                } else {
                    HomeState(searching = false, searchQuery = text, images = emptyList())
                }
            }
            .catch { e ->
                // Handle exceptions if any occur during the search operation
                println("Search failed: ${e.message}")
                // Emit the current state or an empty state in case of failure
                HomeState(searching = false, searchQuery = text, images = emptyList())
            }

        // Launch a coroutine to collect the debounced search results and update the state
        viewModelScope.launch {
            debouncedSearch.collect { newState ->
                newState?.let { setState(it) }
            }
        }
    }

    private fun onToggleSearch() {
        setState(state.value.copy(searching = true, searchQuery = ""))
    }

    private fun setState(newState: HomeState) {
        viewModelScope.launch {
            _state.emit(newState)
        }
    }
}