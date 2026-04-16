package com.example.persistencia.Pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Modelos.ProductoLista
import com.example.persistencia.Herramientas.ListaCompraViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaCompra(navController: NavController) {
    val viewModel: ListaCompraViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current

    val itemsLista by viewModel.itemsLista.collectAsState()
    val productosCatalogo by viewModel.productosCatalogo.collectAsState()
    val carritoCantidades by viewModel.carritoCantidades.collectAsState()
    val sugerencias by viewModel.sugerencias.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val ordenActual by viewModel.ordenActual.collectAsState()

    var textoBusqueda by remember { mutableStateOf("") }
    var mostrarTotalCarrito by remember { mutableStateOf(false) }
    var menuOrdenExpandido by remember { mutableStateOf(false) }
    var menuMasivoExpandido by remember { mutableStateOf(false) }
    var mostrarSugerencias by remember { mutableStateOf(false) }

    // Filtrado de sugerencias según búsqueda
    val sugerenciasBusqueda = remember(textoBusqueda, productosCatalogo) {
        if (textoBusqueda.length > 2) {
            productosCatalogo.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
        } else emptyList()
    }

    // Ordenar items
    val itemsOrdenados = remember(itemsLista, productosCatalogo, ordenActual) {
        when (ordenActual) {
            "alfabetico" -> itemsLista.sortedBy { item ->
                productosCatalogo.find { it.id == item.id }?.nombre?.lowercase() ?: ""
            }

            "precio" -> itemsLista.sortedBy { item ->
                productosCatalogo.find { it.id == item.id }?.precio ?: 0.0
            }

            "categoria" -> itemsLista.sortedBy { item ->
                productosCatalogo.find { it.id == item.id }?.categoria ?: ""
            }

            else -> itemsLista
        }
    }

    // Cálculos de presupuesto
    val presupuestoTotal = remember(itemsLista, productosCatalogo) {
        itemsLista.sumOf { item ->
            (productosCatalogo.find { it.id == item.id }?.precio ?: 0.0) * item.cantidad
        }
    }
    val presupuestoCarrito = remember(itemsLista, productosCatalogo, carritoCantidades) {
        itemsLista.filter { item ->
            val cantidadEnCarrito = carritoCantidades[item.id] ?: 0.0
            cantidadEnCarrito >= item.cantidad
        }.sumOf { item ->
            (productosCatalogo.find { it.id == item.id }?.precio ?: 0.0) * item.cantidad
        }
    }

    // Mostrar modal de sugerencias cuando se generan
    LaunchedEffect(sugerencias) {
        if (sugerencias.isNotEmpty()) {
            mostrarSugerencias = true
        }
    }

    val backgroundModifier = Modifier
        .fillMaxSize()
        .background(colors.background)
        .drawBehind {
            val edgeWidth = with(density) { 25.dp.toPx() }
            val primaryColor = colors.primary.copy(alpha = 0.1f)
            val secondaryColor = colors.secondary.copy(alpha = 0.05f)
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(primaryColor, Color.Transparent),
                    0f,
                    edgeWidth
                ), topLeft = Offset(0f, 0f), size = Size(size.width, edgeWidth)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, primaryColor),
                    size.height - edgeWidth,
                    size.height
                ), topLeft = Offset(0f, size.height - edgeWidth), size = Size(size.width, edgeWidth)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(secondaryColor, Color.Transparent),
                    0f,
                    edgeWidth
                ), topLeft = Offset(0f, 0f), size = Size(edgeWidth, size.height)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, secondaryColor),
                    size.width - edgeWidth,
                    size.width
                ), topLeft = Offset(size.width - edgeWidth, 0f), size = Size(edgeWidth, size.height)
            )
        }

    Box(modifier = backgroundModifier) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Lista de la Compra", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBackIosNew, "Volver")
                        }
                    },
                    actions = {
                        // Botón de sugerencias
                        IconButton(onClick = { viewModel.generarSugerencias() }) {
                            Icon(Icons.Default.Lightbulb, "Sugerencias")
                        }
                        // Menú ordenar
                        IconButton(onClick = { menuOrdenExpandido = true }) {
                            Icon(Icons.Default.Sort, "Ordenar")
                        }
                        DropdownMenu(
                            expanded = menuOrdenExpandido,
                            onDismissRequest = { menuOrdenExpandido = false }) {
                            listOf(
                                "fecha" to "Orden de añadido",
                                "alfabetico" to "Nombre",
                                "precio" to "Precio",
                                "categoria" to "Categoría"
                            ).forEach { (id, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.actualizarOrden(id)
                                        menuOrdenExpandido = false
                                    }
                                )
                            }
                        }
                        // Menú más opciones
                        IconButton(onClick = { menuMasivoExpandido = true }) {
                            Icon(Icons.Default.MoreVert, "Acciones")
                        }
                        DropdownMenu(
                            expanded = menuMasivoExpandido,
                            onDismissRequest = { menuMasivoExpandido = false }) {
                            DropdownMenuItem(
                                text = { Text("Desmarcar todo") },
                                onClick = {
                                    scope.launch { viewModel.desmarcarTodo() }
                                    menuMasivoExpandido = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar todo", color = colors.error) },
                                onClick = {
                                    scope.launch { viewModel.eliminarTodo() }
                                    menuMasivoExpandido = false
                                }
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 4.dp,
                    color = colors.surface
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                if (mostrarTotalCarrito) "En el carro" else "Total estimado",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.secondary
                            )
                            Text(
                                "${"%.2f".format(if (mostrarTotalCarrito) presupuestoCarrito else presupuestoTotal)} €",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                        Button(
                            onClick = { mostrarTotalCarrito = !mostrarTotalCarrito },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                if (mostrarTotalCarrito) Icons.Default.List else Icons.Default.ShoppingCart,
                                null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (mostrarTotalCarrito) "Total" else "Carrito")
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(90.dp))

                    if (isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(itemsOrdenados, key = { it.id }) { item ->
                                val datosProducto = productosCatalogo.find { it.id == item.id }
                                val cantidadEnCarrito = carritoCantidades[item.id] ?: 0.0
                                val estaEnCarrito = cantidadEnCarrito >= item.cantidad
                                val estaMarcado = item.comprado

                                key(item.id) {
                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = { value ->
                                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                                scope.launch { viewModel.eliminarItem(item.id) }
                                                true
                                            } else if (value == SwipeToDismissBoxValue.StartToEnd) {
                                                scope.launch {
                                                    viewModel.cambiarEstado(
                                                        item.id,
                                                        !item.comprado
                                                    )
                                                }
                                                false
                                            } else false
                                        }
                                    )
                                    SwipeToDismissBox(
                                        state = dismissState,
                                        backgroundContent = {
                                            val color = when (dismissState.dismissDirection) {
                                                SwipeToDismissBoxValue.StartToEnd -> Color(
                                                    0xFF4CAF50
                                                )

                                                SwipeToDismissBoxValue.EndToStart -> colors.error
                                                else -> Color.Transparent
                                            }
                                            Box(
                                                Modifier
                                                    .fillMaxSize()
                                                    .background(color, RoundedCornerShape(16.dp))
                                                    .padding(horizontal = 20.dp)
                                            ) {
                                                if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Icon(
                                                    Icons.Default.Check,
                                                    null,
                                                    Modifier.align(Alignment.CenterStart),
                                                    tint = Color.White
                                                )
                                                else if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Icon(
                                                    Icons.Default.Delete,
                                                    null,
                                                    Modifier.align(Alignment.CenterEnd),
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    ) {
                                        TarjetaProductoLista(
                                            item = item,
                                            datosProducto = datosProducto,
                                            estaEnCarrito = estaEnCarrito,
                                            estaMarcado = estaMarcado,
                                            onCheckChanged = { checked ->
                                                scope.launch {
                                                    viewModel.cambiarEstado(
                                                        item.id,
                                                        checked
                                                    )
                                                }
                                            },
                                            onCantidadChanged = { nuevaCantidad ->
                                                scope.launch {
                                                    viewModel.actualizarCantidad(
                                                        item.id,
                                                        nuevaCantidad
                                                    )
                                                }
                                            },
                                            onVerAlternativas = { prod ->
                                                // Mostrar diálogo con alternativas
                                            }
                                        )
                                    }
                                }
                            }
                            item { Spacer(Modifier.height(100.dp)) }
                        }
                    }
                }

                // Buscador flotante
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .zIndex(10f)
                ) {
                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = { textoBusqueda = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Añadir producto...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (textoBusqueda.isNotEmpty()) {
                                IconButton(onClick = { textoBusqueda = "" }) {
                                    Icon(Icons.Default.Close, "Limpiar")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface
                        )
                    )

                    if (sugerenciasBusqueda.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            elevation = CardDefaults.cardElevation(8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surface)
                        ) {
                            Column {
                                sugerenciasBusqueda.take(6).forEach { prod ->
                                    ListItem(
                                        headlineContent = { Text(prod.nombre) },
                                        supportingContent = { Text("${prod.precio} €") },
                                        modifier = Modifier.clickable {
                                            scope.launch {
                                                viewModel.addItem(prod.id)
                                                textoBusqueda = ""
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Modal de sugerencias
        if (mostrarSugerencias) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = {
                    mostrarSugerencias = false
                    viewModel.limpiarSugerencias()
                },
                sheetState = sheetState
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Sugerencias basadas en tus compras",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(sugerencias) { producto ->
                            ListItem(
                                headlineContent = { Text(producto.nombre) },
                                supportingContent = { Text("${producto.precio} €") },
                                trailingContent = {
                                    Button(onClick = {
                                        scope.launch {
                                            viewModel.addItem(producto.id)
                                            mostrarSugerencias = false
                                            viewModel.limpiarSugerencias()
                                        }
                                    }) {
                                        Text(
                                            text = "Añadir",
                                            color = colors.onBackground
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaProductoLista(
    item: ProductoLista,
    datosProducto: Producto?,
    estaEnCarrito: Boolean,
    estaMarcado: Boolean,
    onCheckChanged: (Boolean) -> Unit,
    onCantidadChanged: (Double) -> Unit,
    onVerAlternativas: (Producto) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.comprado,
                    onCheckedChange = onCheckChanged,
                    colors = CheckboxDefaults.colors(colors.secondary)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = datosProducto?.nombre ?: "...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textDecoration = if (estaEnCarrito) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (estaMarcado || estaEnCarrito) colors.onSurface.copy(alpha = 0.5f) else colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (datosProducto?.stock == 0) {
                            IconButton(
                                onClick = { onVerAlternativas(datosProducto) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Sin stock",
                                    tint = colors.error
                                )
                            }
                        }
                    }
                    if (datosProducto?.al_peso == true) {
                        Text(
                            text = "%.2f €/${datosProducto.unidad ?: "kg"}".format(datosProducto.precio),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (estaMarcado || estaEnCarrito) colors.onSurfaceVariant.copy(
                                alpha = 0.5f
                            ) else colors.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "${"%.2f".format(datosProducto?.precio ?: 0.0)} €/ud",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (estaMarcado || estaEnCarrito) colors.onSurfaceVariant.copy(
                                alpha = 0.5f
                            ) else colors.onSurfaceVariant
                        )
                    }
                }
                // Controles de cantidad
                if (datosProducto?.al_peso == true) {
                    var pesoText by remember(item.id) {
                        mutableStateOf(
                            item.cantidad.toString().replace('.', ',')
                        )
                    }
                    OutlinedTextField(
                        value = pesoText,
                        onValueChange = { newValue ->
                            val filtered =
                                newValue.filter { it.isDigit() || it == '.' || it == ',' }
                            pesoText = filtered
                            val normalized = filtered.replace(',', '.')
                            val newCantidad = normalized.toDoubleOrNull()
                            if (newCantidad != null && newCantidad > 0.0) {
                                onCantidadChanged(newCantidad)
                            } else if (filtered.isEmpty()) {
                                pesoText = ""
                            }
                        },
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = if (estaMarcado || estaEnCarrito) colors.onSurface.copy(alpha = 0.5f) else colors.onSurface
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline
                        )
                    )
                    Text(
                        text = datosProducto.unidad ?: "kg",
                        modifier = Modifier.padding(start = 4.dp),
                        color = if (estaMarcado || estaEnCarrito) colors.onSurface.copy(alpha = 0.5f) else colors.onSurface
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onCantidadChanged((item.cantidad - 1).coerceAtLeast(1.0)) },
                            modifier = Modifier.size(32.dp)
                        ) { Icon(Icons.Default.Remove, null, modifier = Modifier.size(20.dp)) }
                        Text(
                            text = item.cantidad.toInt().toString(),
                            fontWeight = FontWeight.Bold,
                            color = if (estaMarcado || estaEnCarrito) colors.onSurface.copy(alpha = 0.5f) else colors.onSurface
                        )
                        IconButton(
                            onClick = { onCantidadChanged(item.cantidad + 1) },
                            modifier = Modifier.size(32.dp)
                        ) { Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp)) }
                    }
                }
            }
            // Icono de oferta
            if (datosProducto?.oferta != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(20.dp)
                        .background(colors.secondary.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                ) {
                    Icon(
                        Icons.Default.LocalOffer,
                        contentDescription = "Oferta",
                        tint = Color.White,
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}