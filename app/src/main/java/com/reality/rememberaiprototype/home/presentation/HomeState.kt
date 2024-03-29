package com.reality.rememberaiprototype.home.presentation

import com.reality.rememberaiprototype.home.data.Image

data class HomeState(
    val images: List<Image> = listOf(),
    val searching: Boolean = true,
    val searchQuery: String = "",
    val recording: Boolean = false,
    val parsing: Boolean = false,
    val showPermissionsDenied: Boolean = false,
    val hasEmptyMemoriesDirectory: Boolean = false
)
