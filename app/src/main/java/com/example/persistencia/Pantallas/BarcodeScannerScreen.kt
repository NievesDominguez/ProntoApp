package com.example.persistencia.Pantallas

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ShoppingCart
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
import com.example.persistencia.Firestore.CarritoDao
import com.example.persistencia.Firestore.ProductosDao
import com.example.persistencia.Herramientas.CameraScannerView
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Navegacion.AppScreens
import kotlinx.coroutines.launch

// Estados posibles del snackbar personalizado
sealed class BarcodeSnackbarState {
    data class ProductoNormal(val producto: Producto) : BarcodeSnackbarState()
    data class ProductoPeso(
        val productoOriginal: Producto,  // Producto base sin modificar
        val pesoKg: Double               // Peso en kg escaneado
    ) : BarcodeSnackbarState()
    data class Error(val mensaje: String) : BarcodeSnackbarState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val activity = context as Activity
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current

    val productosDao = remember { ProductosDao() }
    val carritoDao = remember { CarritoDao() }

    var ultimoCodigo by remember { mutableStateOf("") }
    var snackbarVisible by remember { mutableStateOf(false) }
    var snackbarState by remember { mutableStateOf<BarcodeSnackbarState?>(null) }

    // Permiso de cámara
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

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // SnackbarHost personalizado que muestra el diseño adecuado según el estado
    val customSnackbarHost = @Composable {
        snackbarState?.let { state ->
            when (state) {
                is BarcodeSnackbarState.ProductoNormal -> {
                    ProductoEncontradoSnackbar(
                        producto = state.producto,
                        onDismiss = {
                            snackbarState = null
                            snackbarVisible = false
                            ultimoCodigo = ""
                        },
                        onAddToCart = {
                            scope.launch {
                                carritoDao.addCarrito(state.producto, 1.toDouble())
                                // Pequeña confirmación con el Snackbar estándar (opcional)
                                snackbarHostState.showSnackbar("Producto añadido")
                            }
                            snackbarState = null
                            snackbarVisible = false
                            ultimoCodigo = ""
                        },
                        onViewProduct = {
                            navController.navigate("${AppScreens.PantallaProducto.route}/${state.producto.id}")
                            snackbarState = null
                            snackbarVisible = false
                            ultimoCodigo = ""
                        }
                    )
                }

                is BarcodeSnackbarState.ProductoPeso -> {
                    // Creamos el producto con la cantidad y unidad adecuadas para añadir al carrito
                    val productoConPeso = state.productoOriginal.copy(
                        cantidad = state.pesoKg,
                        unidad = "kg"
                    )
                    ProductoEncontradoSnackbar(
                        producto = state.productoOriginal, // Mostramos el nombre original
                        onDismiss = {
                            snackbarState = null
                            snackbarVisible = false
                            ultimoCodigo = ""
                        },
                        onAddToCart = {
                            scope.launch {
                                // Añadimos la cantidad como el peso
                                carritoDao.addCarrito(productoConPeso, state.pesoKg)
                                snackbarHostState.showSnackbar("${state.pesoKg} kg de ${state.productoOriginal.nombre} añadido")
                            }
                            snackbarState = null
                            snackbarVisible = false
                            ultimoCodigo = ""
                        },
                        onViewProduct = {
                            // Navegamos usando el id original del producto base
                            navController.navigate("${AppScreens.PantallaProducto.route}/${state.productoOriginal.id}")
                            snackbarState = null
                            snackbarVisible = false
                            ultimoCodigo = ""
                        }
                    )
                }

                is BarcodeSnackbarState.Error -> {
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        action = {
                            TextButton(onClick = {
                                snackbarState = null
                                snackbarVisible = false
                                ultimoCodigo = ""
                            }) {
                                Text("Cerrar")
                            }
                        },
                        dismissAction = {
                            IconButton(onClick = {
                                snackbarState = null
                                snackbarVisible = false
                                ultimoCodigo = ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar")
                            }
                        }
                    ) {
                        Text(state.mensaje)
                    }
                }

            }
        }
    }

    Scaffold(
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
                            imageVector = Icons.Default.ArrowBack,
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
        containerColor = Color.Transparent,
        snackbarHost = { customSnackbarHost() }
    ) { padding ->

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

                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor, Color.Transparent),
                            startY = 0f,
                            endY = edgeWidth
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(width, edgeWidth)
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, primaryColor),
                            startY = height - edgeWidth,
                            endY = height
                        ),
                        topLeft = Offset(0f, height - edgeWidth),
                        size = Size(width, edgeWidth)
                    )
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(secondaryColor, Color.Transparent),
                            startX = 0f,
                            endX = edgeWidth
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(edgeWidth, height)
                    )
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
                        // Ignorar si es el mismo código y el snackbar sigue visible
                        if (snackbarVisible && codigo == ultimoCodigo) {
                            return@CameraScannerView
                        }

                        // Cerrar snackbar anterior si existe (código diferente)
                        if (snackbarVisible) {
                            snackbarState = null
                            snackbarVisible = false
                        }

                        ultimoCodigo = codigo
                        snackbarVisible = true

                        scope.launch {
                            try {
                                // 1. Buscar producto normal
                                val productoNormal = productosDao.getProducto(codigo)
                                if (productoNormal != null) {
                                    snackbarState = BarcodeSnackbarState.ProductoNormal(productoNormal)
                                    return@launch
                                }

                                // 2. Producto al peso (EAN-13 que empieza por 23)
                                if (codigo.length == 13 && codigo.startsWith("23")) {
                                    val codigoInterno = codigo.substring(2, 7)
                                    val pesoGramos = codigo.substring(7, 12).toIntOrNull() ?: 0
                                    val pesoKg = pesoGramos / 1000.0

                                    val productoBase = productosDao.getProducto(codigoInterno)
                                    if (productoBase != null) {
                                        snackbarState = BarcodeSnackbarState.ProductoPeso(productoBase, pesoKg)
                                        return@launch
                                    }
                                }

                                // 3. Código no reconocido
                                snackbarState = BarcodeSnackbarState.Error("Código no reconocido: $codigo")
                            } catch (e: Exception) {
                                snackbarState = BarcodeSnackbarState.Error("Error: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Snackbar personalizado para productos encontrados (normales o al peso).
 * Muestra el nombre del producto y dos acciones: añadir al carrito y ver producto.
 */
@Composable
fun ProductoEncontradoSnackbar(
    producto: Producto,
    onDismiss: () -> Unit,
    onAddToCart: () -> Unit,
    onViewProduct: () -> Unit
) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        action = {
            Row {
                IconButton(onClick = onAddToCart) {
                    Icon(
                        Icons.Outlined.ShoppingCart,
                        contentDescription = "Añadir al carrito",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onViewProduct) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Ver producto",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        dismissAction = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar")
            }
        }
    ) {
        Text(
            text = producto.nombre.take(40),
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}