package com.example.persistencia.Pantallas

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.composables.icons.lucide.*
import com.example.persistencia.Firestore.CarritoDao
import com.example.persistencia.Firestore.ListasDao
import com.example.persistencia.Firestore.ProductosDao
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Navegacion.AppScreens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.wait

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Catalogo(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val dao = remember { ProductosDao() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var productosFiltrados by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var textoBusqueda by rememberSaveable { mutableStateOf("") }
    var categoriaSeleccionada by rememberSaveable { mutableStateOf("Todos") }
    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }

    // Estado para controlar la carga
    var cargando by remember { mutableStateOf(true) }

    val sugerencias = remember(textoBusqueda, productos) {
        if (textoBusqueda.length > 2) {
            productos.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
        } else emptyList()
    }

    LaunchedEffect(Unit) {
        cargando = true
        productos = dao.getTodos()
        delay(200L)
        cargando = false // Finaliza la carga
    }

    LaunchedEffect(textoBusqueda, productos, categoriaSeleccionada) {
        productosFiltrados = productos.filter {
            (categoriaSeleccionada == "Todos" || it.categoria == categoriaSeleccionada) &&
                    it.nombre.contains(textoBusqueda, ignoreCase = true)
        }
    }

    val backgroundModifier = Modifier
        .fillMaxSize()
        .background(colors.background)
        .drawBehind {
            val edgeWidth = with(density) { 30.dp.toPx() }
            val primaryAlpha = colors.primary.copy(alpha = 0.08f)
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(primaryAlpha, Color.Transparent),
                    0f,
                    edgeWidth
                ), size = Size(size.width, edgeWidth)
            )
        }

    val categoriasNav = listOf(
        NavigationItems("Todos", Lucide.Store, Lucide.Store),
        NavigationItems("Bebidas", Lucide.Wine, Lucide.Wine),
        NavigationItems("Textil", Lucide.Shirt, Lucide.Shirt),
        NavigationItems("Aperitivos", Lucide.Popcorn, Lucide.Popcorn)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(240.dp),
                drawerContainerColor = colors.surface,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Text(
                    "Categorías",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                categoriasNav.forEachIndexed { index, item ->
                    NavigationDrawerItem(
                        label = { Text(item.title) },
                        selected = index == selectedItemIndex,
                        onClick = {
                            selectedItemIndex = index
                            categoriaSeleccionada = item.title
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(item.selectedIcon, null, Modifier.size(20.dp)) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Catálogo", fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menú")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(modifier = backgroundModifier.padding(padding)) {

                // Capa de productos (Fondo)
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(70.dp))

                    if (cargando) {
                        // Mientras cargan los productos, se muestra el círculo de carga
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = colors.primary,
                                strokeWidth = 3.dp
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(productosFiltrados, key = { it.id }) { producto ->
                                TarjetaProducto(producto, navController)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(70.dp))
                }

                // Capa de búsqueda y sugerencias
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .zIndex(10f)
                ) {
                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = { textoBusqueda = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("¿Qué buscas?") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = colors.primary) },
                        trailingIcon = {
                            if (textoBusqueda.isNotEmpty()) {
                                IconButton(onClick = { textoBusqueda = "" }) {
                                    Icon(Icons.Default.Close, null)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface
                        ),
                        singleLine = true
                    )

                    if (sugerencias.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            elevation = CardDefaults.cardElevation(8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surface)
                        ) {
                            Column {
                                sugerencias.take(5).forEach { prod ->
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                prod.nombre,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        supportingContent = {
                                            Text(
                                                "${prod.precio} €",
                                                fontSize = 12.sp
                                            )
                                        },
                                        leadingContent = {
                                            AsyncImage(
                                                model = prod.imagenUrl,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Fit
                                            )
                                        },
                                        modifier = Modifier.clickable {
                                            navController.navigate(AppScreens.PantallaProducto.route + "/${prod.id}")
                                            textoBusqueda = ""
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

@Composable
fun TarjetaProducto(producto: Producto, navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val daoCarrito = remember { CarritoDao() }
    val daoLista = remember { ListasDao() }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        onClick = { navController.navigate(AppScreens.PantallaProducto.route + "/${producto.id}") }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)) {
            Column(horizontalAlignment = Alignment.Start) {
                // Contenedor de imagen con altura fija para alinear el texto inferior
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = producto.imagenUrl,
                        contentDescription = producto.nombre,
                        modifier = Modifier.size(70.dp),
                        contentScale = ContentScale.Fit
                    )

                    if (producto.oferta != null) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = "Oferta",
                            tint = colors.secondary,
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.TopStart)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 2,
                    minLines = 2,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )

                Text(
                    text = "%.2f €".format(producto.precio),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = colors.onSurface
                )
            }

            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.MoreVert,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = colors.onSurfaceVariant
                    )
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Carrito", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.ShoppingCart,
                                null,
                                Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            scope.launch {
                                daoCarrito.addCarrito(producto, 1)
                                Toast.makeText(context, "Añadido", Toast.LENGTH_SHORT).show()
                                expanded = false
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Lista", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Outlined.ListAlt, null, Modifier.size(18.dp)) },
                        onClick = {
                            scope.launch {
                                daoLista.addItem(producto.id)
                                Toast.makeText(context, "A la lista", Toast.LENGTH_SHORT).show()
                                expanded = false
                            }
                        }
                    )
                }
            }
        }
    }
}

data class NavigationItems(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null
)