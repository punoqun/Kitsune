package io.github.drumber.kitsune.ui.settings

import io.github.drumber.kitsune.preference.AppTheme
import io.github.drumber.kitsune.preference.MediaItemSize

data class AppearanceUiState(
    val isDynamicColorAvailable: Boolean = false,
    val useDynamicColorTheme: Boolean = false,
    val appTheme: AppTheme = AppTheme.DEFAULT,
    val darkMode: String = "-1",
    val oledBlackMode: Boolean = false,
    val mediaItemSize: MediaItemSize = MediaItemSize.MEDIUM
)
