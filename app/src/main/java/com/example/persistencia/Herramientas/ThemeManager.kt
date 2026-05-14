package com.example.persistencia.Herramientas

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.compositionLocalOf

class ThemeManager(context: Context) {
    // Accede a SharedPreferences para persistir la preferencia de tema
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    // Estado que contiene la preferencia de tema actual
    val themePreference = mutableStateOf(loadTheme())

    // Actualiza la preferencia de tema si es diferente a la actual
    fun setThemePreference(newValue: ThemePreference) {
        if (themePreference.value != newValue) {
            themePreference.value = newValue  // Actualiza el estado
            saveTheme(newValue)  // Persiste el cambio
        }
    }

    // Carga la preferencia guardada en SharedPreferences
    private fun loadTheme(): ThemePreference {
        val ordinal = prefs.getInt("theme_preference", ThemePreference.System.ordinal)
        return ThemePreference.entries.getOrElse(ordinal) { ThemePreference.System }
    }

    // Guarda la preferencia en SharedPreferences
    private fun saveTheme(pref: ThemePreference) {
        prefs.edit().putInt("theme_preference", pref.ordinal).apply()
    }
}

// Enum que define las tres opciones de tema disponibles
enum class ThemePreference {
    System,  // Seguir la configuración del sistema
    Light,   // Forzar tema claro
    Dark     // Forzar tema oscuro
}

// CompositionLocal para proporcionar el ThemeManager a toda la jerarquía de Compose
val LocalThemeManager = compositionLocalOf<ThemeManager> { error("No ThemeManager provided") }