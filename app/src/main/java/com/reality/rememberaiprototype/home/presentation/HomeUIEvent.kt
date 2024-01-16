package com.reality.rememberaiprototype.home.presentation

import android.content.Intent

sealed class HomeUIEvent {
    data class Search(val query: String) : HomeUIEvent()
    object ToggleSearch : HomeUIEvent()
    object PrimaryButtonClick : HomeUIEvent()
    object Refresh : HomeUIEvent()
    object ParseDirectoryClosed : HomeUIEvent()
    object ParseMemoriesFromDirectory : HomeUIEvent()
    object PermissionsDenied : HomeUIEvent()
    data class ScreenShotCaptureClicked(
        val data: Intent,
        val resultCode: Int
    ) : HomeUIEvent()

    object ScreenShotCaptureCancelled : HomeUIEvent()
}