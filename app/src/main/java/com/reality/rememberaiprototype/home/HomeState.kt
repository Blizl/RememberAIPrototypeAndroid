package com.reality.rememberaiprototype.home

data class HomeState(
    val images: List<String> = listOf(),
    val searching: Boolean = false,
    val searchQuery: String = ""
)
