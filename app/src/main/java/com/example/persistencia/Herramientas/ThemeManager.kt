package com.example.persistencia.Herramientas

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class ThemeManager {
    var themePreference by mutableStateOf(ThemePreference.System)
}

enum class ThemePreference {
    System,
    Light,
    Dark
}

val LocalThemeManager = compositionLocalOf { ThemeManager() }