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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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

    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current

    // Fondo con bordes degradados
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
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor, Color.Transparent),
                    startY = 0f, endY = edgeWidth
                ),
                topLeft = Offset(0f, 0f), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, primaryColor),
                    startY = height - edgeWidth, endY = height
                ),
                topLeft = Offset(0f, height - edgeWidth), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(secondaryColor, Color.Transparent),
                    startX = 0f, endX = edgeWidth
                ),
                topLeft = Offset(0f, 0f), size = Size(edgeWidth, height)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, secondaryColor),
                    startX = width - edgeWidth, endX = width
                ),
                topLeft = Offset(width - edgeWidth, 0f), size = Size(edgeWidth, height)
            )
        }

    // Lista de items para la barra lateral de navegación
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

    val dao = ProductosDao()
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }

    LaunchedEffect(Unit) {
        productos = dao.getTodos()
    }

    var query by remember { mutableStateOf("") }

    productos.filter { producto ->
        producto.nombre.contains(query, ignoreCase = true)
    }

    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Catálogo",
                        color = colors.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colors.onBackground
                ),
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
                            tint = colors.onBackground
                        )
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = backgroundModifier
                .padding(top = 70.dp)
        ) {

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    Box(modifier = Modifier.width(220.dp)) {
                        ModalDrawerSheet(
                            drawerContainerColor = colors.surface
                        ) {
                            Spacer(modifier = Modifier.height(60.dp))
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
                                            categoriaSeleccionada = item.title
                                            scope.launch {
                                                drawerState.close()
                                                productos = dao.getPorCategoria(item.title)
                                            }
                                        }
                                        scope.launch {
                                            drawerState.close()
                                            productos = dao.getPorCategoria(item.title)
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (index == selectedItemIndex) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title,
                                            tint = if (index == selectedItemIndex) colors.primary else colors.onSurface
                                        )
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedIconColor = colors.primary,
                                        unselectedIconColor = colors.onSurface,
                                        selectedTextColor = colors.primary,
                                        unselectedTextColor = colors.onSurface,
                                        selectedContainerColor = colors.primary.copy(alpha = 0.15f),
                                        unselectedContainerColor = Color.Transparent
                                    ),
                                    badge = {
                                        item.badgeCount?.let {
                                            Text(text = item.badgeCount.toString())
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                            }
                        }
                    }
                },
                gesturesEnabled = true
            ) {
                Box {

                    // BARRA DE BÚSQUEDA
                    var query by remember { mutableStateOf("") }
                    var productosFiltrados by remember { mutableStateOf(emptyList<Producto>()) }

                    val sugerencias = remember(query, productos) {
                        if (query.isBlank()) emptyList()
                        else productos.filter { it.nombre.contains(query, ignoreCase = true) }
                    }

                    LaunchedEffect(productos) {
                        productosFiltrados = productos
                    }

                    SearchBarProductos(
                        query = query,
                        onQueryChange = { query = it },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .alpha(0.95f),
                        onSearchConfirmed = {
                            productosFiltrados =
                                if (query.isBlank()) productos
                                else productos.filter {
                                    it.nombre.contains(query, ignoreCase = true)
                                }
                        },
                        sugerencias = sugerencias,
                        onProductoClick = { producto ->
                            navController.navigate(AppScreens.PantallaProducto.route + "/${producto.id}")
                        }
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(top = 70.dp)
                    ) {
                        item {
                            categoriaSeleccionada?.let {
                                Text(
                                    modifier = Modifier.padding(24.dp),
                                    text = it,
                                    color = colors.onBackground,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        items(productosFiltrados.chunked(2)) { fila ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                fila.forEach { producto ->
                                    TarjetaProducto(
                                        producto,
                                        modifier = Modifier.weight(1f),
                                        navController
                                    )
                                }
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
                                    color = colors.onBackground,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

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
    val colors = MaterialTheme.colorScheme
    val daoCarrito = CarritoDao()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.height(190.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.onPrimary.copy(alpha = 0.8f)
            ),
            onClick = {
                navController.navigate(AppScreens.PantallaProducto.route + "/${producto.id}")
            }
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                AsyncImage(
                    model = producto.imagenUrl,
                    contentDescription = producto.nombre,
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = producto.nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.onSurface
                )

                Text(
                    text = "%.2f €".format(producto.precio),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    tint = colors.onSurface // antes por defecto negro
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = colors.surface // fondo del menú
            ) {
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

data class NavigationItems(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarProductos(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchConfirmed: () -> Unit,
    sugerencias: List<Producto>,
    onProductoClick: (Producto) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    var expanded by rememberSaveable { mutableStateOf(false) }

    DockedSearchBar(
        modifier = modifier,
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = {
                    onQueryChange(it)
                    expanded = it.isNotEmpty()
                },
                onSearch = {
                    expanded = false
                    onSearchConfirmed()
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text("Buscar productos") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) }
            )
        },
        expanded = expanded,
        onExpandedChange = { expanded = it },
        colors = SearchBarDefaults.colors(
            containerColor = colors.onPrimary.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .heightIn(max = 300.dp)
        ) {
            LazyColumn(
                modifier = Modifier.wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(sugerencias.take(10)) { producto ->
                    ListItem(
                        headlineContent = { Text(producto.nombre, color = colors.onSurface) },
                        supportingContent = { Text("${producto.precio} €", color = colors.onSurface) },
                        modifier = Modifier.clickable {
                            onProductoClick(producto)
                            expanded = false
                        }
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = colors.outline
                    )
                }
            }
        }
    }
}