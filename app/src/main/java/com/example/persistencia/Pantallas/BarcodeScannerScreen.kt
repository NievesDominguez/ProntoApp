package com.example.persistencia.Pantallas

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.persistencia.Herramientas.CameraScannerView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val activity = context as Activity
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current

    // Pedimos permiso de cámara al entrar en la pantalla
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        }
    }

    // Estado del Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        // Barra superior
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Escanea un código",
                        color = colors.onBackground,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Volver",
                            tint = colors.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colors.onBackground
                )
            )
        },
        containerColor = Color.Transparent, // El fondo lo manejará el Box con drawBehind
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->

        // Fondo con bordes degradados
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .drawBehind {
                    val edgeWidth = with(density) { 25.dp.toPx() }
                    val primaryColor = colors.primary.copy(alpha = 0.1f)
                    val secondaryColor = colors.secondary.copy(alpha = 0.05f)
                    val width = size.width
                    val height = size.height

                    // Borde superior
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor, Color.Transparent),
                            startY = 0f,
                            endY = edgeWidth
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(width, edgeWidth)
                    )
                    // Borde inferior
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, primaryColor),
                            startY = height - edgeWidth,
                            endY = height
                        ),
                        topLeft = Offset(0f, height - edgeWidth),
                        size = Size(width, edgeWidth)
                    )
                    // Borde izquierdo
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(secondaryColor, Color.Transparent),
                            startX = 0f,
                            endX = edgeWidth
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(edgeWidth, height)
                    )
                    // Borde derecho
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, secondaryColor),
                            startX = width - edgeWidth,
                            endX = width
                        ),
                        topLeft = Offset(width - edgeWidth, 0f),
                        size = Size(edgeWidth, height)
                    )
                }
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Vista de cámara con bordes redondeadosa
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceVariant.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    CameraScannerView(
                        modifier = Modifier.fillMaxSize()
                    ) { codigo ->
                        if (!codigo.equals(snackbarHostState.currentSnackbarData?.visuals?.message)) {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = codigo,
                                    actionLabel = "Añadir al carrito",
                                    duration = SnackbarDuration.Indefinite,
                                    withDismissAction = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}