package com.example.persistencia.Pantallas

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.persistencia.Herramientas.DescuentosRepository
import com.example.persistencia.Herramientas.ProductosRepository
import com.example.persistencia.Herramientas.UsuariosRepository
import com.example.persistencia.Herramientas.fondoDegradado
import com.example.persistencia.Modelos.Descuento
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Navegacion.AppScreens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.example.persistencia.Herramientas.FlyTarget
import com.example.persistencia.Herramientas.LocalFlyToTargetState
import androidx.compose.ui.geometry.Offset
import com.example.persistencia.Herramientas.CarritoRepository
import com.example.persistencia.Herramientas.ListasRepository

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Catalogo(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var productosFiltrados by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var textoBusqueda by rememberSaveable { mutableStateOf("") }
    var categoriaSeleccionada by rememberSaveable { mutableStateOf("Todos") }
    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }
    val gridState = rememberLazyGridState()
    var ordenSeleccionado by rememberSaveable { mutableStateOf("nombre") }
    var menuOrdenExpandido by remember { mutableStateOf(false) }

    // Estado para controlar la carga
    var cargando by remember { mutableStateOf(true) }

    val sugerencias = remember(textoBusqueda, productos) {
        if (textoBusqueda.length > 2) {
            productos.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
        } else emptyList()
    }

    var ofertas by remember { mutableStateOf<List<Descuento>>(emptyList()) }
    var cuponesDisponibles by remember { mutableStateOf<List<Descuento>>(emptyList()) }
    var cuponesUsuario by remember { mutableStateOf<List<String>>(emptyList()) }

    // Carga datos necesarios para el catálogo
    LaunchedEffect(Unit) {
        cargando = true
        productos = ProductosRepository.getTodos()
        ofertas = DescuentosRepository.getOfertas()
        cuponesDisponibles = DescuentosRepository.getCupones()
        cuponesUsuario = UsuariosRepository.getCupones()
        delay(200L)
        cargando = false // Finaliza la carga
    }

    // Carga productos según los filtros, búsquedas y demás
    LaunchedEffect(textoBusqueda, productos, categoriaSeleccionada, ordenSeleccionado) {
        productosFiltrados = productos.filter {
            (categoriaSeleccionada == "Todos" || it.categoria == categoriaSeleccionada) &&
                    it.nombre.contains(textoBusqueda, ignoreCase = true)
        }.let { filtrados ->
            when (ordenSeleccionado) {
                "nombre" -> filtrados.sortedBy { it.nombre.lowercase() }
                "precio_asc" -> filtrados.sortedBy { it.precio }
                "precio_desc" -> filtrados.sortedByDescending { it.precio }
                "categoria" -> filtrados.sortedBy { it.categoria }
                else -> filtrados
            }
        }
    }

    // Volver al inicio del scroll
    LaunchedEffect(ordenSeleccionado) {
        gridState.scrollToItem(0)
    }

    val backgroundModifier = Modifier.fondoDegradado()

    // Lista de categorías de productos
    val categoriasNav = listOf(
        NavigationItems("Todos", Lucide.Store, Lucide.Store),
        NavigationItems("Frutas y Verduras", Lucide.Apple, Lucide.Apple),
        NavigationItems("Carnicería", Lucide.Beef, Lucide.Beef),
        NavigationItems("Pescadería", Lucide.Fish, Lucide.Fish),
        NavigationItems("Despensa", Lucide.Bean, Lucide.Bean),
        NavigationItems("Panadería y Pastelería", Lucide.CakeSlice, Lucide.CakeSlice),
        NavigationItems("Conservas", Lucide.Cuboid, Lucide.Cuboid),
        NavigationItems("Lácteos", Lucide.Milk, Lucide.Milk),
        NavigationItems("Bebidas", Lucide.Wine, Lucide.Wine),
        NavigationItems("Aperitivos", Lucide.Popcorn, Lucide.Popcorn),
        NavigationItems("Congelados", Lucide.Snowflake, Lucide.Snowflake),
        NavigationItems("Dulces", Lucide.Candy, Lucide.Candy),
        NavigationItems("Textil", Lucide.Shirt, Lucide.Shirt)
    )

    // Modal lateral con las categorías
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
                // Lazycolumn scrollable
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categoriasNav.size) { index ->
                        val item = categoriasNav[index]
                        NavigationDrawerItem(
                            label = { Text(item.title) },
                            selected = index == selectedItemIndex,
                            // Al hacer click, filtra por categoría y cierra el modal
                            onClick = {
                                selectedItemIndex = index
                                categoriaSeleccionada = item.title
                                scope.launch { drawerState.close() }
                            },
                            icon = { Icon(item.selectedIcon, item.selectedIcon.name, Modifier.size(20.dp)) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).height(55.dp)
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Catálogo", fontWeight = FontWeight.ExtraBold) },
                    // Botón para abrir el modal
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menú")
                        }
                    },
                    actions = {
                        // Selector de orden
                        Box {
                            IconButton(onClick = { menuOrdenExpandido = true }) {
                                Icon(Icons.Default.SwapVert, "Ordenar")
                            }

                            DropdownMenu(
                                expanded = menuOrdenExpandido,
                                onDismissRequest = { menuOrdenExpandido = false }
                            ) {
                                listOf(
                                    "nombre" to "Nombre",
                                    "precio_asc" to "Precio ascendente",
                                    "precio_desc" to "Precio descendente",
                                    "categoria" to "Categoría"
                                ).forEach { (id, label) ->
                                    val isSelected = id == ordenSeleccionado

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                label,
                                                color = if (isSelected) colors.primary else colors.onSurface
                                            )
                                        },
                                        onClick = {
                                            ordenSeleccionado = id
                                            menuOrdenExpandido = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(modifier = backgroundModifier.padding(padding)) {

                // Capa de productos
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
                            state = gridState,
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(productosFiltrados, key = { it.id }) { producto ->
                                TarjetaProducto(
                                    producto,
                                    navController,
                                    ofertas,
                                    cuponesDisponibles,
                                    cuponesUsuario
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }

                // Capa de búsqueda y sugerencias
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .zIndex(10f)
                ) {
                    // Barra de búsqueda
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

                    // Muestra las sugerencias según la búsqueda
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
fun TarjetaProducto(
    producto: Producto,
    navController: NavController,
    ofertas: List<Descuento>,
    cuponesDisponibles: List<Descuento>,
    cuponesUsuario: List<String>
) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val flyState = LocalFlyToTargetState.current
    var menuPosition by remember { mutableStateOf(Offset.Zero) }

    // Determinar si el producto tiene oferta o cupón aplicables
    val esOferta = producto.oferta != null && ofertas.any { it.codigo == producto.oferta }
    val esCuponUsuario = producto.oferta != null
            && !esOferta
            && cuponesDisponibles.any { it.codigo == producto.oferta }
            && producto.oferta in cuponesUsuario

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        onClick = { navController.navigate(AppScreens.PantallaProducto.route + "/${producto.id}") }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                // Contenedor de imagen
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = producto.imagenUrl,
                        contentDescription = producto.nombre,
                        modifier = Modifier.size(90.dp),
                        contentScale = ContentScale.Fit
                    )

                    // Icono de descuento
                    if (esOferta) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = "Oferta",
                            tint = colors.secondary,
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.TopStart)
                        )
                    } else if (esCuponUsuario) {
                        Icon(
                            imageVector = Lucide.TicketPercent,
                            contentDescription = "Cupón",
                            tint = colors.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.TopStart)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Nombre del producto
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        maxLines = 2,
                        minLines = 1,
                        lineHeight = 16.sp,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )
                }

                // Precio del producto
                Text(
                    text = "%.2f €".format(producto.precio),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = colors.onSurface
                )
            }

            // Menú de opciones
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .onGloballyPositioned { coords ->
                        menuPosition = coords.positionInRoot()
                    }) {
                IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.MoreVert,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = colors.onSurfaceVariant
                    )
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    // Añadir al carrito
                    DropdownMenuItem(
                        text = { Text("Carrito", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.ShoppingCart, null, Modifier.size(18.dp))
                        },
                        onClick = {
                            scope.launch {
                                CarritoRepository.addCarrito(producto, 1.0)
                                flyState?.trigger(menuPosition, FlyTarget.CARRITO)
                                expanded = false
                            }
                        }
                    )

                    // Añadir a la lista
                    DropdownMenuItem(
                        text = { Text("Lista", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Outlined.ListAlt, null, Modifier.size(18.dp)) },
                        onClick = {
                            scope.launch {
                                ListasRepository.addItem(producto.id)
                                flyState?.trigger(menuPosition, FlyTarget.LISTA)
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