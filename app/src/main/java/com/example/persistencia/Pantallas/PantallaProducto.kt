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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    val transition = rememberInfiniteTransition() // Permite crear animaciones infinitas
    // Anima la opacidad del color
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
    // Degradado magenta a morado
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFD13CF2), Color(0xFF6C3AEC))
    )

    // Da el color de fondo y permite que los elementos de dentro tengan un margen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // Imagen
            Box(
                modifier = Modifier
                    .height(280.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .then(shimmerEffect())
            )

            Spacer(Modifier.height(20.dp))

            // Nombre
            Box(
                modifier = Modifier
                    .height(26.dp)
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(8.dp))
                    .then(shimmerEffect())
            )

            Spacer(Modifier.height(12.dp))

            // Precio
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
@RequiresApi(Build.VERSION_CODES.TIRAMISU) // Sólo Android 13 o superior (API 33)
@OptIn(
    ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun PantallaProducto(idProducto: String, navController: NavController) {

    val daoCarrito = CarritoDao() // Dao del carrito
    val dao = ProductosDao() // Dao del producto
    var producto by remember { mutableStateOf<Producto?>(null) } // Producto actual

    // Cargar el producto desde Firestore
    LaunchedEffect(idProducto) {
        producto = dao.getProducto(idProducto)
    }

    // Mientras carga se muestra ProductoTemp como placeholder
    if (producto == null) {
        ProductoTemp()
        return
    }

    val p = producto!! // Hace que no pueda ser null

    // Degradado magenta a morado
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFD13CF2), Color(0xFF6C3AEC))
    )

    val scope = rememberCoroutineScope() // Para ejecutar corrutinas
    val context = LocalContext.current // Para acceder al sistema

    // Al cargar la ventana pide permiso para enviar notificaciones si no se pidió antes
    val postNotificationPermission =
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    val notificationHandler = NotificationHandler(context)
    LaunchedEffect(key1 = true) {
        if (!postNotificationPermission.status.isGranted) {
            postNotificationPermission.launchPermissionRequest()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() } // Estado del snackbar


    // Muestra el diálogo de edición
    var mostrarDialogo by remember { mutableStateOf(false) }

    // TOPBAR
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                colors = topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                ),
                // Vuelve a la pantalla anterior
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch { navController.popBackStack() }
                    }) {
                        Icon(
                            imageVector = Lucide.ArrowLeft,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Botón de editar
                    IconButton(onClick = { mostrarDialogo = true }) {
                        Icon(
                            imageVector = Lucide.Pencil,
                            contentDescription = "Editar producto",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->


        // Da el color de fondo y permite que los elementos de dentro tengan un margen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues), // Esto evita que el contenido tape la TopBar
            contentAlignment = Alignment.Center,
        ) {

            // DIALOG DE EDICIÓN DEL PRODUCTO
            // Permite editar el producto
            if (mostrarDialogo) {

                EditarProductoDialog(
                    producto = p, // Producto actual a editar
                    onDismiss = { mostrarDialogo = false }, // Cierra el diálogo al pulsar cancelar

                    // Comportamiento al pulsar guardar
                    onSave = { nombre, precio, descripcion ->
                        scope.launch {
                            val productoAnterior =
                                p.copy() // Se guarda el producto actual para poder revertir los cambios

                            // Actualizar en Firestore
                            dao.actualizarProducto(
                                id = p.id,
                                nombre = nombre,
                                precio = precio,
                                descripcion = descripcion
                            )

                            // Actualizar en pantalla
                            producto = producto!!.copy(
                                nombre = nombre,
                                precio = precio,
                                descripcion = descripcion
                            )

                            mostrarDialogo = false // Cierra el diálogo

                            // Mostrar Snackbar con opción de deshacer
                            val resultado = snackbarHostState.showSnackbar(
                                message = "Producto actualizado",
                                actionLabel = "Deshacer",
                                duration = SnackbarDuration.Indefinite,
                                withDismissAction = true
                            )

                            // Al pulsar deshacer se revierten los cambios
                            if (resultado == SnackbarResult.ActionPerformed) {
                                dao.actualizarProducto(
                                    id = productoAnterior.id,
                                    nombre = productoAnterior.nombre,
                                    precio = productoAnterior.precio,
                                    descripcion = productoAnterior.descripcion
                                )

                                producto = productoAnterior
                            }
                            // Si se ha pulsado fuera del snackbar o cancelar, se cierra
                            else {
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
                    .verticalScroll(rememberScrollState()) // Permite hacer scroll
            ) {

                // INFORMACIÓN DEL PRODUCTO

                // Tarjeta que contiene la imagen del producto
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.25f)
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

                // Nombre del producto
                Text(
                    text = p.nombre,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(10.dp))

                // Precio del producto
                Text(
                    // El %.2f redondea a 2 decimales
                    text = "%.2f €".format(p.precio),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFFE8FF),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(20.dp))

                // Descripción del producto
                Text(
                    text = p.descripcion,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(40.dp))

                // BOTONES
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Añadir al carrito
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x944E89FF),
                            contentColor = Color.White
                        ),
                        onClick = {
                            scope.launch {
                                Toast.makeText(
                                    context,
                                    "Producto añadido al carrito",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Manda una notificación de aviso que lleva al carrito
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

                    // Añadir a la lista de la compra
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x944E89FF),
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


// Diálogo de edición de producto
@Composable
fun EditarProductoDialog(
    producto: Producto,
    onDismiss: () -> Unit,
    onSave: (String, Double, String) -> Unit
) {
    // Datos del producto
    var nombre by remember { mutableStateOf(producto.nombre) }
    var precio by remember { mutableStateOf(producto.precio.toString()) }
    var descripcion by remember { mutableStateOf(producto.descripcion) }

    // Diálogo
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") }
                )
                // Precio
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    maxLines = 4
                )
            }
        },

        // Al pulsar guardar se ejecuta onSave
        confirmButton = {
            TextButton(onClick = {
                val precioDouble = precio.toDoubleOrNull() ?: producto.precio
                onSave(nombre, precioDouble, descripcion)
            }) {
                Text("Guardar")
            }
        },
        // Al pulsar cancelar se ejecuta onDismiss
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

