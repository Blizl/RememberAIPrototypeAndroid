package com.reality.rememberaiprototype.home.presentation

sealed class HomeUIAction {
    object ShowParseDirectory : HomeUIAction()
    object HideParseDirectory : HomeUIAction()
}