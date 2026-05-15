package com.example.persistencia.Pantallas

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.persistencia.Firestore.CarritoDao
import com.example.persistencia.Firestore.ProductosDao
import com.example.persistencia.Herramientas.CameraScannerView
import com.example.persistencia.Herramientas.CarritoRepository
import com.example.persistencia.Herramientas.ProductosRepository
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Navegacion.AppScreens
import kotlinx.coroutines.launch

// Estado del snackbar cuando se detecta un producto en el escaneo
sealed class BarcodeSnackbarState {
    data class ProductoNormal(val producto: Producto) : BarcodeSnackbarState()
    data class ProductoPeso(
        val producto: Producto,
        val pesoKg: Double
    ) : BarcodeSnackbarState()
    data class Error(val mensaje: String) : BarcodeSnackbarState()
}

// Estado del snackbar cuando se añade automáticamente al carrito
data class AddToCartSnackbarState(
    val texto: String,
    val producto: Producto,
    val cantidad: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(navController: NavHostController) {

    val context = LocalContext.current
    val activity = context as Activity

    val scope = rememberCoroutineScope()

    // Estado del snackbar de producto detectado
    var snackbarState by remember { mutableStateOf<BarcodeSnackbarState?>(null) }

    // Estado del snackbar de añadido al carrito
    var addSnackbar by remember { mutableStateOf<AddToCartSnackbarState?>(null) }

    // Activa o desactiva el modo de auto añadido al carrito
    var autoAddToCart by remember { mutableStateOf(false) }

    // Evita duplicados rápidos en modo automático
    var lastAddTime by remember { mutableStateOf(0L) }

    var lastScanTime by remember { mutableStateOf(0L) }

    // Solicitud de permiso de cámara al iniciar
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

    val snackbarHost = @Composable {

        // Snackbar de producto detectado manualmente
        snackbarState?.let { state ->
            when (state) {

                is BarcodeSnackbarState.ProductoNormal -> {
                    ProductoEncontradoSnackbar(
                        producto = state.producto,
                        onDismiss = { snackbarState = null },
                        onAddToCart = {
                            scope.launch {
                                CarritoRepository.addCarrito(state.producto, 1.0)
                                addSnackbar = AddToCartSnackbarState(
                                    "${state.producto.nombre} añadido al carrito",
                                    state.producto,
                                    1.0
                                )
                            }
                            snackbarState = null
                        },
                        onViewProduct = {
                            navController.navigate(
                                "${AppScreens.PantallaProducto.route}/${state.producto.id}"
                            )
                            snackbarState = null
                        }
                    )
                }

                is BarcodeSnackbarState.ProductoPeso -> {
                    ProductoEncontradoSnackbar(
                        producto = state.producto,
                        pesoKg = state.pesoKg,  // Añadir este parámetro
                        onDismiss = { snackbarState = null },
                        onAddToCart = {
                            scope.launch {
                                CarritoRepository.addCarrito(state.producto, state.pesoKg)
                                addSnackbar = AddToCartSnackbarState(
                                    "${state.producto.nombre} (${"%.3f".format(state.pesoKg)} kg) añadido al carrito",
                                    state.producto,
                                    state.pesoKg
                                )
                            }
                            snackbarState = null
                        },
                        onViewProduct = {
                            navController.navigate(
                                "${AppScreens.PantallaProducto.route}/${state.producto.id}"
                            )
                            snackbarState = null
                        }
                    )
                }

                is BarcodeSnackbarState.Error -> {
                    Snackbar(Modifier.padding(16.dp)) {
                        Text(state.mensaje)
                    }
                }
            }
        }

        // Snackbar de añadido automático o manual con opción de deshacer
        addSnackbar?.let { state ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = {
                    if (it == SwipeToDismissBoxValue.StartToEnd ||
                        it == SwipeToDismissBoxValue.EndToStart
                    ) {
                        addSnackbar = null
                        true
                    } else {
                        false
                    }
                }
            )

            // Auto-dismiss después de 4 segundos
            LaunchedEffect(addSnackbar) {
                kotlinx.coroutines.delay(4000)
                addSnackbar = null
            }

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {},
                content = {
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        action = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Botón X para cerrar
                                IconButton(
                                    onClick = { addSnackbar = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                // Botón deshacer
                                TextButton(onClick = {
                                    scope.launch {
                                        // Si el producto es al peso se usa la cantidad real añadida
                                        val undoAmount =
                                            if (state.producto.al_peso == true) state.cantidad else 1.0

                                        CarritoRepository.addCarrito(state.producto, -undoAmount)
                                    }
                                    addSnackbar = null
                                }) {
                                    Text("Deshacer")
                                }
                            }
                        }
                    ) {
                        Text(state.texto)
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Escanea un código", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Activa o desactiva el modo automático de añadir al carrito
                    IconButton(onClick = { autoAddToCart = !autoAddToCart }) {
                        Icon(
                            imageVector = if (autoAddToCart)
                                Icons.Default.ToggleOn
                            else
                                Icons.Default.ToggleOff,
                            contentDescription = "Auto añadir al carrito",
                            tint = if (autoAddToCart)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        snackbarHost = { snackbarHost() },
        containerColor = Color.Transparent
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            CameraScannerView(
                modifier = Modifier.fillMaxSize()
            ) { codigo ->

                val now = System.currentTimeMillis()

                // Evita múltiples disparos del scanner por el mismo frame
                if (now - lastScanTime < 1200) return@CameraScannerView

                lastScanTime = now

                scope.launch {

                    val productoNormal = ProductosRepository.getProducto(codigo)

                    // Producto estándar encontrado
                    if (productoNormal != null) {

                        if (autoAddToCart) {

                            // Evita añadir varias veces seguidas el mismo producto
                            if (now - lastAddTime > 1500) {
                                CarritoRepository.addCarrito(productoNormal, 1.0)
                                lastAddTime = now

                                addSnackbar = AddToCartSnackbarState(
                                    "${productoNormal.nombre} añadido al carrito",
                                    productoNormal,
                                    1.0
                                )
                            }

                            return@launch
                        }

                        snackbarState = BarcodeSnackbarState.ProductoNormal(productoNormal)
                        return@launch
                    }

                    // Producto vendido por peso (EAN-13 especial)
                    if (codigo.length == 13 && codigo.startsWith("23")) {

                        val codigoInterno = codigo.substring(2, 7)
                        val pesoKg =
                            (codigo.substring(7, 12).toIntOrNull() ?: 0) / 1000.0

                        val productoBase = ProductosRepository.getProducto(codigoInterno)

                        if (productoBase != null) {

                            if (autoAddToCart) {

                                if (now - lastAddTime > 1500) {
                                    CarritoRepository.addCarrito(productoBase, pesoKg)
                                    lastAddTime = now

                                    addSnackbar = AddToCartSnackbarState(
                                        "${productoBase.nombre} añadido al carrito",
                                        productoBase,
                                        pesoKg
                                    )
                                }

                                return@launch
                            }

                            snackbarState = BarcodeSnackbarState.ProductoPeso(
                                productoBase,
                                pesoKg
                            )
                            return@launch
                        }
                    }
                }
            }
        }
    }
}

// Snackbar que muestra información del producto escaneado. Permite añadir al carrito o ver detalle del producto
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoEncontradoSnackbar(
    producto: Producto,
    pesoKg: Double? = null,  // Añadir este parámetro opcional
    onDismiss: () -> Unit,
    onAddToCart: () -> Unit,
    onViewProduct: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()

    // Detecta swipe para cerrar el snackbar
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd ||
            dismissState.currentValue == SwipeToDismissBoxValue.EndToStart
        ) {
            onDismiss()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {},
        content = {
            Snackbar(Modifier.padding(16.dp)) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewProduct() },
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Imagen del producto
                    AsyncImage(
                        model = producto.imagenUrl,
                        contentDescription = "Imagen producto",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        // Nombre del producto
                        Text(
                            text = producto.nombre,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        // Mostrar peso si es producto al peso
                        if (pesoKg != null) {
                            Text(
                                text = "${"%.3f".format(pesoKg)} kg",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Precio del producto (calculado si es al peso)
                    val precioMostrar = if (pesoKg != null) {
                        producto.precio * pesoKg
                    } else {
                        producto.precio
                    }
                    Text(
                        text = "${"%.2f".format(precioMostrar)} €",
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // Botón para añadir al carrito
                    IconButton(onClick = onAddToCart) {
                        Icon(
                            Icons.Outlined.ShoppingCart,
                            contentDescription = "Añadir al carrito"
                        )
                    }
                }
            }
        }
    )
}