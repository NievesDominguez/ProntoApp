package com.example.persistencia.Pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.persistencia.Firestore.ListasDao
import com.example.persistencia.Firestore.ProductosDao
import com.example.persistencia.Firestore.UsuariosDao
import com.example.persistencia.Modelos.ItemLista
import com.example.persistencia.Modelos.Producto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaCompra(navController: NavController) {
    val listasDao = remember { ListasDao() }
    val productosDao = remember { ProductosDao() }
    val usuariosDao = remember { UsuariosDao() }
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current

    // Estados de datos
    var itemsLista by remember { mutableStateOf(listOf<ItemLista>()) }
    var productosCatalogo by remember { mutableStateOf(listOf<Producto>()) }
    var textoBusqueda by remember { mutableStateOf("") }
    var sugerencias by remember { mutableStateOf(listOf<Producto>()) }

    // Estados de configuración de vista
    var ordenActual by remember { mutableStateOf("fecha") }
    var mostrarTotalCarrito by remember { mutableStateOf(false) }
    var menuOrdenExpandido by remember { mutableStateOf(false) }
    var menuMasivoExpandido by remember { mutableStateOf(false) }

    // Carga inicial de datos y preferencias del usuario
    LaunchedEffect(Unit) {
        itemsLista = listasDao.getLista()
        productosCatalogo = productosDao.getTodos()
        ordenActual = usuariosDao.getOrdenLista()
    }

    // Filtrado de sugerencias según el texto de búsqueda
    LaunchedEffect(textoBusqueda) {
        sugerencias = if (textoBusqueda.length > 2) {
            productosCatalogo.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
        } else emptyList()
    }

    // Lógica para ordenar la lista en memoria según la opción elegida
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
            else -> itemsLista // Orden por defecto (fecha de añadido en Firestore)
        }
    }

    // Cálculo de precios para el panel inferior
    val presupuestoTotal = remember(itemsLista, productosCatalogo) {
        itemsLista.sumOf { item ->
            (productosCatalogo.find { it.id == item.id }?.precio ?: 0.0) * item.cantidad
        }
    }
    val presupuestoCarrito = remember(itemsLista, productosCatalogo) {
        itemsLista.filter { it.comprado }.sumOf { item ->
            (productosCatalogo.find { it.id == item.id }?.precio ?: 0.0) * item.cantidad
        }
    }

    // Configuración del fondo con degradados suaves en los bordes
    val backgroundModifier = Modifier
        .fillMaxSize()
        .background(colors.background)
        .drawBehind {
            val edgeWidth = with(density) { 25.dp.toPx() }
            val primaryColor = colors.primary.copy(alpha = 0.1f)
            val secondaryColor = colors.secondary.copy(alpha = 0.05f)
            drawRect(brush = Brush.verticalGradient(listOf(primaryColor, Color.Transparent), 0f, edgeWidth), topLeft = Offset(0f, 0f), size = Size(size.width, edgeWidth))
            drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, primaryColor), size.height - edgeWidth, size.height), topLeft = Offset(0f, size.height - edgeWidth), size = Size(size.width, edgeWidth))
            drawRect(brush = Brush.horizontalGradient(listOf(secondaryColor, Color.Transparent), 0f, edgeWidth), topLeft = Offset(0f, 0f), size = Size(edgeWidth, size.height))
            drawRect(brush = Brush.horizontalGradient(listOf(Color.Transparent, secondaryColor), size.width - edgeWidth, size.width), topLeft = Offset(size.width - edgeWidth, 0f), size = Size(edgeWidth, size.height))
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
                        // Menú para cambiar el orden de los productos
                        IconButton(onClick = { menuOrdenExpandido = true }) {
                            Icon(Icons.Default.Sort, "Ordenar")
                        }
                        DropdownMenu(expanded = menuOrdenExpandido, onDismissRequest = { menuOrdenExpandido = false }) {
                            listOf("fecha" to "Orden de añadido", "alfabetico" to "Nombre", "precio" to "Precio", "categoria" to "Categoría").forEach { (id, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        ordenActual = id
                                        menuOrdenExpandido = false
                                        scope.launch { usuariosDao.setOrdenLista(id) }
                                    }
                                )
                            }
                        }
                        // Menú para acciones sobre toda la lista
                        IconButton(onClick = { menuMasivoExpandido = true }) {
                            Icon(Icons.Default.MoreVert, "Acciones")
                        }
                        DropdownMenu(expanded = menuMasivoExpandido, onDismissRequest = { menuMasivoExpandido = false }) {
                            DropdownMenuItem(
                                text = { Text("Desmarcar todo") },
                                onClick = {
                                    scope.launch { listasDao.desmarcarTodo(); itemsLista = listasDao.getLista() }
                                    menuMasivoExpandido = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar todo", color = colors.error) },
                                onClick = {
                                    scope.launch { listasDao.eliminarTodo(); itemsLista = emptyList() }
                                    menuMasivoExpandido = false
                                }
                            )
                        }
                    }
                )
            },
            bottomBar = {
                // Barra inferior informativa con el coste de la compra
                Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 4.dp, color = colors.surface) {
                    Row(modifier = Modifier.padding(16.dp).navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(if (mostrarTotalCarrito) "En el carro" else "Total estimado", style = MaterialTheme.typography.labelMedium, color = colors.secondary)
                            Text("${"%.2f".format(if (mostrarTotalCarrito) presupuestoCarrito else presupuestoTotal)} €", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.primary)
                        }
                        Button(onClick = { mostrarTotalCarrito = !mostrarTotalCarrito }, shape = RoundedCornerShape(12.dp)) {
                            Icon(if (mostrarTotalCarrito) Icons.Default.List else Icons.Default.ShoppingCart, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (mostrarTotalCarrito) "Total" else "Carrito")
                        }
                    }
                }
            }
        ) { padding ->
            // Contenedor principal que permite superponer el buscador a la lista
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {

                // Capa de la lista de productos
                Column(modifier = Modifier.fillMaxSize()) {
                    // Espacio reservado para que el buscador flotante no tape el primer item
                    Spacer(modifier = Modifier.height(90.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(itemsOrdenados, key = { it.id }) { item ->
                            val datosProducto = productosCatalogo.find { it.id == item.id }
                            key(item.id) {
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        if (value == SwipeToDismissBoxValue.EndToStart) {
                                            scope.launch { listasDao.eliminarItem(item.id); itemsLista = listasDao.getLista() }
                                            true
                                        } else if (value == SwipeToDismissBoxValue.StartToEnd) {
                                            scope.launch { listasDao.cambiarEstado(item.id, !item.comprado); itemsLista = listasDao.getLista() }
                                            false
                                        } else false
                                    }
                                )
                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        val color = when (dismissState.dismissDirection) {
                                            SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                                            SwipeToDismissBoxValue.EndToStart -> colors.error
                                            else -> Color.Transparent
                                        }
                                        Box(Modifier.fillMaxSize().background(color, RoundedCornerShape(16.dp)).padding(horizontal = 20.dp)) {
                                            if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Icon(Icons.Default.Check, null, Modifier.align(Alignment.CenterStart), tint = Color.White)
                                            else if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Icon(Icons.Default.Delete, null, Modifier.align(Alignment.CenterEnd), tint = Color.White)
                                        }
                                    }
                                ) {
                                    Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                                        Row(modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = item.comprado,
                                                onCheckedChange = { scope.launch { listasDao.cambiarEstado(item.id, it); itemsLista = listasDao.getLista() } },
                                                colors = CheckboxDefaults.colors(colors.secondary)
                                            )
                                            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    // Icono de oferta si el producto tiene una activa
                                                    if (datosProducto?.oferta != null) {
                                                        Icon(Icons.Default.LocalOffer, "Oferta", tint = colors.secondary, modifier = Modifier.size(14.dp).padding(end = 4.dp))
                                                    }
                                                    Text(datosProducto?.nombre ?: "...", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, textDecoration = if (item.comprado) TextDecoration.LineThrough else null, fontSize = 15.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                                Text("${datosProducto?.precio ?: 0.0} €/ud", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(onClick = { scope.launch { listasDao.actualizarCantidad(item.id, item.cantidad - 1); itemsLista = listasDao.getLista() } }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Remove, null, modifier = Modifier.size(20.dp)) }
                                                Text("${item.cantidad}", fontWeight = FontWeight.Bold)
                                                IconButton(onClick = { scope.launch { listasDao.actualizarCantidad(item.id, item.cantidad + 1); itemsLista = listasDao.getLista() } }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp)) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(100.dp)) }
                    }
                }

                // Capa del buscador (se dibuja encima gracias a zIndex)
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

                    // Lista de sugerencias que flota sobre la lista de la compra
                    if (sugerencias.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            elevation = CardDefaults.cardElevation(8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surface)
                        ) {
                            Column {
                                sugerencias.take(6).forEach { prod ->
                                    ListItem(
                                        headlineContent = { Text(prod.nombre) },
                                        supportingContent = { Text("${prod.precio} €") },
                                        modifier = Modifier.clickable {
                                            scope.launch {
                                                listasDao.addItem(prod.id)
                                                textoBusqueda = ""
                                                itemsLista = listasDao.getLista()
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
    }
}