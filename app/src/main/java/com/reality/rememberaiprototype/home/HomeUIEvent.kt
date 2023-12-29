package com.reality.rememberaiprototype.home

sealed class HomeUIEvent {
    data class Search(val query: String): HomeUIEvent()
    object ToggleSearch: HomeUIEvent()
}