package com.example.persistencia.Pantallas

import android.R.attr.query
import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.scrollableArea
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.room.util.copy
import coil.compose.AsyncImage
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.ListCheck
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plug
import com.composables.icons.lucide.Popcorn
import com.composables.icons.lucide.Shirt
import com.composables.icons.lucide.Store
import com.composables.icons.lucide.Wine
import com.example.persistencia.Firestore.CarritoDao
import com.example.persistencia.Firestore.ProductosDao
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Navegacion.AppScreens
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Catalogo(navController: NavController) {

    // Clase local para los elementos del carousel
    data class CarouselItem(
        val id: Int,
        val imgLink: String,
        val contentDescription: String
    )

    // Degradado de fondo semitransparente
    val gradient2 = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF6C3AEC).copy(alpha = 0.5f),
            Color(0xFF6C3AEC).copy(alpha = 0.5f)
        )
    )

    // Degradado magenta a morado
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFD13CF2),
            Color(0xFF6C3AEC)
        )
    )

    //Lista de items para la barra lateral de navegación
    val categorias = listOf(
        NavigationItems(
            title = "Todos",
            selectedIcon = Lucide.Store,
            unselectedIcon = Lucide.Store
        ),
        NavigationItems(
            title = "Bebidas",
            selectedIcon = Lucide.Wine,
            unselectedIcon = Lucide.Wine
        ),
        NavigationItems(
            title = "Textil",
            selectedIcon = Lucide.Shirt,
            unselectedIcon = Lucide.Shirt,
            //badgeCount = 105
        ),
        NavigationItems(
            title = "Aperitivos",
            selectedIcon = Lucide.Popcorn,
            unselectedIcon = Lucide.Popcorn
        )
    )

    val dao = ProductosDao() // Dao de la base de datos
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }

    // Cargar productos al entrar en la pantalla
    LaunchedEffect(Unit) {
        productos = dao.getTodos()
    }

    var query by remember { mutableStateOf("") }

    // Filtrado local de productos según la búsqueda
    productos.filter { producto ->
        producto.nombre.contains(query, ignoreCase = true)
    }

    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current // Para acceder al sistema

    // Para el panel lateral de navegación
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        topBar = {
            // TopBar transparente
            TopAppBar(
                modifier = Modifier.height(56.dp),
                title = {
                    Text(
                        text = "Catálogo",
                        // Si el menú lateral está abierto, el título cambia a negro
                        color = if (drawerState.isOpen) Color.Black else Color.White
                    )
                },
                colors = topAppBarColors(
                    containerColor = Color.Transparent, // Transparente para que se vea el fondo
                    titleContentColor = Color.White
                ),
                // Abre o cierra el menú lateral
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open()
                            else drawerState.close()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Barra lateral",
                            // Si el menú lateral está abierto, el icono cambia a negro
                            tint = if (drawerState.isOpen) Color.Black else Color.White
                        )
                    }
                }
            )
        }
    ) {


        // Fondo degradado de la pantalla
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {

            // Barra horizontal de navegación, permite navegar por categorías
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    // Lo metemos dentro de un box para modificar el tamaño
                    Box(modifier = Modifier.width(220.dp)) {
                        ModalDrawerSheet {
                            Spacer(modifier = Modifier.height(40.dp))
                            categorias.forEachIndexed { index, item ->
                                NavigationDrawerItem(
                                    label = { Text(text = item.title) },
                                    selected = index == selectedItemIndex,
                                    onClick = {
                                        selectedItemIndex = index
                                        if (item.title == "Todos") {
                                            categoriaSeleccionada = null
                                            scope.launch {
                                                productos = dao.getTodos()
                                                drawerState.close()
                                            }

                                        } else {
                                            categoriaSeleccionada =
                                                item.title // Guardamos la categoría seleccionada
                                            scope.launch {
                                                drawerState.close()

                                                // Filtrar productos por categoría
                                                productos = dao.getPorCategoria(item.title)
                                            }
                                        }
                                        scope.launch {
                                            drawerState.close()

                                            // Filtrar productos por categoría
                                            productos = dao.getPorCategoria(item.title)
                                        }
                                    },
                                    // Icono correspondiente al item
                                    icon = {
                                        Icon(
                                            imageVector = if (index == selectedItemIndex) {
                                                item.selectedIcon
                                            } else item.unselectedIcon,
                                            contentDescription = item.title
                                        )
                                    },
                                    // Colores del item, cuando está seleccionado o no
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedIconColor = Color(0xFF6C3AEC),
                                        unselectedIconColor = Color.Black,
                                        selectedTextColor = Color(0xFF6C3AEC),
                                        unselectedTextColor = Color.Black,
                                        selectedContainerColor = Color(0xFF6C3AEC).copy(alpha = 0.15f),
                                        unselectedContainerColor = Color.Transparent
                                    ),

                                    // Muestra badge al lateral si el item lo tiene
                                    badge = {
                                        item.badgeCount?.let {
                                            Text(text = item.badgeCount.toString())
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(NavigationDrawerItemDefaults.ItemPadding) //padding between items
                                )
                            }

                        }
                    }
                },
                gesturesEnabled = true // Permite abrir y cerrar con gestos
            ) {


                Column {

                    // BARRA DE BÚSQUEDA
                    var query by remember { mutableStateOf("") } // Texto a buscar

                    var productosFiltrados by remember { mutableStateOf(emptyList<Producto>()) }

                    // Sugerencias mientras se escribe
                    val sugerencias = remember(query, productos) {
                        if (query.isBlank()) emptyList()
                        else productos.filter { it.nombre.contains(query, ignoreCase = true) }
                    }

                    // Cuando los productos cambian (por primera carga o recarga), actualizamos la lista principal para que muestre todos los productos.
                    LaunchedEffect(productos) {
                        productosFiltrados = productos
                    }

                    SearchBarProductos(
                        query = query,
                        onQueryChange = { query = it },

                        // Al pulsar buscar filtra la lista de productos de abajo
                        onSearchConfirmed = {
                            productosFiltrados =
                                if (query.isBlank()) productos
                                else productos.filter {
                                    it.nombre.contains(
                                        query,
                                        ignoreCase = true
                                    )
                                }
                        },

                        sugerencias = sugerencias,

                        // Al hacer click en una sugerencia lleva a la página del producto
                        onProductoClick = { producto ->
                            navController.navigate(AppScreens.PantallaProducto.route + "/${producto.id}")
                        }
                    )


                    // Con un LazyColumn único se puede hacer scroll vertical de toda la pantalla
                    // No usar Column infinito y dentro un LazyColumn
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        // Si hay una categoría seleccionada, muestra el nombre
                        item {
                            categoriaSeleccionada?.let {
                                Text(
                                    modifier = Modifier.padding(24.dp),
                                    text = it,
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }


                        // Grid con dos columnas. No se usa grid porque entra el conflicto con el scroll de la pantalla
                        items(productosFiltrados.chunked(2)) { fila ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {

                                // Dibujamos los productos de la fila
                                fila.forEach { producto ->
                                    TarjetaProducto(
                                        producto,
                                        modifier = Modifier.weight(1f), // Deben repartirse el ancho por igual
                                        navController
                                    )
                                }


                                // Si la fila tiene solo 1 producto, rellenamos el hueco
                                if (fila.size == 1) {
                                    Spacer(modifier = Modifier.width(170.dp))
                                }
                            }
                        }

                        if (productosFiltrados.isEmpty()) {
                            item {
                                Text(
                                    modifier = Modifier.padding(24.dp),
                                    text = "No hay productos que coincidan con la búsqueda",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Espacio final para que el ultimo elemento no quede cortado por el bottombar
                        item {
                            Spacer(Modifier.height(150.dp))
                        }
                    }
                }
            }

        }
    }


}

// Tarjeta, estructura de un producto
@Composable
fun TarjetaProducto(
    producto: Producto,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val daoCarrito = CarritoDao() // Dao del carrito
    val scope = rememberCoroutineScope() // Para ejecutar corrutinas

    val context = LocalContext.current // Para acceder al sistema
    // Permite abrir y cerrar el dropdown
    var expanded by remember { mutableStateOf(false) }

    // Contenedor que permite superponer el dropdown sobre el Card
    Box(
        modifier = modifier
            .height(190.dp)
    ) {

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.8f)
            ),
            onClick = {
                navController.navigate(AppScreens.PantallaProducto.route + "/${producto.id}")
            }
        ) {

            Column(
                modifier = Modifier.padding(12.dp)
            ) {

                Spacer(Modifier.height(4.dp)) // deja espacio para el botón flotante

                // Imagen del producto
                AsyncImage(
                    model = producto.imagenUrl,
                    contentDescription = producto.nombre,
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(8.dp))

                // Nombre
                Text(
                    text = producto.nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Precio
                Text(
                    // El %.2f redondea a 2 decimales
                    text = "%.2f €".format(producto.precio),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6C3AEC)
                )
            }
        }

        // Botón del dropdown menu
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {

            // Al pulsar abre o cierra el dropdown
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {

                // Botón añadir al carrito
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    },
                    text = { Text("Añadir al carrito") },
                    onClick = {
                        scope.launch {
                            Toast.makeText(
                                context,
                                "${producto.nombre} añadido al carrito",
                                Toast.LENGTH_SHORT
                            ).show()
                            expanded = false
                            daoCarrito.addCarrito(producto, 1)
                        }
                    }
                )

                // Botón añadir a la lista
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Lucide.ListCheck, contentDescription = null)
                    },
                    text = { Text("Añadir a la lista") },
                    onClick = {
                        Toast.makeText(
                            context,
                            "${producto.nombre} añadido a la lista",
                            Toast.LENGTH_SHORT
                        ).show()
                        expanded = false
                    }
                )
            }
        }
    }
}

// Clase Navigation Items para items de la barra lateral
data class NavigationItems(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null
)


// Barra de búsqueda
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarProductos(
    query: String, // Texto escrito por el usuario
    onQueryChange: (String) -> Unit, // Actualiza el texto y filtra sugerencias
    onSearchConfirmed: () -> Unit, // Acción al pulsar buscar (filtrar lista principal)
    sugerencias: List<Producto>, // Lista filtrada para sugerencias
    onProductoClick: (Producto) -> Unit, // Acción al pulsar una sugerencia
    modifier: Modifier = Modifier
) {
    // Controla si el SearchBar está expandido (si muestra sugerencias)
    var expanded by rememberSaveable { mutableStateOf(false) }

    // Contenedor que evita que el menú se expanda hacia arriba
    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics { isTraversalGroup = true }
    ) {

        // Barra de búsqueda
        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 5.dp),

            // Campo de texto del buscador
            inputField = {
                SearchBarDefaults.InputField(
                    query = query, // Texto actual

                    onQueryChange = {
                        onQueryChange(it) // Actualiza texto y filtra sugerencias
                        expanded = true // Abre sugerencias
                    },

                    onSearch = {
                        expanded = false // Cierra sugerencias
                        onSearchConfirmed() // Filtra la lista de productos
                    },

                    expanded = expanded,
                    onExpandedChange = { expanded = it },

                    placeholder = { Text("Buscar productos") },

                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null)
                    }
                )
            },

            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {

            // Sugerencias
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Muestra solo 10 sugerencias
                items(sugerencias.take(10)) { producto ->

                    ListItem(
                        headlineContent = { Text(producto.nombre) },
                        supportingContent = { Text("${producto.precio} €") },
                        modifier = Modifier
                            .fillMaxWidth()
                            // Permite navegar a la pantalla del producto al pulsar una sugerencia
                            .clickable {
                                onProductoClick(producto)
                                expanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}