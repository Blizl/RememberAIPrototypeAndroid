package com.reality.rememberaiprototype.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reality.rememberaiprototype.home.domain.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    init {
        fetchSavedImages()
    }

    private fun fetchSavedImages() {
        viewModelScope.launch{
            val images = repository.fetchSavedImages()
            setState(state.value.copy(images = images))
        }
    }

    fun dispatchEvent(event: HomeUIEvent) {
        when (event) {
            is HomeUIEvent.Search -> onSearchTextChange(event.query)
            is HomeUIEvent.ToggleSearch -> onToggleSearch()
        }
    }


    private fun onSearchTextChange(text: String) {

    }

    private fun onToggleSearch() {


    }

    private fun setState(newState: HomeState) {
        viewModelScope.launch {
            _state.emit(newState)
        }
    }
}