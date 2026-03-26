@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.persistencia.Pantallas

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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.example.persistencia.Modelos.ProductoCarrito
import com.example.persistencia.Navegacion.AppScreens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Carrito(
    navController: NavController,
    daoCarrito: CarritoDao = CarritoDao(),
    daoProductos: ProductosDao = ProductosDao(),
    daoOfertas: DescuentosDao = DescuentosDao()
) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                    listOf(primaryColor, Color.Transparent),
                    0f,
                    edgeWidth
                ), topLeft = Offset(0f, 0f), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, primaryColor),
                    height - edgeWidth,
                    height
                ), topLeft = Offset(0f, height - edgeWidth), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(secondaryColor, Color.Transparent),
                    0f,
                    edgeWidth
                ), topLeft = Offset(0f, 0f), size = Size(edgeWidth, height)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, secondaryColor),
                    width - edgeWidth,
                    width
                ), topLeft = Offset(width - edgeWidth, 0f), size = Size(edgeWidth, height)
            )
        }

    var carrito by remember { mutableStateOf<List<ProductoCarrito>>(emptyList()) }
    var ofertas by remember { mutableStateOf<List<Descuento>>(emptyList()) }
    var cuponesActivos by remember { mutableStateOf<List<Descuento>>(emptyList()) }

    // Nueva variable de carga
    var cargando by remember { mutableStateOf(true) }

    suspend fun recargarCarrito() {
        val items = daoCarrito.getCarrito()
        carrito = items.mapNotNull { (id, cantidad) ->
            daoProductos.getProducto(id)?.let { ProductoCarrito(it, cantidad) }
        }
    }

    LaunchedEffect(Unit) {
        cargando = true
        ofertas = daoOfertas.getOfertas()
        recargarCarrito()
        val codigosCupones = daoCarrito.getCupones()
        val todosCupones = daoOfertas.getCupones()
        cuponesActivos = todosCupones.filter { it.codigo in codigosCupones }
        delay(200L) // Espera forzada
        cargando = false
    }

    val grupos = carrito.filter { it.producto.oferta != null }.groupBy { it.producto.oferta }
    val total = calcularTotalCarrito(carrito, ofertas, cuponesActivos)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Carrito de la compra",
                        color = colors.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colors.onBackground
                ),
                actions = {
                    Row(modifier = Modifier.padding(end = 15.dp)) {
                        IconButton(
                            modifier = Modifier.size(35.dp),
                            shape = CircleShape,
                            onClick = { navController.navigate(AppScreens.Escaner.route) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = colors.onPrimary.copy(alpha = 0.8f),
                                contentColor = colors.onBackground
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
        Box(modifier = backgroundModifier) {

            if (cargando) {
                // Indicador de carga respetando la estructura del Box
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            } else {
                // Solo mostramos el contenido si no está cargando
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp, 100.dp, 16.dp, 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(carrito) { item ->
                        val oferta = ofertas.find { it.codigo == item.producto.oferta }
                        val cupon = cuponesActivos.find { it.codigo == item.producto.oferta }

                        val descuentoAplicable = oferta ?: cupon

                        val itemsGrupo = carrito.filter { it.producto.oferta == item.producto.oferta }

                        val precioFinal = if (descuentoAplicable != null && itemsGrupo.isNotEmpty()) {
                            calcularPrecioProducto(item, itemsGrupo, descuentoAplicable)
                        } else {
                            item.producto.precio * item.cantidad
                        }



                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    colors.onPrimary.copy(alpha = 0.8f),
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
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.producto.nombre,
                                    color = colors.onBackground,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2
                                )
                                Text(
                                    text = "%.2f €".format(item.producto.precio),
                                    color = colors.onBackground.copy(alpha = 0.8f),
                                    fontSize = 15.sp
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        scope.launch {
                                            if (daoCarrito.addCarrito(
                                                    item.producto,
                                                    -1
                                                )
                                            ) recargarCarrito() else Toast.makeText(
                                                context,
                                                "Error",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }) {
                                        Icon(
                                            Icons.Default.Remove,
                                            "Restar",
                                            tint = colors.onBackground
                                        )
                                    }
                                    Text(
                                        text = item.cantidad.toString(),
                                        color = colors.onBackground,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(onClick = {
                                        scope.launch {
                                            if (daoCarrito.addCarrito(
                                                    item.producto,
                                                    1
                                                )
                                            ) recargarCarrito() else Toast.makeText(
                                                context,
                                                "Error",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }) {
                                        Icon(
                                            Icons.Default.Add,
                                            "Añadir",
                                            tint = colors.onBackground
                                        )
                                    }
                                }
                                val precioOriginal = item.producto.precio * item.cantidad
                                val hayDescuento = precioFinal < precioOriginal
                                Text(
                                    text = "%.2f €".format(precioFinal),
                                    color = if (hayDescuento) MaterialTheme.colorScheme.onError else colors.onBackground,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 6.dp, end = 12.dp)
                                )
                            }
                        }
                    }
                }

                // UI persistente (botones y total) solo tras la carga
                if (carrito.isNotEmpty()) {
                    IconButton(
                        onClick = { navController.navigate(AppScreens.Cupones.route) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = colors.onPrimary.copy(
                                alpha = 0.8f
                            ), contentColor = colors.onSurface
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 200.dp, start = 20.dp)
                    ) {
                        Icon(Lucide.TicketPercent, contentDescription = "Ver cupones")
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(colors.onPrimary)
                            .padding(30.dp, 10.dp, 20.dp, 135.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total: %.2f €".format(total),
                            color = colors.onBackground,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                        Spacer(modifier = Modifier.width(50.dp))
                        Button(
                            onClick = { navController.navigate("pago_stripe/${total.toFloat()}") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Finalizar compra"
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(text = "Finalizar")
                        }
                    }
                }
            }
        }
    }
}