package com.example.persistencia.Herramientas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.fondoDegradado(): Modifier {
    val colors = MaterialTheme.colorScheme // Colores del tema actual
    val density = LocalDensity.current // Densidad para conversion de dp a px
    val scope = rememberCoroutineScope() // Para acciones asíncronas
    val context = LocalContext.current // Para toasts
    return this
        .fillMaxSize()
        .background(colors.background)
        .drawBehind {
            val edgeWidth = with(density) { 25.dp.toPx() }
            val primaryColor = colors.primary.copy(alpha = 0.1f)
            val secondaryColor = colors.secondary.copy(alpha = 0.05f)
            val width = size.width
            val height = size.height
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(primaryColor, Color.Transparent), 0f, edgeWidth
                ), topLeft = Offset(0f, 0f), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, primaryColor), height - edgeWidth, height
                ), topLeft = Offset(0f, height - edgeWidth), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(secondaryColor, Color.Transparent), 0f, edgeWidth
                ), topLeft = Offset(0f, 0f), size = Size(edgeWidth, height)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, secondaryColor), width - edgeWidth, width
                ), topLeft = Offset(width - edgeWidth, 0f), size = Size(edgeWidth, height)
            )
        }
}