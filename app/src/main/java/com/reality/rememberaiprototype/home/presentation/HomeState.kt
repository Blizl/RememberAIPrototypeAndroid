package com.reality.rememberaiprototype.home.presentation

data class HomeState(
    val images: List<String> = listOf(),
    val searching: Boolean = true,
    val searchQuery: String = "",
    val recording: Boolean = false,
    val mlText: String = "hey this is a test"
)
