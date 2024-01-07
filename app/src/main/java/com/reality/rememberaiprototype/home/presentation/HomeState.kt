package com.reality.rememberaiprototype.home.presentation

import com.reality.rememberaiprototype.home.data.Image

data class HomeState(
    val images: List<Image> = listOf(),
    val searching: Boolean = true,
    val searchQuery: String = "",
    val recording: Boolean = false,
)
