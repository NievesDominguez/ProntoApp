package com.example.persistencia.Pantallas

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
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
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Tag
import com.composables.icons.lucide.Ticket
import com.example.persistencia.Firestore.CarritoDao
import com.example.persistencia.Firestore.DescuentosDao
import com.example.persistencia.Firestore.ProductosDao
import com.example.persistencia.Firestore.UsuariosDao
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Modelos.Descuento
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch

// Componentes de carga y efectos visuales

@SuppressLint("ModifierFactoryExtensionFunction")
@Composable
fun shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )
    return Modifier.background(Color.LightGray.copy(alpha = alpha))
}

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
            val width = size.width
            val height = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    listOf(primaryColor, Color.Transparent),
                    0f,
                    edgeWidth
                ),
                size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, primaryColor),
                    height - edgeWidth,
                    height
                ),
                topLeft = Offset(0f, height - edgeWidth),
                size = Size(width, edgeWidth)
            )
        }

    Box(modifier = backgroundModifier.padding(24.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .height(320.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .then(shimmerEffect())
            )
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .height(30.dp)
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(8.dp))
                    .then(shimmerEffect())
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.4f)
                    .clip(RoundedCornerShape(8.dp))
                    .then(shimmerEffect())
            )
        }
    }
}

// Pantalla principal del producto

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PantallaProducto(idProducto: String, navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Inicialización de DAOs para la gestión de datos
    val daoCarrito = remember { CarritoDao() }
    val daoProductos = remember { ProductosDao() }
    val daoOfertas = remember { DescuentosDao() }
    val daoUsuario = remember { UsuariosDao() }

    // Estados para almacenar la información recuperada de la base de datos
    var producto by remember { mutableStateOf<Producto?>(null) }
    var ofertas by remember { mutableStateOf<List<Descuento>>(emptyList()) }
    var cuponesUsuario by remember { mutableStateOf<List<String>>(emptyList()) }
    var cuponesDisponibles by remember { mutableStateOf<List<Descuento>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    // Carga de datos sincronizada al iniciar la pantalla
    LaunchedEffect(idProducto) {
        cargando = true
        producto = daoProductos.getProducto(idProducto)
        ofertas = daoOfertas.getOfertas()
        cuponesDisponibles = daoOfertas.getCupones()
        cuponesUsuario = daoUsuario.getCupones()
        cargando = false
    }

    if (cargando || producto == null) {
        ProductoTemp()
        return
    }

    val p = producto!!

    // Resolución de promociones aplicables basadas en el código del producto
    val ofertaAplicable = ofertas.find { it.codigo == p.oferta }
    val cuponAplicable = cuponesDisponibles.find { it.codigo == p.oferta }
        ?.takeIf { it.codigo in cuponesUsuario }

    val backgroundModifier = Modifier
        .fillMaxSize()
        .background(colors.background)
        .drawBehind {
            val edgeWidth = with(density) { 25.dp.toPx() }
            val primaryColor = colors.primary.copy(alpha = 0.1f)
            val width = size.width
            val height = size.height
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(primaryColor, Color.Transparent),
                    0f,
                    edgeWidth
                ),
                size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, primaryColor),
                    height - edgeWidth,
                    height
                ),
                topLeft = Offset(0f, height - edgeWidth),
                size = Size(width, edgeWidth)
            )
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, null, tint = colors.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarDialogo = true }) {
                        Icon(Lucide.Pencil, null, modifier = Modifier.size(20.dp))
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = backgroundModifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Cabecera con imagen del producto
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.surfaceVariant.copy(
                                alpha = 0f
                            )
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        AsyncImage(
                            model = p.imagenUrl,
                            contentDescription = p.nombre,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }


                // Etiquetas de categorización
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 28.dp)
                ) {
                    ProductBadge(text = p.categoria, color = colors.primary)
                    ProductBadge(text = p.subcategoria, color = colors.secondary)
                }

                // Sección de ofertas y cupones
                if (ofertaAplicable != null || cuponAplicable != null) {

                    ofertaAplicable?.let {
                        PromoCard(
                            desc = it.nombre ?: it.codigo!!,
                            color = Color(0xFF4CAF50),
                            icono = Lucide.Tag,
                        )
                    }

                    cuponAplicable?.let { cupon ->
                        val activo = cupon.codigo in cuponesUsuario
                        PromoCard(
                            desc = cupon.nombre ?: cupon.codigo!!,
                            color = Color(0xFF2196F3),
                            icono = Lucide.Ticket,
                            switch = true,
                            estadoSwitch = activo,
                            onSwitch = { isChecked ->
                                scope.launch {
                                    if (isChecked) daoCarrito.activarCupon(cupon.codigo!!)
                                    else daoCarrito.desactivarCupon(cupon.codigo!!)
                                    cuponesUsuario = daoUsuario.getCupones()
                                }
                            }
                        )
                    }
                }

                //Spacer(Modifier.height(10.dp))

                // Bloque de información principal
                Column(modifier = Modifier.padding(horizontal = 28.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Nombre del producto
                            Text(
                                text = p.nombre,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.onBackground,
                                lineHeight = 34.sp
                            )
                            //Spacer(Modifier.height(4.dp))
                            // Cantidad por unidad
                            Text(
                                text = "${p.cantidad} ${p.unidad}",
                                fontSize = 12.sp,
                                color = colors.onBackground.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            // Precio
                            Text(
                                text = "%.2f€".format(p.precio),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.primary
                            )
                        }
                    }

                    Spacer(Modifier.height(1.dp))

                    // Descripción del producto
                    Text(
                        text = p.descripcion,
                        fontSize = 15.sp,
                        color = colors.onBackground.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )

                    Spacer(Modifier.height(120.dp))
                }
            }

            // Barra inferior fija con acciones principales
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                color = colors.background,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 16.dp, 24.dp, 25.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Añadir a la lista de la compra
                    Button(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Guardado en la lista de la compra",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.secondary)
                    ) {
                        Icon(
                            Icons.Default.PlaylistAdd,
                            null,
                            modifier = Modifier.size(22.dp),
                            tint = colors.onSurface
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Añadir a la lista",
                            fontWeight = FontWeight.Black,
                            color = colors.onSurface
                        )
                    }

                    // Añadir al carrito
                    Button(
                        onClick = {
                            scope.launch {
                                daoCarrito.addCarrito(p, 1)
                                Toast.makeText(context, "Añadido al carrito", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            null,
                            modifier = Modifier.size(22.dp),
                            tint = colors.onSurface
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Añadir al carrito",
                            fontWeight = FontWeight.Black,
                            color = colors.onSurface
                        )
                    }
                }
            }
        }
    }

    // Diálogo para la edición de detalles del producto
    if (mostrarDialogo) {
        EditarProductoDialog(
            producto = p,
            onDismiss = { mostrarDialogo = false },
            onSave = { nombre, precio, descripcion ->
                scope.launch {
                    daoProductos.actualizarProducto(p.id, nombre, precio, descripcion)
                    producto =
                        producto?.copy(nombre = nombre, precio = precio, descripcion = descripcion)
                    mostrarDialogo = false
                }
            }
        )
    }
}

// Controla el aspecto de las categorías de producto
@Composable
fun ProductBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

// Controla el aspecto del apartado de ofertas
@Composable
fun PromoCard(
    desc: String,
    color: Color,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    switch: Boolean = false,
    estadoSwitch: Boolean = false,
    onSwitch: (Boolean) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 28.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(desc, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            if (switch) {
                Switch(
                    checked = estadoSwitch,
                    onCheckedChange = onSwitch,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = color,
                        checkedTrackColor = color.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}


// Diálogo para editar producto
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
        title = { Text("Editar producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") }
                )
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val precioDouble = precio.toDoubleOrNull() ?: producto.precio
                onSave(nombre, precioDouble, descripcion)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}