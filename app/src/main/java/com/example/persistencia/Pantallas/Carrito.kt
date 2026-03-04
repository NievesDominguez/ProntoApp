@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.persistencia.Pantallas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.persistencia.Firestore.CarritoDao
import com.example.persistencia.Firestore.DescuentosDao
import com.example.persistencia.Firestore.ProductosDao
import com.example.persistencia.Modelos.Descuento
import com.example.persistencia.Modelos.ProductoCarrito
import kotlinx.coroutines.launch

// -------------------------
// FUNCIONES DE OFERTAS
// -------------------------

// Segunda unidad combinable
fun aplicarSegundaUnidadCombinable(
    items: List<ProductoCarrito>,
    oferta: Descuento
): Double {

    val descuento = ((oferta.formula?.get("descuento") ?: 0) as Number).toDouble()

    // Expandir unidades
    val unidades = items.flatMap { item ->
        List(item.cantidad) { item.producto.precio }
    }.sorted() // de menor a mayor

    var total = 0.0
    var index = 0

    while (index < unidades.size) {
        if (index + 1 < unidades.size) {
            // Hay pareja → aplicar descuento a la más barata (unidades[index])
            val barato = unidades[index]
            val caro = unidades[index + 1]

            total += barato * (1 - descuento / 100)
            total += caro
            index += 2
        } else {
            // Unidad suelta → sin descuento
            total += unidades[index]
            index += 1
        }
    }

    return total
}



// n por m combinable (3x2, 4x3…)
fun aplicarNxMCombinable(
    items: List<ProductoCarrito>,
    oferta: Descuento
): Double {
    val n = ((oferta.formula?.get("n") ?: 0) as Number).toInt()
    val m = ((oferta.formula?.get("m") ?: 0) as Number).toInt()

    val unidades = items.flatMap { item ->
        List(item.cantidad) { item.producto.precio }
    }.sorted()

    var total = 0.0
    var index = 0

    while (index < unidades.size) {
        val grupo = unidades.drop(index).take(n)

        total += if (grupo.size == n) {
            grupo.take(m).sum()
        } else grupo.sum()

        index += n
    }

    return total
}

// Precio total de un grupo de productos con la misma oferta
fun calcularTotalGrupo(
    items: List<ProductoCarrito>,
    oferta: Descuento
): Double {
    return when (oferta.formula?.get("tipo")) {
        "segunda_unidad" -> aplicarSegundaUnidadCombinable(items, oferta)
        "n_por_m" -> aplicarNxMCombinable(items, oferta)
        else -> items.sumOf { it.producto.precio * it.cantidad }
    }
}


// -------------------------
// PANTALLA CARRITO
// -------------------------

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

    suspend fun recargarCarrito() {
        val items = daoCarrito.getCarrito()
        carrito = items.mapNotNull { (id, cantidad) ->
            daoProductos.getProducto(id)?.let { ProductoCarrito(it, cantidad) }
        }
    }

    LaunchedEffect(Unit) {
        ofertas = daoOfertas.getDescuentos()
        recargarCarrito()
    }

    // Agrupar productos por oferta
    val grupos = carrito
        .filter { it.producto.oferta != null }
        .groupBy { it.producto.oferta }

    // Total del carrito
    val total = calcularTotalCarrito(carrito, ofertas)


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {

        Column {
            Text(
                modifier = Modifier.padding(24.dp, 50.dp, 24.dp, 24.dp),
                text = "Carrito de la compra",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 100.dp, 16.dp, 100.dp),
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
                        .background(Color(0xFFFFFFFF).copy(alpha = 0.15f), RoundedCornerShape(16.dp))
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
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Nombre + precio por unidad
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
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

                    // Cantidad + precio final
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    ) {

                        // Controles de cantidad
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        if (daoCarrito.addCarrito(item.producto, -1)) recargarCarrito()
                                        else Toast.makeText(context, "No se ha podido modificar el carrito", Toast.LENGTH_SHORT).show()
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
                                        if (daoCarrito.addCarrito(item.producto, 1)) recargarCarrito()
                                        else Toast.makeText(context, "No se ha podido modificar el carrito", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Color.White)
                            }
                        }

                        // Precio total (en rojo si hay descuento)
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

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.20f))
                .padding(20.dp, 20.dp, 20.dp, 150.dp)
        ) {
            Text(
                text = "Total: %.2f €".format(total),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


fun calcularPreciosUnitariosSegundaUnidad(
    items: List<ProductoCarrito>,
    oferta: Descuento
): List<Double> {

    val descuento = ((oferta.formula?.get("descuento") ?: 0) as Number).toDouble()

    data class Unidad(val producto: com.example.persistencia.Modelos.Producto, val index: Int)

    // Expandir unidades con índice original
    val unidades = mutableListOf<Unidad>()
    var idx = 0
    for (item in items) {
        repeat(item.cantidad) {
            unidades.add(Unidad(item.producto, idx))
            idx++
        }
    }

    // Orden global por precio (mezclando productos)
    val ordenadas = unidades.sortedBy { u -> u.producto.precio }

    // Número de unidades que deben llevar descuento
    val numDescuentos = ordenadas.size / 2

    // Marcar descuento en las numDescuentos unidades más baratas
    val descuentoAplicado = BooleanArray(unidades.size)
    for (i in 0 until numDescuentos) {
        val unidadConDescuento = ordenadas[i]
        descuentoAplicado[unidadConDescuento.index] = true
    }

    // Calcular precios finales en orden original
    val preciosFinales = MutableList(unidades.size) { 0.0 }
    for ((pos, unidad) in unidades.withIndex()) {
        val base = unidad.producto.precio
        preciosFinales[pos] =
            if (descuentoAplicado[pos]) base * (1 - descuento / 100)
            else base
    }

    return preciosFinales
}


fun calcularPreciosUnitariosNxM(
    items: List<ProductoCarrito>,
    oferta: Descuento
): List<Double> {

    val n = ((oferta.formula?.get("n") ?: 0) as Number).toInt()
    val m = ((oferta.formula?.get("m") ?: 0) as Number).toInt()

    data class Unidad(val producto: com.example.persistencia.Modelos.Producto, val index: Int)

    val unidades = mutableListOf<Unidad>()
    var idx = 0
    for (item in items) {
        repeat(item.cantidad) {
            unidades.add(Unidad(item.producto, idx))
            idx++
        }
    }

    val ordenadas = unidades.sortedBy { u -> u.producto.precio }

    val paga = BooleanArray(unidades.size)
    var i = 0
    while (i < ordenadas.size) {
        val grupo = ordenadas.drop(i).take(n)
        // En cada grupo, las m más baratas se pagan
        val grupoOrdenado = grupo.sortedBy { u -> u.producto.precio }
        grupoOrdenado.take(m).forEach { u ->
            paga[u.index] = true
        }
        i += n
    }

    val preciosFinales = MutableList(unidades.size) { 0.0 }
    for ((pos, unidad) in unidades.withIndex()) {
        preciosFinales[pos] = if (paga[pos]) unidad.producto.precio else 0.0
    }

    return preciosFinales
}


fun calcularPrecioProducto(
    item: ProductoCarrito,
    itemsGrupo: List<ProductoCarrito>,
    oferta: Descuento
): Double {

    val preciosUnitarios: List<Double> = when (oferta.formula?.get("tipo")) {
        "segunda_unidad" -> calcularPreciosUnitariosSegundaUnidad(itemsGrupo, oferta)
        "n_por_m" -> calcularPreciosUnitariosNxM(itemsGrupo, oferta)
        else -> {
            val lista = mutableListOf<Double>()
            for (ig in itemsGrupo) {
                repeat(ig.cantidad) {
                    lista.add(ig.producto.precio)
                }
            }
            lista
        }
    }

    // Expandir unidades del grupo en el mismo orden que se generaron arriba
    val unidades = mutableListOf<com.example.persistencia.Modelos.Producto>()
    for (ig in itemsGrupo) {
        repeat(ig.cantidad) {
            unidades.add(ig.producto)
        }
    }

    var total = 0.0
    for (i in unidades.indices) {
        val prod = unidades[i]
        if (prod.id == item.producto.id) {
            total += preciosUnitarios[i]
        }
    }

    return total
}


fun calcularTotalCarrito(
    carrito: List<ProductoCarrito>,
    ofertas: List<Descuento>
): Double {

    var total = 0.0

    // Agrupar productos por oferta
    val grupos = carrito
        .filter { item -> item.producto.oferta != null }
        .groupBy { item -> item.producto.oferta }

    // Procesar grupos con oferta
    for ((codigoOferta, itemsGrupo) in grupos) {
        val oferta = ofertas.find { o -> o.codigo == codigoOferta } ?: continue

        // Obtener precios unitarios reales del grupo
        val preciosUnitarios: List<Double> = when (oferta.formula?.get("tipo")) {
            "segunda_unidad" -> calcularPreciosUnitariosSegundaUnidad(itemsGrupo, oferta)
            "n_por_m" -> calcularPreciosUnitariosNxM(itemsGrupo, oferta)
            else -> {
                val lista = mutableListOf<Double>()
                for (ig in itemsGrupo) {
                    repeat(ig.cantidad) {
                        lista.add(ig.producto.precio)
                    }
                }
                lista
            }
        }

        // Expandir unidades del grupo
        val unidades = mutableListOf<com.example.persistencia.Modelos.Producto>()
        for (ig in itemsGrupo) {
            repeat(ig.cantidad) {
                unidades.add(ig.producto)
            }
        }

        // Sumar precios reales del grupo
        for (i in unidades.indices) {
            total += preciosUnitarios[i]
        }
    }

    // Productos sin oferta
    val sinOferta = carrito.filter { item -> item.producto.oferta == null }
    total += sinOferta.sumOf { item -> item.producto.precio * item.cantidad }

    return total
}

