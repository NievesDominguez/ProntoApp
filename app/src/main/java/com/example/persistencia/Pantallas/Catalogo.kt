package com.example.persistencia.Pantallas

import android.R.attr.query
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import com.composables.icons.lucide.Shirt
import com.composables.icons.lucide.Store
import com.example.persistencia.Firestore.CatalogoVistaModelo
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Navegacion.AppScreens
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Catalogo(navController: NavController, vistaModelo: CatalogoVistaModelo = viewModel()) {

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

    // Lista fija de imagenes que se muestran en el carousel
    val carouselItems = remember {
        listOf(
            CarouselItem(
                0,
                "https://storage.googleapis.com/media.bckts.are.external.alcampo.es/PROMOCIONES%20EXCLUSIVAS%20ONLINE/2026/OFERTAS%20EXCLUSIVAS%20%28Folleto%2003%29/Frescos.jpg",
                "Oferta 1"
            ),
            CarouselItem(
                1,
                "https://storage.googleapis.com/media.bckts.are.external.alcampo.es/PROMOCIONES%20EXCLUSIVAS%20ONLINE/2026/OFERTAS%20EXCLUSIVAS%20%28Folleto%2003%29/DesayunoMerienda.jpg",
                "Oferta 2"
            ),
            CarouselItem(
                2,
                "https://storage.googleapis.com/media.bckts.are.external.alcampo.es/PROMOCIONES%20EXCLUSIVAS%20ONLINE/2026/OFERTAS%20EXCLUSIVAS%20%28Folleto%2003%29/Lacteos.jpg",
                "Oferta 3"
            ),
            CarouselItem(
                3,
                "https://storage.googleapis.com/media.bckts.are.external.alcampo.es/PROMOCIONES%20EXCLUSIVAS%20ONLINE/2026/OFERTAS%20EXCLUSIVAS%20%28Folleto%2003%29/ComidaPreparada.jpg",
                "Oferta 4"
            ),
            CarouselItem(
                4,
                "https://storage.googleapis.com/media.bckts.are.external.alcampo.es/PROMOCIONES%20EXCLUSIVAS%20ONLINE/2026/OFERTAS%20EXCLUSIVAS%20%28Folleto%2003%29/SinGluten.jpg",
                "Oferta 5"
            )
        )
    }

    //Lista de items para la barra lateral de navegación
    val categorias = listOf(
        NavigationItems(
            title = "Todo",
            selectedIcon = Lucide.Store,
            unselectedIcon = Lucide.Store
        ),
        NavigationItems(
            title = "Alimentación",
            selectedIcon = Icons.Filled.Restaurant,
            unselectedIcon = Icons.Outlined.Restaurant
        ),
        NavigationItems(
            title = "Textil",
            selectedIcon = Lucide.Shirt,
            unselectedIcon = Lucide.Shirt,
            badgeCount = 105
        ),
        NavigationItems(
            title = "Electrónica",
            selectedIcon = Lucide.Plug,
            unselectedIcon = Lucide.Plug
        )
    )

    // Lista de productos del ViewModel
    val productos by vistaModelo.productos.collectAsState()
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    // Filtrado local de productos según la búsqueda
    val productosFiltrados by remember(query, productos) {
        mutableStateOf(productos.filter { producto ->
            producto.nombre.contains(query, ignoreCase = true)
        })
    }

    val context = LocalContext.current // Para acceder al sistema

    // Para el panel lateral de navegación
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }


    // Fondo degradado de la pantalla
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient2)
    ) {


        // TopBar transparente
        TopAppBar(
            modifier = Modifier.height(56.dp),
            title = { Text("Catálogo") },
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
                    Icon(Icons.Default.Menu, contentDescription = "Barra lateral")
                }
            }
        )

        // Barra horizontal de navegación, permite navegar por categorías
        // El Drawer envuelve la pantalla principal, pero no el topbar
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                // Lo metemos dentro de un box para modificar el tamaño
                Box(modifier = Modifier.width(220.dp)) {
                    ModalDrawerSheet {
                        Spacer(modifier = Modifier.height(16.dp)) //space (margin) from top
                        categorias.forEachIndexed { index, item ->
                            NavigationDrawerItem(
                                label = { Text(text = item.title) },
                                selected = index == selectedItemIndex,
                                onClick = {
                                    //  navController.navigate(item.route)

                                    selectedItemIndex = index
                                    scope.launch {
                                        drawerState.close()
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

                // Barra de búsqueda
                var query by remember { mutableStateOf("") }

                SearchBarProductos(
                    query = query,
                    onQueryChange = { query = it },
                    productosFiltrados = productosFiltrados,
                    onProductoClick = { producto ->
                        navController.navigate(AppScreens.PantallaProducto.route + "/${producto.id}")
                    }
                )


                // Con un LazyColumn único se puede hacer scroll vertical de toda la pantalla
                // No usar Column infinito y dentro un LazyColumn
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {

                    // Novedades
                    item {
                        Text(
                            modifier = Modifier.padding(24.dp, 30.dp, 24.dp, 24.dp),
                            text = "Novedades",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Carrusel
                    item {
                        HorizontalUncontainedCarousel(
                            state = rememberCarouselState { carouselItems.count() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(top = 16.dp, bottom = 16.dp),
                            itemWidth = 186.dp,
                            itemSpacing = 8.dp,
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) { i ->
                            val item = carouselItems[i]
                            AsyncImage(
                                model = item.imgLink,
                                contentDescription = item.contentDescription,
                                modifier = Modifier
                                    .height(205.dp)
                                    .maskClip(MaterialTheme.shapes.extraLarge),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }


                    // Todos los productos
                    item {
                        Text(
                            modifier = Modifier.padding(24.dp),
                            text = "Todos los productos",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
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

                    // Espacio final para que el ultimo elemento no quede pegado al borde
                    item {
                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        }

    }

}


@Composable
fun TarjetaProducto(
    producto: Producto,
    modifier: Modifier = Modifier,
    navController: NavController
) {
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
                containerColor = Color.White.copy(alpha = 0.9f)
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
                    text = "${producto.precio} €",
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
                        Toast.makeText(
                            context,
                            "${producto.nombre} añadido al carrito",
                            Toast.LENGTH_SHORT
                        ).show()
                        expanded = false
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


class CatalogoVistaModelo : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos

    init {
        cargarProductos()
    }

    private fun cargarProductos() {
        firestore.collection("productos")
            .get()
            .addOnSuccessListener { snapshot ->
                _productos.value = snapshot.toObjects(Producto::class.java)
            }
            .addOnFailureListener { e ->
                Log.e("CatalogoVM", "Error cargando productos", e)
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarProductos(
    query: String, // Búsqueda del usuario
    onQueryChange: (String) -> Unit, // Comportamiento cuando cambia el texto
    productosFiltrados: List<Producto>, // Lista filtrada según la búsqueda
    onProductoClick: (Producto) -> Unit, // Acción al pulsar un producto
    modifier: Modifier = Modifier
) {
    // Controla si el SearchBar está expandido (si muestra resultados)
    var expanded by rememberSaveable { mutableStateOf(false) }

    // IMPORTANTE: este Box NO usa fillMaxSize()
    // Así evitamos que el menú se expanda hacia arriba.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics { isTraversalGroup = true } // Mejora accesibilidad
    ) {

        // Barra de búsqueda
        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 5.dp)
                .semantics { traversalIndex = 0f }, // Orden de navegación accesible

            // Campo de texto del buscador (nuevo API no deprecado)
            inputField = {
                SearchBarDefaults.InputField(
                    query = query, // Texto actual de búsqueda

                    onQueryChange = {
                        onQueryChange(it) // Actualiza el texto
                        expanded = true // Abre el menú al escribir
                    },

                    onSearch = {
                        expanded = false // Cierra al pulsar buscar
                    },

                    expanded = expanded, // Estado del menú abierto o cerrado
                    onExpandedChange = { expanded = it },

                    // Placeholder dentro del campo
                    placeholder = { Text("Buscar productos") },

                    // Icono de lupa
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null)
                    }
                )
            },

            expanded = expanded, // Controla si se muestran resultados
            onExpandedChange = { expanded = it }
        ) {

            // Lista de resultados de la búsqueda
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp) // Limita la altura → evita expandirse hacia arriba
            ) {
                items(productosFiltrados.take(10)) { producto ->

                    // Cada resultado de búsqueda
                    ListItem(
                        headlineContent = { Text(producto.nombre) }, // Nombre del producto
                        supportingContent = { Text("${producto.precio} €") }, // Precio
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onProductoClick(producto) // Acción al pulsar
                                expanded = false // Cierra el menú
                            }
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}




