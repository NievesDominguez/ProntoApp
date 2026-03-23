package com.example.persistencia.Pantallas

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.example.persistencia.Firestore.CarritoDao
import com.example.persistencia.Firestore.ProductosDao
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Herramientas.NotificationHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

// Efecto de brillo animado mientras carga el producto
@SuppressLint("ModifierFactoryExtensionFunction")
@Composable
fun shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        )
    )
    return Modifier.background(Color.LightGray.copy(alpha = alpha))
}

// Esto se muestra en lo que se carga el producto de la base de datos y se le aplica el efecto de brillo
@Composable
fun ProductoTemp() {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val backgroundModifier = Modifier
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
                    startY = 0f, endY = edgeWidth
                ),
                topLeft = Offset(0f, 0f), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, primaryColor),
                    startY = height - edgeWidth, endY = height
                ),
                topLeft = Offset(0f, height - edgeWidth), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(secondaryColor, Color.Transparent),
                    startX = 0f, endX = edgeWidth
                ),
                topLeft = Offset(0f, 0f), size = Size(edgeWidth, height)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, secondaryColor),
                    startX = width - edgeWidth, endX = width
                ),
                topLeft = Offset(width - edgeWidth, 0f), size = Size(edgeWidth, height)
            )
        }

    Box(
        modifier = backgroundModifier.padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .height(280.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .then(shimmerEffect())
            )
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .height(26.dp)
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(8.dp))
                    .then(shimmerEffect())
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .height(22.dp)
                    .fillMaxWidth(0.3f)
                    .clip(RoundedCornerShape(8.dp))
                    .then(shimmerEffect())
            )
        }
    }
}

// Pantalla del producto
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(
    ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun PantallaProducto(idProducto: String, navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current

    val backgroundModifier = Modifier
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
                    startY = 0f, endY = edgeWidth
                ),
                topLeft = Offset(0f, 0f), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, primaryColor),
                    startY = height - edgeWidth, endY = height
                ),
                topLeft = Offset(0f, height - edgeWidth), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(secondaryColor, Color.Transparent),
                    startX = 0f, endX = edgeWidth
                ),
                topLeft = Offset(0f, 0f), size = Size(edgeWidth, height)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, secondaryColor),
                    startX = width - edgeWidth, endX = width
                ),
                topLeft = Offset(width - edgeWidth, 0f), size = Size(edgeWidth, height)
            )
        }

    val daoCarrito = CarritoDao()
    val dao = ProductosDao()
    var producto by remember { mutableStateOf<Producto?>(null) }

    LaunchedEffect(idProducto) {
        producto = dao.getProducto(idProducto)
    }

    if (producto == null) {
        ProductoTemp()
        return
    }

    val p = producto!!

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val postNotificationPermission =
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    val notificationHandler = NotificationHandler(context)
    LaunchedEffect(key1 = true) {
        if (!postNotificationPermission.status.isGranted) {
            postNotificationPermission.launchPermissionRequest()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                colors = topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colors.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch { navController.popBackStack() }
                    }) {
                        Icon(
                            imageVector = Lucide.ArrowLeft,
                            contentDescription = "Atrás",
                            tint = colors.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarDialogo = true }) {
                        Icon(
                            imageVector = Lucide.Pencil,
                            contentDescription = "Editar producto",
                            tint = colors.onBackground
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->

        Box(
            modifier = backgroundModifier
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            if (mostrarDialogo) {
                EditarProductoDialog(
                    producto = p,
                    onDismiss = { mostrarDialogo = false },
                    onSave = { nombre, precio, descripcion ->
                        scope.launch {
                            val productoAnterior = p.copy()

                            dao.actualizarProducto(
                                id = p.id,
                                nombre = nombre,
                                precio = precio,
                                descripcion = descripcion
                            )

                            producto = producto!!.copy(
                                nombre = nombre,
                                precio = precio,
                                descripcion = descripcion
                            )

                            mostrarDialogo = false

                            val resultado = snackbarHostState.showSnackbar(
                                message = "Producto actualizado",
                                actionLabel = "Deshacer",
                                duration = SnackbarDuration.Indefinite,
                                withDismissAction = true
                            )

                            if (resultado == SnackbarResult.ActionPerformed) {
                                dao.actualizarProducto(
                                    id = productoAnterior.id,
                                    nombre = productoAnterior.nombre,
                                    precio = productoAnterior.precio,
                                    descripcion = productoAnterior.descripcion
                                )
                                producto = productoAnterior
                            } else {
                                if (resultado == SnackbarResult.Dismissed) {
                                    mostrarDialogo = false
                                }
                            }
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 0.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceVariant.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    AsyncImage(
                        model = p.imagenUrl,
                        contentDescription = p.nombre,
                        modifier = Modifier
                            .height(250.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = p.nombre,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "%.2f €".format(p.precio),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = p.descripcion,
                    fontSize = 16.sp,
                    color = colors.onBackground.copy(alpha = 0.9f),
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(40.dp))

                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary.copy(alpha = 0.6f),
                            contentColor = Color.White
                        ),
                        onClick = {
                            scope.launch {
                                Toast.makeText(
                                    context,
                                    "Producto añadido al carrito",
                                    Toast.LENGTH_SHORT
                                ).show()

                                notificationHandler.showSimpleNotification(
                                    "Producto añadido al carrito",
                                    "Has añadido ${p.nombre} al carrito.",
                                    "Carrito",
                                )

                                daoCarrito.addCarrito(p, 1)
                            }
                        }
                    ) {
                        Text(
                            text = "Añadir al carrito",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary.copy(alpha = 0.6f),
                            contentColor = Color.White
                        ),
                        onClick = {
                            scope.launch {
                                Toast.makeText(
                                    context,
                                    "Producto añadido a la lista de la compra",
                                    Toast.LENGTH_SHORT
                                ).show()

                                notificationHandler.showSimpleNotification(
                                    "Función en desarrollo",
                                    "Lo sentimos, esta función aún no está disponible.",
                                    "PantallaPrincipal",
                                )
                            }
                        }
                    ) {
                        Text(
                            text = "Añadir a la lista de la compra",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun EditarProductoDialog(
    producto: Producto,
    onDismiss: () -> Unit,
    onSave: (String, Double, String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var nombre by remember { mutableStateOf(producto.nombre) }
    var precio by remember { mutableStateOf(producto.precio.toString()) }
    var descripcion by remember { mutableStateOf(producto.descripcion) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar producto", color = colors.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline
                    )
                )
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline
                    )
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val precioDouble = precio.toDoubleOrNull() ?: producto.precio
                    onSave(nombre, precioDouble, descripcion)
                }
            ) {
                Text("Guardar", color = colors.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = colors.onSurface)
            }
        },
        containerColor = colors.surface
    )
}