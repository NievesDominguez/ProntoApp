package com.example.persistencia.Herramientas

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.compositionLocalOf

class ThemeManager(context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    // Exponemos el MutableState para que Compose lo observe, pero para leer hay que usar .value
    val themePreference = mutableStateOf(loadTheme())

    fun setThemePreference(newValue: ThemePreference) {
        if (themePreference.value != newValue) {
            themePreference.value = newValue
            saveTheme(newValue)
        }
    }

    private fun loadTheme(): ThemePreference {
        val ordinal = prefs.getInt("theme_preference", ThemePreference.System.ordinal)
        return ThemePreference.entries.getOrElse(ordinal) { ThemePreference.System }
    }

    private fun saveTheme(pref: ThemePreference) {
        prefs.edit().putInt("theme_preference", pref.ordinal).apply()
    }
}

enum class ThemePreference {
    System, Light, Dark
}

val LocalThemeManager = compositionLocalOf<ThemeManager> { error("No ThemeManager provided") }