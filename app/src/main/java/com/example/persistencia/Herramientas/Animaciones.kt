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

// Define los dos destinos posibles de la animación
enum class FlyTarget { CARRITO, LISTA }

// Gestiona el estado de la animación
class FlyToTargetState {
    var isAnimating by mutableStateOf(false)  // Indica si la animación está activa
        private set
    var startOffset by mutableStateOf(Offset.Zero)  // Posición inicial del icono
        private set
    var target by mutableStateOf(FlyTarget.CARRITO)  // Destino de la animación
        private set

    // Inicia la animación desde una posición hacia un destino
    fun trigger(from: Offset, to: FlyTarget) {
        startOffset = from
        target = to
        isAnimating = true
    }

    // Marca la animación como finalizada
    fun onFinished() {
        isAnimating = false
    }
}

// CompositionLocal para compartir el estado entre composables
val LocalFlyToTargetState = staticCompositionLocalOf<FlyToTargetState?> { null }

@Composable
fun FlyToTargetOverlay(state: FlyToTargetState) {
    // Si no hay animación activa, no renderiza nada
    if (!state.isAnimating) return

    val density = LocalDensity.current
    val config = LocalConfiguration.current
    // Convierte las dimensiones de pantalla a píxeles
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    // Calcula la posición X del destino basándose en el índice del icono en la BottomBar
    // BottomBar tiene 5 botones: Principal(0), Catalogo(1), Carrito(2), ListaCompra(3), Perfil(4)
    val targetIndex = when (state.target) {
        FlyTarget.CARRITO -> 2
        FlyTarget.LISTA -> 3
    }
    // Calcula la posición X centrada en el segmento correspondiente del icono
    val targetX = screenWidthPx * (targetIndex + 0.5f) / 5f
    val targetY = screenHeightPx // Parte inferior de la pantalla (donde está la navbar)

    // Selecciona el icono según el destino
    val icon: ImageVector = when (state.target) {
        FlyTarget.CARRITO -> Icons.Default.ShoppingCart
        FlyTarget.LISTA -> Lucide.NotebookPen
    }

    // Animatable para controlar el progreso de la animación (0f a 1f)
    val progress = remember { Animatable(0f) }

    // Efecto que se ejecuta cuando cambia isAnimating
    LaunchedEffect(state.isAnimating) {
        if (state.isAnimating) {
            progress.snapTo(0f)  // Reinicia el progreso
            progress.animateTo(
                1f,
                animationSpec = tween(550, easing = FastOutSlowInEasing)  // 550ms con curva suave
            )
            state.onFinished()  // Marca la animación como terminada
        }
    }

    val p = progress.value
    // Interpolación lineal de la posición X e Y
    val currentX = lerp(state.startOffset.x, targetX, p)
    val currentY = lerp(state.startOffset.y, targetY, p)
    // Arco parabólico hacia arriba para que el movimiento no sea lineal
    val arcOffset = -250f * sin(p * PI.toFloat())
    // Se reduce el tamaño del icono durante la animación
    val currentScale = lerp(1f, 0.3f, p)
    // El icono se desvanece en el último 20% de la animación
    val currentAlpha = if (p < 0.8f) 1f else lerp(1f, 0f, (p - 0.8f) / 0.2f)

    // Renderiza el icono animado sobre toda la pantalla
    Box(Modifier.fillMaxSize()) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer {
                    translationX = currentX - 48f // Centrar el icono (ajuste de offset)
                    translationY = currentY + arcOffset - 48f
                    scaleX = currentScale
                    scaleY = currentScale
                    alpha = currentAlpha
                }
        )
    }
}

// Función auxiliar de interpolación lineal
private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}