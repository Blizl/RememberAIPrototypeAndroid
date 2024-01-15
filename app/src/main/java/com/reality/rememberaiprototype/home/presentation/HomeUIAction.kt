package com.reality.rememberaiprototype.home.presentation

sealed class HomeUIAction {
    object ShowGenericError : HomeUIAction()
    data class ShowError(val message: String) : HomeUIAction()
    object ShowParseDirectory : HomeUIAction()
    object HideParseDirectory : HomeUIAction()
}