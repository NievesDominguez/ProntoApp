package com.example.persistencia.Pantallas

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.composables.icons.lucide.Atom
import com.composables.icons.lucide.Bean
import com.composables.icons.lucide.Egg
import com.composables.icons.lucide.Fish
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Milk
import com.composables.icons.lucide.Nut
import com.composables.icons.lucide.Shell
import com.composables.icons.lucide.Sprout
import com.composables.icons.lucide.Tag
import com.composables.icons.lucide.Ticket
import com.composables.icons.lucide.Wheat
import com.example.persistencia.Herramientas.CarritoRepository
import com.example.persistencia.Herramientas.DescuentosRepository
import com.example.persistencia.Herramientas.ProductosRepository
import com.example.persistencia.Herramientas.UsuariosRepository
import com.example.persistencia.Herramientas.ListasRepository
import com.example.persistencia.Herramientas.fondoDegradado
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Modelos.Descuento
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.example.persistencia.Herramientas.FlyTarget
import com.example.persistencia.Herramientas.LocalFlyToTargetState

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

    val backgroundModifier = Modifier.fondoDegradado()

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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val flyState = LocalFlyToTargetState.current

    // Estados para almacenar la información recuperada de la base de datos
    var producto by remember { mutableStateOf<Producto?>(null) }
    var ofertas by remember { mutableStateOf<List<Descuento>>(emptyList()) }
    var cuponesUsuario by remember { mutableStateOf<List<String>>(emptyList()) }
    var cuponesDisponibles by remember { mutableStateOf<List<Descuento>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var cantidad by remember { mutableStateOf(1.0) }
    var pesoText by remember { mutableStateOf("1,000") }

    var listaButtonPos by remember { mutableStateOf(Offset.Zero) }
    var carritoButtonPos by remember { mutableStateOf(Offset.Zero) }

    // Carga de datos sincronizada al iniciar la pantalla
    LaunchedEffect(idProducto) {
        cargando = true
        producto = ProductosRepository.getProducto(idProducto)
        ofertas = DescuentosRepository.getOfertas()
        cuponesDisponibles = DescuentosRepository.getCupones()
        cuponesUsuario = UsuariosRepository.getCupones()
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

    val backgroundModifier = Modifier.fondoDegradado()

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
//                actions = {
//                    IconButton(onClick = { mostrarDialogo = true }) {
//                        Icon(Lucide.Pencil, null, modifier = Modifier.size(20.dp))
//                    }
//                }
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
                            containerColor = colors.surfaceVariant.copy(alpha = 0f)
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

                    // Indicadores de alérgenos
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        AlergenosOverlay(
                            contiene = p.alergenos_contiene ?: emptyList(),
                            trazas = p.alergenos_trazas ?: emptyList()
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
                        PromoCard(
                            desc = cupon.nombre ?: cupon.codigo!!,
                            color = Color(0xFF2196F3),
                            icono = Lucide.Ticket,
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

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

                    Spacer(Modifier.height(140.dp))
                }
            }

            // Barra inferior fija con acciones principales
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                color = colors.background,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    // Selector de cantidad
                    val esAlPeso = p.al_peso == true

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cantidad:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = colors.onBackground,
                            modifier = Modifier.padding(end = 12.dp)
                        )

                        if (esAlPeso) {
                            // Producto al peso: campo de texto decimal + unidad
                            OutlinedTextField(
                                value = pesoText,
                                onValueChange = { newValue ->
                                    val filtered = newValue.filter { it.isDigit() || it == '.' || it == ',' }
                                    pesoText = filtered
                                    val normalized = filtered.replace(',', '.')
                                    val nuevaCantidad = normalized.toDoubleOrNull()
                                    if (nuevaCantidad != null && nuevaCantidad > 0.0) {
                                        cantidad = nuevaCantidad
                                    }
                                },
                                modifier = Modifier.width(100.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = colors.onBackground,
                                    fontWeight = FontWeight.Bold
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.outline
                                )
                            )
                            Text(
                                text = p.unidad ?: "kg",
                                modifier = Modifier.padding(start = 6.dp),
                                fontSize = 16.sp,
                                color = colors.onBackground
                            )
                        } else {
                            // Producto normal: botones +/- con cantidad entera
                            IconButton(
                                onClick = {
                                    if (cantidad > 1) cantidad -= 1.0
                                }
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Restar",
                                    tint = colors.onBackground
                                )
                            }
                            Text(
                                text = cantidad.toInt().toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onBackground,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            IconButton(
                                onClick = { cantidad += 1.0 }
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Añadir",
                                    tint = colors.onBackground
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Añadir a la lista de la compra
                        Button(
                            modifier = Modifier
                                .height(56.dp)
                                .weight(1f)
                                .onGloballyPositioned { coords ->
                                    listaButtonPos = coords.positionInRoot()
                                },
                            onClick = {
                                scope.launch {
                                    val listaActivaId = ListasRepository.getListaActivaId()
                                    if (listaActivaId != null) {
                                        ListasRepository.addItem(p.id, 1.0)
                                        flyState?.trigger(listaButtonPos, FlyTarget.LISTA)
//                                        Toast.makeText(
//                                            context,
//                                            "Añadido a la lista de la compra",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "No hay ninguna lista activa",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
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
                            modifier = Modifier
                                .height(56.dp)
                                .weight(1f)
                                .onGloballyPositioned { coords ->
                                    carritoButtonPos = coords.positionInRoot()
                                },
                            onClick = {
                                scope.launch {
                                    CarritoRepository.addCarrito(p, cantidad)
                                    flyState?.trigger(carritoButtonPos, FlyTarget.CARRITO)
                                    val textoToast = if (esAlPeso) {
                                        "Añadido ${"%.2f".format(cantidad)} ${p.unidad ?: "kg"} al carrito"
                                    } else {
                                        "Añadido x${cantidad.toInt()} al carrito"
                                    }
                                    //Toast.makeText(context, textoToast, Toast.LENGTH_SHORT).show()
                                }
                            },
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

                    Spacer(Modifier.height(9.dp))
                }
            }
        }
    }

//    // Diálogo para la edición de detalles del producto
//    if (mostrarDialogo) {
//        EditarProductoDialog(
//            producto = p,
//            onDismiss = { mostrarDialogo = false },
//            onSave = { nombre, precio, descripcion ->
//                scope.launch {
//                    ProductosRepository.actualizarProducto(p.id, nombre, precio, descripcion)
//                    producto =
//                        producto?.copy(nombre = nombre, precio = precio, descripcion = descripcion)
//                    mostrarDialogo = false
//                }
//            }
//        )
//    }
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
    icono: ImageVector,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 0.dp, start = 28.dp, end = 28.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
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
        }
    }
}


// Diálogo para editar producto (depecado)
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


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlergenosOverlay(
    contiene: List<String>,
    trazas: List<String>
) {
    val colors = MaterialTheme.colorScheme

    // Iconos representativos de alérgenos
    val iconosAlergenos = mapOf(
        "pescado" to Lucide.Fish,
        "leche" to Lucide.Milk,
        "huevo" to Lucide.Egg,
        "gluten" to Lucide.Wheat,
        "soja" to Lucide.Sprout,
        "mostaza" to Lucide.Bean,
        "marisco" to Lucide.Shell,
        "sulfitos" to Lucide.Atom,
        "frutos secos" to Lucide.Nut,
        "otros" to Lucide.Info
    )

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(2.dp)
            .wrapContentSize()
    ) {
        // Alérgenos que contiene (color fuerte)
        contiene.forEach { alergeno ->
            val icono = iconosAlergenos[alergeno.trim().lowercase()] ?: Lucide.Info
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.secondary.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            Toast.makeText(context, "Contiene $alergeno", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = alergeno,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

            }
            Spacer(Modifier.height(6.dp))
        }

        // Trazas (color suave)
        trazas.forEach { alergeno ->
            val icono = iconosAlergenos[alergeno.trim().lowercase()] ?: Lucide.Info
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.secondary.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            Toast.makeText(context, "Puede contener trazas de $alergeno", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = alergeno,
                        tint = colors.onBackground.copy(alpha = 0.9f),
                        modifier = Modifier.size(18.dp)
                    )
                }

            }
            Spacer(Modifier.height(6.dp))
        }
    }
}
