package com.example.persistencia.Pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.example.persistencia.Firestore.CarritoDao
import com.example.persistencia.Firestore.ProductosDao
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Modelos.ProductoCarrito
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Carrito(
    navController: NavController,
    daoCarrito: CarritoDao = CarritoDao(),
    daoProductos: ProductosDao = ProductosDao()
) {
    // Degradado magenta a morado
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFD13CF2),
            Color(0xFF6C3AEC)
        )
    )

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var carrito by remember { mutableStateOf<List<ProductoCarrito>>(emptyList()) }

    // Función para recargar el carrito
    suspend fun recargarCarrito() {
        val items = daoCarrito.getCarrito() // List<Pair<idProducto, cantidad>>

        carrito = items.mapNotNull { (id, cantidad) ->
            val producto = daoProductos.getProducto(id)
            if (producto != null) ProductoCarrito(producto, cantidad) else null
        }
    }

    // Cargar carrito al entrar
    LaunchedEffect(Unit) {
        recargarCarrito()
    }

    // Fondo degradado de la pantalla
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = "Carrito de la compra",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(carrito) { item ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Imagen del producto
                    AsyncImage(
                        model = item.producto.imagenUrl,
                        contentDescription = item.producto.nombre,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Nombre y precio
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(item.producto.nombre, color = Color.White, fontSize = 18.sp)
                        Text(
                            "${item.producto.precio} €",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }

                    // Botones + y -
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        IconButton(onClick = {
                            scope.launch {
                                val ok = daoCarrito.addCarrito(item.producto, -1)
                                if (ok) recargarCarrito()
                            }
                        }) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Restar",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = item.cantidad.toString(),
                            color = Color.White,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(onClick = {
                            scope.launch {
                                val ok = daoCarrito.addCarrito(item.producto, +1)
                                if (ok) recargarCarrito()
                            }
                        }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Añadir",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

}

