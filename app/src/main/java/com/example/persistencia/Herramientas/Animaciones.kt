package com.example.persistencia.Herramientas

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.NotebookPen
import kotlin.math.PI
import kotlin.math.sin

// Objetivos de la animación añadir al carrito/lista
enum class FlyTarget { CARRITO, LISTA }

class FlyToTargetState {
    var isAnimating by mutableStateOf(false)
        private set
    var startOffset by mutableStateOf(Offset.Zero)
        private set
    var target by mutableStateOf(FlyTarget.CARRITO)
        private set

    fun trigger(from: Offset, to: FlyTarget) {
        startOffset = from
        target = to
        isAnimating = true
    }

    fun onFinished() {
        isAnimating = false
    }
}

val LocalFlyToTargetState = staticCompositionLocalOf<FlyToTargetState?> { null }

@Composable
fun FlyToTargetOverlay(state: FlyToTargetState) {
    if (!state.isAnimating) return

    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    // Calcular posición destino basada en la posición del icono en la BottomBar
    // BottomBar tiene 5 items: Principal(0), Catalogo(1), Carrito(2), ListaCompra(3), Perfil(4)
    val targetIndex = when (state.target) {
        FlyTarget.CARRITO -> 2
        FlyTarget.LISTA -> 3
    }
    val targetX = screenWidthPx * (targetIndex + 0.5f) / 5f
    val targetY = screenHeightPx // Parte inferior de la pantalla (donde está el navbar)

    val icon: ImageVector = when (state.target) {
        FlyTarget.CARRITO -> Icons.Default.ShoppingCart
        FlyTarget.LISTA -> Lucide.NotebookPen
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(state.isAnimating) {
        if (state.isAnimating) {
            progress.snapTo(0f)
            progress.animateTo(
                1f,
                animationSpec = tween(550, easing = FastOutSlowInEasing)
            )
            state.onFinished()
        }
    }

    val p = progress.value
    val currentX = lerp(state.startOffset.x, targetX, p)
    val currentY = lerp(state.startOffset.y, targetY, p)
    // Arco parabólico hacia arriba para que no sea lineal
    val arcOffset = -250f * sin(p * PI.toFloat())
    val currentScale = lerp(1f, 0.3f, p)
    val currentAlpha = if (p < 0.8f) 1f else lerp(1f, 0f, (p - 0.8f) / 0.2f)

    Box(Modifier.fillMaxSize()) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer {
                    translationX = currentX - 48f // Centrar el icono
                    translationY = currentY + arcOffset - 48f
                    scaleX = currentScale
                    scaleY = currentScale
                    alpha = currentAlpha
                }
        )
    }
}

private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}