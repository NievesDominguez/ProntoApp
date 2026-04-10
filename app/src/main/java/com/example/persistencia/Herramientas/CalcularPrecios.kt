package com.example.persistencia.Herramientas

import com.example.persistencia.Modelos.Descuento
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Modelos.ProductoCarrito

// Segunda unidad combinable
fun aplicarSegundaUnidadCombinable(
    items: List<ProductoCarrito>,
    oferta: Descuento
): Double {

    val descuento = ((oferta.formula?.get("descuento") ?: 0) as Number).toDouble()

    // Expandir unidades (ahora cantidad es Double, usamos toInt() para productos por unidad)
    val unidades = items.flatMap { item ->
        List(item.cantidad.toInt()) { item.producto.precio }
    }.sorted()

    var total = 0.0
    var index = 0

    while (index < unidades.size) {
        if (index + 1 < unidades.size) {
            val barato = unidades[index]
            val caro = unidades[index + 1]
            total += barato * (1 - descuento / 100)
            total += caro
            index += 2
        } else {
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
        List(item.cantidad.toInt()) { item.producto.precio }
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

fun calcularPreciosUnitariosSegundaUnidad(
    items: List<ProductoCarrito>,
    oferta: Descuento
): List<Double> {

    val descuento = ((oferta.formula?.get("descuento") ?: 0) as Number).toDouble()

    data class Unidad(val producto: Producto, val index: Int)

    val unidades = mutableListOf<Unidad>()
    var idx = 0
    for (item in items) {
        repeat(item.cantidad.toInt()) {
            unidades.add(Unidad(item.producto, idx))
            idx++
        }
    }

    val ordenadas = unidades.sortedBy { u -> u.producto.precio }
    val numDescuentos = ordenadas.size / 2

    val descuentoAplicado = BooleanArray(unidades.size)
    for (i in 0 until numDescuentos) {
        val unidadConDescuento = ordenadas[i]
        descuentoAplicado[unidadConDescuento.index] = true
    }

    val preciosFinales = MutableList(unidades.size) { 0.0 }
    for ((pos, unidad) in unidades.withIndex()) {
        val base = unidad.producto.precio
        preciosFinales[pos] = if (descuentoAplicado[pos]) base * (1 - descuento / 100) else base
    }

    return preciosFinales
}

fun calcularPreciosUnitariosNxM(
    items: List<ProductoCarrito>,
    oferta: Descuento
): List<Double> {

    val n = ((oferta.formula?.get("n") ?: 0) as Number).toInt()
    val m = ((oferta.formula?.get("m") ?: 0) as Number).toInt()

    data class Unidad(val producto: Producto, val index: Int)

    val unidades = mutableListOf<Unidad>()
    var idx = 0
    for (item in items) {
        repeat(item.cantidad.toInt()) {
            unidades.add(Unidad(item.producto, idx))
            idx++
        }
    }

    val ordenadas = unidades.sortedBy { it.producto.precio }
    val total = unidades.size
    val gratis = total / n * (n - m)

    val paga = BooleanArray(unidades.size) { true }
    for (i in 0 until gratis) {
        paga[ordenadas[i].index] = false
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
                repeat(ig.cantidad.toInt()) {
                    lista.add(ig.producto.precio)
                }
            }
            lista
        }
    }

    val unidades = mutableListOf<Producto>()
    for (ig in itemsGrupo) {
        repeat(ig.cantidad.toInt()) {
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
    ofertas: List<Descuento>,
    cupones: List<Descuento> = emptyList()
): Double {

    var total = 0.0

    val descuentosProducto = (ofertas + cupones).filter {
        val tipo = (it.formula?.get("tipo") as? String) ?: it.tipo
        tipo == "segunda_unidad" || tipo == "n_por_m"
    }

    val grupos = carrito
        .filter { it.producto.oferta != null }
        .groupBy { it.producto.oferta }

    for ((codigo, itemsGrupo) in grupos) {
        val descuento = descuentosProducto.find { it.codigo == codigo }
        if (descuento != null) {
            total += calcularTotalGrupo(itemsGrupo, descuento)
        } else {
            total += itemsGrupo.sumOf { it.producto.precio * it.cantidad }
        }
    }

    val sinDescuento = carrito.filter { it.producto.oferta == null }
    total += sinDescuento.sumOf { it.producto.precio * it.cantidad }

    for (cupon in cupones) {
        val tipo = (cupon.formula?.get("tipo") as? String) ?: cupon.tipo

        when (tipo) {
            "fijo" -> {
                val minimo = ((cupon.formula?.get("minimo") ?: 0) as Number).toDouble()
                val valor = ((cupon.formula?.get("valor") ?: 0) as Number).toDouble()
                if (total >= minimo) total -= valor
            }
            "porcentaje" -> {
                val minimo = ((cupon.formula?.get("minimo") ?: 0) as Number).toDouble()
                val porcentaje = ((cupon.formula?.get("valor") ?: 0) as Number).toDouble()
                if (total >= minimo) total *= (1 - porcentaje / 100.0)
            }
            "maximo" -> {
                val max = cupon.max_descuento ?: total
                if (total > max) total = max
            }
        }
    }

    if (total < 0) total = 0.0
    return total
}