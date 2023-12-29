package com.reality.rememberaiprototype.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reality.rememberaiprototype.home.domain.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(val repository: ImageRepository) : ViewModel() {

    private val _state: MutableStateFlow<HomeState> =
        MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _uiAction: MutableStateFlow<HomeUIAction?> = MutableStateFlow(
        null
    )
    val uiAction = _uiAction.asStateFlow()
    private var images: Flow<List<String>> = flowOf()

    init {
        fetchSavedImages()
    }

    private fun fetchSavedImages() {
        viewModelScope.launch {
            images = flowOf(repository.fetchSavedImages())
            images.collect {
                setState(state.value.copy(images = it))
            }

        }
    }

    fun dispatchEvent(event: HomeUIEvent) {
        when (event) {
            is HomeUIEvent.Search -> onSearchTextChange(event.query)
            is HomeUIEvent.ToggleSearch -> onToggleSearch()
        }
    }


    private fun onSearchTextChange(text: String) {
        val debounceDuration = 500L // Debounce duration in milliseconds

        // Debounce the user input before processing the images
        val debouncedSearch = images
            .debounce(debounceDuration)
            .map { list ->
                val filteredImages = list.filter { it.contains(text) }
                HomeState(searching = true, searchQuery = text, images = filteredImages)
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
                setState(newState)
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