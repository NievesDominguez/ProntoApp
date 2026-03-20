@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.persistencia.Pantallas

import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TicketPercent
import com.example.persistencia.Firestore.CarritoDao
import com.example.persistencia.Firestore.DescuentosDao
import com.example.persistencia.Firestore.ProductosDao
import com.example.persistencia.Herramientas.calcularPrecioProducto
import com.example.persistencia.Herramientas.calcularTotalCarrito
import com.example.persistencia.Modelos.Descuento
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Modelos.ProductoCarrito
import com.example.persistencia.Navegacion.AppScreens
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun Carrito(
    navController: NavController,
    daoCarrito: CarritoDao = CarritoDao(),
    daoProductos: ProductosDao = ProductosDao(),
    daoOfertas: DescuentosDao = DescuentosDao()
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFD13CF2), Color(0xFF6C3AEC))
    )

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var carrito by remember { mutableStateOf<List<ProductoCarrito>>(emptyList()) }
    var ofertas by remember { mutableStateOf<List<Descuento>>(emptyList()) }
    var cuponesActivos by remember { mutableStateOf<List<Descuento>>(emptyList()) }

    suspend fun recargarCarrito() {
        val items = daoCarrito.getCarrito()
        carrito = items.mapNotNull { (id, cantidad) ->
            daoProductos.getProducto(id)?.let { ProductoCarrito(it, cantidad) }
        }
    }

    // Cargar carrito, ofertas y cupones activos
    LaunchedEffect(Unit) {
        ofertas = daoOfertas.getDescuentos()
        recargarCarrito()

        val codigosCupones = daoCarrito.getCuponesActivos()
        val todosCupones = daoOfertas.getCupones()
        cuponesActivos = todosCupones.filter { it.codigo in codigosCupones }
    }

    // Agrupar productos por oferta
    val grupos = carrito
        .filter { it.producto.oferta != null }
        .groupBy { it.producto.oferta }

    // TOTAL FINAL (ofertas + cupones)
    val total = calcularTotalCarrito(carrito, ofertas, cuponesActivos)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Carrito de la compra",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    Row(modifier = Modifier.padding(end = 15.dp)) {
                        IconButton(
                            modifier = Modifier.size(35.dp),
                            shape = CircleShape,
                            onClick = { navController.navigate(AppScreens.Escaner.route) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.White.copy(alpha = 0.25f),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Escanear")
                        }
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp, 100.dp, 16.dp, 200.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(carrito) { item ->

                    val oferta = ofertas.find { it.codigo == item.producto.oferta }
                    val itemsGrupo = grupos[item.producto.oferta]

                    val precioFinal = if (oferta != null && itemsGrupo != null) {
                        calcularPrecioProducto(item, itemsGrupo, oferta)
                    } else item.producto.precio * item.cantidad

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        AsyncImage(
                            model = item.producto.imagenUrl,
                            contentDescription = item.producto.nombre,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.producto.nombre,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2
                            )
                            Text(
                                text = "%.2f €".format(item.producto.precio),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 15.sp
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f)
                        ) {

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            if (daoCarrito.addCarrito(item.producto, -1))
                                                recargarCarrito()
                                            else Toast.makeText(
                                                context,
                                                "No se ha podido modificar el carrito",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Restar", tint = Color.White)
                                }

                                Text(
                                    text = item.cantidad.toString(),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            if (daoCarrito.addCarrito(item.producto, 1))
                                                recargarCarrito()
                                            else Toast.makeText(
                                                context,
                                                "No se ha podido modificar el carrito",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Color.White)
                                }
                            }

                            val precioOriginal = item.producto.precio * item.cantidad
                            val hayDescuento = precioFinal < precioOriginal

                            Text(
                                text = "%.2f €".format(precioFinal),
                                color = if (hayDescuento) Color(0xFFFF0000) else Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp, end = 12.dp),
                            )
                        }
                    }
                }
            }

            // Botón cupones
            IconButton(
                onClick = { navController.navigate(AppScreens.Cupones.route) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF6C3AEC)
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 200.dp, start = 20.dp)
            ) {
                Icon(Lucide.TicketPercent, contentDescription = "Ver cupones")
            }

            // Total final
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.20f))
                    .padding(30.dp, 10.dp, 20.dp, 135.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Total: %.2f €".format(total),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )

                Spacer(modifier = Modifier.width(50.dp))

                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF6C3AEC),
                    )
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Finalizar compra")
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Finalizar")
                }
            }
        }
    }
}

