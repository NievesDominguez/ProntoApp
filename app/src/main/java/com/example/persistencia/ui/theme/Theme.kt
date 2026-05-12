package com.example.persistencia.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Colores de marca fijos
val BrandPurple = Color(0xFF7700FF)
val BrandPink = Color(0xFFFF008A)

// Versiones más claras para modo oscuro
val BrandPurpleLight = Color(0xFF9B44FF)
val BrandPinkLight = Color(0xFFFF2296)

// Esquema de colores claro
private val LightColorScheme = lightColorScheme(
    primary = BrandPurple,
    secondary = BrandPink,
    tertiary = Color(0xFFF5F5F5),
    background = Color.White,
    surface = Color.White,
    onPrimary = Color(0xFFDCCCE1),
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    outline = Color.Gray.copy(alpha = 0.5f),
    onError = Color(0xFFFF0000),
    error = Color(0xFFFF0000),
)

// Esquema de colores oscuro
private val DarkColorScheme = darkColorScheme(
    primary = BrandPurpleLight,
    secondary = BrandPinkLight,
    tertiary = Color(0xFF2C2C2C),
    background = Color(0xFF3A3A3A),
    surface = Color(0xFF464646),
    onPrimary = Color(0xFF524C54),
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    outline = Color.Gray.copy(alpha = 0.7f),
    onError = Color(0xFFFF3C3C),
    error = Color(0xFFFF3C3C)
)

@Composable
fun PersistenciaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}