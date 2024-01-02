package com.reality.rememberaiprototype.home.presentation

sealed class HomeUIEvent {
    data class Search(val query: String): HomeUIEvent()
    object ToggleSearch: HomeUIEvent()
    object PrimaryButtonClick : HomeUIEvent()
    object Refresh : HomeUIEvent()

}