@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.persistencia.Pantallas

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.persistencia.Herramientas.CarritoRepository
import com.example.persistencia.Herramientas.DescuentosRepository
import com.example.persistencia.Herramientas.ProductosRepository
import com.example.persistencia.Herramientas.calcularPrecioProducto
import com.example.persistencia.Herramientas.calcularTotalCarrito
import com.example.persistencia.Herramientas.fondoDegradado
import com.example.persistencia.Modelos.Descuento
import com.example.persistencia.Modelos.ProductoCarrito
import com.example.persistencia.Navegacion.AppScreens
import com.stripe.android.paymentsheet.PaymentSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import com.example.persistencia.Herramientas.CheckoutHelper
import com.example.persistencia.Modelos.DescuentoTicket

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Carrito(
    navController: NavController,
    paymentSheet: PaymentSheet
) {
    val colors = MaterialTheme.colorScheme // Colores del tema actual
    val scope = rememberCoroutineScope() // Para acciones asíncronas
    val context = LocalContext.current // Para toasts

    var carrito by remember { mutableStateOf<List<ProductoCarrito>>(emptyList()) }
    var ofertas by remember { mutableStateOf<List<Descuento>>(emptyList()) }
    var cuponesActivos by remember { mutableStateOf<List<Descuento>>(emptyList()) }

    val backgroundModifier = Modifier.fondoDegradado()

    var cargando by remember { mutableStateOf(true) }

    // Recarga el carrito desde Firestore
    suspend fun recargarCarrito() {
        val items = CarritoRepository.getCarrito()
        carrito = items.mapNotNull { (id, cantidad) ->
            ProductosRepository.getProducto(id)?.let { ProductoCarrito(it, cantidad) }
        }
    }

    // Carga inicial de datos
    LaunchedEffect(Unit) {
        cargando = true
        ofertas = DescuentosRepository.getOfertas()
        recargarCarrito()
        val codigosCupones = CarritoRepository.getCupones()
        val todosCupones = DescuentosRepository.getCupones()
        cuponesActivos = todosCupones.filter { it.codigo in codigosCupones }
        delay(200L)
        cargando = false
    }

    // Cálculo del total del carrito
    val total = calcularTotalCarrito(carrito, ofertas, cuponesActivos)

    var mostrarDescuentos by remember { mutableStateOf(false) }
    var mostrarOpcionesEntrega by remember { mutableStateOf(false) }

    // Calcula los descuentos que han sido aplicados
    val descuentosAplicados = remember(carrito, ofertas, cuponesActivos) {
        if (carrito.isNotEmpty()) {
            val (_, descuentos, _) = CheckoutHelper.calcularTicket(carrito, ofertas, cuponesActivos)
            descuentos
        } else {
            emptyList()
        }
    }

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
                        // Botón para abrir el escáner
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

            // Muestra un indicador mientras carga los datos
            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp, 100.dp, 16.dp, 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(carrito) { item ->
                        // Comprueba si el producto tiene una oferta o cupón
                        val oferta = ofertas.find { it.codigo == item.producto.oferta }
                        val cupon = cuponesActivos.find { it.codigo == item.producto.oferta }

                        val descuentoAplicable = oferta ?: cupon

                        // Agrupa productos por oferta
                        val itemsGrupo =
                            carrito.filter { it.producto.oferta == item.producto.oferta }

                        // Calcula el precio final
                        val precioFinal =
                            if (descuentoAplicable != null && itemsGrupo.isNotEmpty()) {
                                calcularPrecioProducto(item, itemsGrupo, descuentoAplicable)
                            } else {
                                item.producto.precio * item.cantidad
                            }

                        val esProductoAlPeso = item.producto.al_peso == true

                        // Fila que muestra el producto en el carrito
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
                            // Imagen del producto
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
                                // Nombre del producto
                                Text(
                                    text = item.producto.nombre,
                                    color = colors.onBackground,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2
                                )
                                // Precio por unidad (o peso) del producto
                                if (esProductoAlPeso) {
                                    Text(
                                        text = ("%.2f €/" + item.producto.unidad).format(item.producto.precio),
                                        color = colors.onBackground.copy(alpha = 0.8f),
                                        fontSize = 15.sp
                                    )
                                } else {
                                    Text(
                                        text = "%.2f €/ud".format(item.producto.precio),
                                        color = colors.onBackground.copy(alpha = 0.8f),
                                        fontSize = 15.sp
                                    )
                                }
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (esProductoAlPeso) {
                                    // Producto al peso: solo botón eliminar y cantidad
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "%.2f kg".format(item.cantidad),
                                            color = colors.onBackground,
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )

                                        IconButton(onClick = {
                                            scope.launch {
                                                if (CarritoRepository.addCarrito(
                                                        item.producto,
                                                        -item.cantidad
                                                    )
                                                ) recargarCarrito() else Toast.makeText(
                                                    context,
                                                    "Error al eliminar",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                "Eliminar",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                } else {
                                    // Producto normal: controles + y -, cantidad como número entero
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Botón para quitar 1
                                        IconButton(onClick = {
                                            scope.launch {
                                                if (CarritoRepository.addCarrito(
                                                        item.producto,
                                                        -1.0
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
                                        // Cantidad del producto
                                        Text(
                                            text = item.cantidad.toInt().toString(),
                                            color = colors.onBackground,
                                            fontSize = 18.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                        // Botón para añadir 1
                                        IconButton(onClick = {
                                            scope.launch {
                                                if (CarritoRepository.addCarrito(
                                                        item.producto,
                                                        1.0
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
                                }
                                val precioOriginal = item.producto.precio * item.cantidad
                                val hayDescuento = precioFinal < precioOriginal

                                // Precio final del producto(s)
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

                if (carrito.isNotEmpty()) {
                    // Botón para ver los cupones
                    IconButton(
                        onClick = { navController.navigate(AppScreens.Cupones.route) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = colors.onPrimary.copy(alpha = 0.8f),
                            contentColor = colors.onSurface
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
                        // Precio total del carrito con descuentos
                        Text(
                            text = "Total: %.2f €".format(total),
                            color = colors.onBackground,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier.clickable(enabled = descuentosAplicados.isNotEmpty()) {
                                mostrarDescuentos = true
                            }
                        )
                        Spacer(modifier = Modifier.width(50.dp))

                        // Botón para pagar
                        Button(
                            onClick = {
//                                scope.launch {
//                                    val clientSecret = crearPaymentIntent(total)
//
//                                    if (clientSecret != null) {
//                                        paymentSheet.presentWithPaymentIntent(
//                                            clientSecret,
//                                            PaymentSheet.Configuration(
//                                                merchantDisplayName = "Pronto"
//                                            )
//                                        )
//                                    } else {
//                                        Toast.makeText(context, "Error al iniciar pago", Toast.LENGTH_SHORT).show()
//                                    }
//                                }
                                mostrarOpcionesEntrega = true
                            },
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

            if (mostrarOpcionesEntrega) {
                val sheetState = rememberModalBottomSheetState()
                ModalBottomSheet(
                    onDismissRequest = { mostrarOpcionesEntrega = false },
                    sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "¿Cómo quieres recibir tu pedido?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Opción 1: Comprar en supermercado
                        Button(
                            onClick = {
                                mostrarOpcionesEntrega = false
                                scope.launch {
                                    val clientSecret = crearPaymentIntent(total)
                                    if (clientSecret != null) {
                                        paymentSheet.presentWithPaymentIntent(
                                            clientSecret,
                                            PaymentSheet.Configuration(merchantDisplayName = "Pronto")
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error al iniciar pago",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Comprar en supermercado")
                        }

                        // Opción 2: Envío a domicilio
                        Button(
                            onClick = {
                                mostrarOpcionesEntrega = false
                                scope.launch {
                                    val clientSecret = crearPaymentIntent(total)
                                    if (clientSecret != null) {
                                        paymentSheet.presentWithPaymentIntent(
                                            clientSecret,
                                            PaymentSheet.Configuration(merchantDisplayName = "Pronto")
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error al iniciar pago",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Envío a domicilio")
                        }

                        // Opción 3: Recoger en tienda
                        Button(
                            onClick = {
                                mostrarOpcionesEntrega = false
                                scope.launch {
                                    val clientSecret = crearPaymentIntent(total)
                                    if (clientSecret != null) {
                                        paymentSheet.presentWithPaymentIntent(
                                            clientSecret,
                                            PaymentSheet.Configuration(merchantDisplayName = "Pronto")
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error al iniciar pago",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recoger en tienda")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Muestra los descuentos que han sido aplicados y el ahorro
            if (mostrarDescuentos) {
                val sheetState = rememberModalBottomSheetState()
                ModalBottomSheet(
                    onDismissRequest = { mostrarDescuentos = false },
                    sheetState = sheetState
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Descuentos aplicados",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (descuentosAplicados.isEmpty()) {
                            Text(
                                "No hay descuentos aplicados",
                                color = colors.onBackground.copy(alpha = 0.6f)
                            )
                        } else {
                            descuentosAplicados.forEach { descuento ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = descuento.nombre ?: descuento.codigo,
                                        fontSize = 16.sp,
                                        color = colors.onBackground,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "-%.2f €".format(descuento.descuentoAplicado),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                HorizontalDivider(color = colors.onBackground.copy(alpha = 0.1f))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val totalAhorro = descuentosAplicados.sumOf { it.descuentoAplicado }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total ahorrado",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onBackground
                                )
                                Text(
                                    "-%.2f €".format(totalAhorro),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// Crear PaymentIntent en Stripe mediante backend
suspend fun crearPaymentIntent(total: Double): String? {
    return try {
        withContext(kotlinx.coroutines.Dispatchers.IO) {

            val client = OkHttpClient()

            val json = JSONObject()
            json.put("amount", (total * 100).toInt())

            val body = json.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://stripebackend-zg26.onrender.com/create-payment-intent") // URL del endpoint en Render
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            JSONObject(responseBody!!).getString("clientSecret") // Clave secreta de Stripe
        }

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}