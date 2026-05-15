package com.example.persistencia.Pantallas

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.persistencia.Herramientas.fondoDegradado
import com.example.persistencia.Navegacion.AppScreens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaCompra(navController: NavController) {
    val viewModel: ListaCompraViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    val itemsLista by viewModel.itemsLista.collectAsState()
    val productosCatalogo by viewModel.productosCatalogo.collectAsState()
    val carritoCantidades by viewModel.carritoCantidades.collectAsState()
    val sugerencias by viewModel.sugerencias.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val ordenActual by viewModel.ordenActual.collectAsState()
    val listaActivaId by viewModel.listaActivaId.collectAsState()
    val nombreListaActiva by viewModel.nombreListaActiva.collectAsState()

    // Recargar datos cada vez que se entra en la pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarListas()
    }

    var textoBusqueda by remember { mutableStateOf("") }
    var mostrarSugerencias by remember { mutableStateOf(false) }

    var showCrearListaDialog by remember { mutableStateOf(false) }
    var showInvitarDialog by remember { mutableStateOf(false) }
    var emailInvitado by remember { mutableStateOf("") }
    var showEliminarListaDialog by remember { mutableStateOf(false) }

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

    LaunchedEffect(sugerencias) {
        if (sugerencias.isNotEmpty()) {
            mostrarSugerencias = true
        }
    }

    val backgroundModifier = Modifier.fondoDegradado()

    // Diálogos
    if (showCrearListaDialog) {
        var nombre by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCrearListaDialog = false },
            title = { Text("Nueva lista") },
            text = {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la lista") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nombre.isNotBlank()) {
                        scope.launch {
                            viewModel.crearLista(nombre.trim())
                        }
                        showCrearListaDialog = false
                    }
                }) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showCrearListaDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showInvitarDialog) {
        AlertDialog(
            onDismissRequest = { showInvitarDialog = false },
            title = { Text("Invitar a usuario") },
            text = {
                OutlinedTextField(
                    value = emailInvitado,
                    onValueChange = { emailInvitado = it },
                    label = { Text("Correo electrónico") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (emailInvitado.isNotBlank()) {

                        val email = emailInvitado.trim() // IMPORTANTE

                        scope.launch {
                            val success = viewModel.invitarUsuario(email)

                            if (!success) {
                                Toast.makeText(context, "Usuario no encontrado", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(context, "Invitación enviada", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                        showInvitarDialog = false
                        emailInvitado = ""
                    }
                }) { Text("Invitar") }
            },
            dismissButton = {
                TextButton(onClick = { showInvitarDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showEliminarListaDialog) {
        AlertDialog(
            onDismissRequest = { showEliminarListaDialog = false },
            title = { Text("Eliminar lista") },
            text = {
                Text("¿Estás seguro de que quieres eliminar \"${nombreListaActiva}\"? Esta acción no se puede deshacer y se eliminará para todos los miembros.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarLista()
                    showEliminarListaDialog = false
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEliminarListaDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = nombreListaActiva ?: "Lista de la compra",
                        color = colors.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {

                    // Cambiar lista
                    var selectorListasExpandido by remember { mutableStateOf(false) }

                    Box {
                        IconButton(onClick = { selectorListasExpandido = true }) {
                            Icon(Icons.Default.List, "Cambiar lista")
                        }

                        DropdownMenu(
                            expanded = selectorListasExpandido,
                            onDismissRequest = { selectorListasExpandido = false }
                        ) {
                            viewModel.listasUsuario.collectAsState().value.forEach { lista ->

                                val isSelected = lista.id == listaActivaId

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            lista.nombre,
                                            color = if (isSelected)
                                                colors.primary
                                            else
                                                colors.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.seleccionarLista(lista.id)
                                        selectorListasExpandido = false
                                    }
                                )
                            }

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("+ Nueva lista") },
                                onClick = {
                                    selectorListasExpandido = false
                                    showCrearListaDialog = true
                                }
                            )
                        }
                    }

                    // Ordenar productos
                    var menuOrdenExpandido by remember { mutableStateOf(false) }

                    Box {
                        IconButton(onClick = { menuOrdenExpandido = true }) {
                            Icon(Icons.Default.SwapVert, "Ordenar")
                        }

                        DropdownMenu(
                            expanded = menuOrdenExpandido,
                            onDismissRequest = { menuOrdenExpandido = false }
                        ) {
                            listOf(
                                "fecha" to "Orden de añadido",
                                "alfabetico" to "Nombre",
                                "precio" to "Precio",
                                "categoria" to "Categoría"
                            ).forEach { (id, label) ->

                                val isSelected = id == ordenActual

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            label,
                                            color = if (isSelected)
                                                colors.primary
                                            else
                                                colors.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.actualizarOrden(id)
                                        menuOrdenExpandido = false
                                    }
                                )
                            }
                        }
                    }

                    // ================== 3. MÁS ==================
                    var menuOpcionesExpandido by remember { mutableStateOf(false) }

                    Box {
                        IconButton(onClick = { menuOpcionesExpandido = true }) {
                            Icon(Icons.Default.MoreVert, "Opciones")
                        }

                        DropdownMenu(
                            expanded = menuOpcionesExpandido,
                            onDismissRequest = { menuOpcionesExpandido = false }
                        ) {

                            DropdownMenuItem(
                                text = { Text("Sugerencias") },
                                onClick = {
                                    viewModel.generarSugerencias()
                                    menuOpcionesExpandido = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Desmarcar todo") },
                                onClick = {
                                    scope.launch { viewModel.desmarcarTodo() }
                                    menuOpcionesExpandido = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Eliminar todo", color = colors.error) },
                                onClick = {
                                    scope.launch { viewModel.eliminarTodo() }
                                    menuOpcionesExpandido = false
                                }
                            )

                            // Opciones solo para el creador de la lista
                            if (viewModel.esOwner) {
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Invitar usuario") },
                                    onClick = {
                                        menuOpcionesExpandido = false
                                        showInvitarDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar lista", color = colors.error) },
                                    onClick = {
                                        menuOpcionesExpandido = false
                                        showEliminarListaDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },

        ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (listaActivaId == null) {
                // No hay lista seleccionada
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No tienes ninguna lista aún",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showCrearListaDialog = true }) {
                            Text("Crear mi primera lista")
                        }
                    }
                }
            } else {
                // Hay lista activa: interfaz normal
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
                                                scope.launch {
                                                    viewModel.generarSugerencias()
                                                    mostrarSugerencias = true
                                                }
                                            },
                                            onProductoClick = {
                                                navController.navigate(
                                                    "${AppScreens.PantallaProducto.route}/${item.id}"
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                            item { Spacer(Modifier.height(100.dp)) }
                        }
                    }
                }

                // Buscador flotante (solo si hay lista)
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
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = colors.primary) },
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
                        "Sugerencias para tu lista",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(sugerencias) { producto ->
                            ListItem(
                                headlineContent = { Text(producto.nombre) },
                                supportingContent = { Text("${producto.precio} €") },
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                viewModel.addItem(producto.id)
                                                mostrarSugerencias = false
                                                viewModel.limpiarSugerencias()
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AddCircleOutline,
                                            contentDescription = "Añadir",
                                            tint = colors.onBackground
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

// Tarjeta que representa cada producto de la lista
@Composable
fun TarjetaProductoLista(
    item: ProductoLista,
    datosProducto: Producto?,
    estaEnCarrito: Boolean,
    estaMarcado: Boolean,
    onCheckChanged: (Boolean) -> Unit,
    onCantidadChanged: (Double) -> Unit,
    onVerAlternativas: (Producto) -> Unit,
    onProductoClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        onClick = onProductoClick
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
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        "Producto no disponible",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onVerAlternativas(datosProducto)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Sin stock",
                                    tint = colors.secondary
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
                        mutableStateOf(item.cantidad.toString().replace('.', ','))
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