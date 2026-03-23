package com.example.persistencia.Pantallas

import androidx.compose.animation.animateColorAsState
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
import com.example.persistencia.Modelos.ItemLista
import com.example.persistencia.Modelos.Producto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaCompra(navController: NavController) {
    val listasDao = remember { ListasDao() }
    val productosDao = remember { ProductosDao() }
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current

    var itemsLista by remember { mutableStateOf(listOf<ItemLista>()) }
    var productosCatalogo by remember { mutableStateOf(listOf<Producto>()) }
    var textoBusqueda by remember { mutableStateOf("") }
    var sugerencias by remember { mutableStateOf(listOf<Producto>()) }

    LaunchedEffect(Unit) {
        itemsLista = listasDao.getLista()
        productosCatalogo = productosDao.getTodos()
    }

    LaunchedEffect(textoBusqueda) {
        sugerencias = if (textoBusqueda.length > 2) {
            productosCatalogo.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
        } else emptyList()
    }

    // Fondo degradado por los bordes (como en Catalogo.kt)
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
                brush = Brush.verticalGradient(listOf(primaryColor, Color.Transparent), 0f, edgeWidth),
                topLeft = Offset(0f, 0f), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.verticalGradient(listOf(Color.Transparent, primaryColor), height - edgeWidth, height),
                topLeft = Offset(0f, height - edgeWidth), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.horizontalGradient(listOf(secondaryColor, Color.Transparent), 0f, edgeWidth),
                topLeft = Offset(0f, 0f), size = Size(edgeWidth, height)
            )
            drawRect(
                brush = Brush.horizontalGradient(listOf(Color.Transparent, secondaryColor), width - edgeWidth, width),
                topLeft = Offset(width - edgeWidth, 0f), size = Size(edgeWidth, height)
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
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {

                // Buscador flotante
                Box(modifier = Modifier.padding(16.dp).zIndex(5f)) {
                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = { textoBusqueda = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Añadir producto...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface
                        )
                    )

                    if (sugerencias.isNotEmpty()) {
                        Card(
                            modifier = Modifier.padding(top = 62.dp).fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(8.dp),
                            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                        ) {
                            Column {
                                sugerencias.take(6).forEach { prod ->
                                    ListItem(
                                        headlineContent = { Text(prod.nombre) },
                                        modifier = Modifier.clickable {
                                            scope.launch {
                                                listasDao.agregarOIncrementar(prod.id)
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

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(itemsLista, key = { it.id }) { item ->
                        val datosProducto = productosCatalogo.find { it.id == item.id }

                        key(item.id) {
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        scope.launch {
                                            listasDao.eliminarItem(item.id)
                                            itemsLista = listasDao.getLista()
                                        }
                                        true
                                    } else if (value == SwipeToDismissBoxValue.StartToEnd) {
                                        scope.launch {
                                            listasDao.cambiarEstadoComprado(item.id, !item.comprado)
                                            itemsLista = listasDao.getLista()
                                        }
                                        false // Para que rebote y no se quede desplazado
                                    } else false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val direction = dismissState.dismissDirection
                                    // El color solo aparece si hay movimiento real
                                    val color = when (direction) {
                                        SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                                        SwipeToDismissBoxValue.EndToStart -> colors.onError
                                        else -> Color.Transparent
                                    }

                                    Box(
                                        Modifier.fillMaxSize()
                                            .background(color, RoundedCornerShape(16.dp))
                                            .padding(horizontal = 20.dp)
                                    ) {
                                        if (direction == SwipeToDismissBoxValue.StartToEnd)
                                            Icon(Icons.Default.Check, null, Modifier.align(Alignment.CenterStart), tint = Color.White)
                                        else
                                            Icon(Icons.Default.Delete, null, Modifier.align(Alignment.CenterEnd), tint = Color.White)
                                    }
                                }
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                                ) {
                                    ListItem(
                                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                        leadingContent = {
                                            Checkbox(
                                                checked = item.comprado,
                                                onCheckedChange = {
                                                    scope.launch {
                                                        listasDao.cambiarEstadoComprado(item.id, it)
                                                        itemsLista = listasDao.getLista()
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(colors.secondary)
                                            )
                                        },
                                        headlineContent = {
                                            Text(
                                                datosProducto?.nombre ?: "...",
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    textDecoration = if (item.comprado) TextDecoration.LineThrough else null
                                                )
                                            )
                                        },
                                        trailingContent = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(onClick = { scope.launch {
                                                    listasDao.actualizarCantidad(item.id, item.cantidad - 1)
                                                    itemsLista = listasDao.getLista()
                                                } }) { Icon(Icons.Default.Remove, null) }

                                                Text("${item.cantidad}", fontWeight = FontWeight.Bold)

                                                IconButton(onClick = { scope.launch {
                                                    listasDao.actualizarCantidad(item.id, item.cantidad + 1)
                                                    itemsLista = listasDao.getLista()
                                                } }) { Icon(Icons.Default.Add, null) }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}