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

    // Expandir unidades
    val unidades = items.flatMap { item ->
        List(item.cantidad) { item.producto.precio }
    }.sorted() // de menor a mayor

    var total = 0.0
    var index = 0

    while (index < unidades.size) {
        if (index + 1 < unidades.size) {
            // Si hay pareja, aplicar descuento a la más barata (unidades[index])
            val barato = unidades[index]
            val caro = unidades[index + 1]

            total += barato * (1 - descuento / 100)
            total += caro
            index += 2
        } else {
            // Unidad suelta: sin descuento
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

    // Orden global por precio
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

    data class Unidad(val producto: Producto, val index: Int)

    val unidades = mutableListOf<Unidad>()
    var idx = 0
    for (item in items) {
        repeat(item.cantidad) {
            unidades.add(Unidad(item.producto, idx))
            idx++
        }
    }

    val ordenadas = unidades.sortedBy { it.producto.precio }

    val total = unidades.size
    val gratis = total / n * (n - m)

    val paga = BooleanArray(unidades.size) { true }

    // Marcar las más baratas como gratis
    for (i in 0 until gratis) {
        paga[ordenadas[i].index] = false
    }

    val preciosFinales = MutableList(unidades.size) { 0.0 }

    for ((pos, unidad) in unidades.withIndex()) {
        preciosFinales[pos] =
            if (paga[pos]) unidad.producto.precio else 0.0
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
    ofertas: List<Descuento>,
    cupones: List<Descuento> = emptyList()
): Double {

    var total = 0.0

    // 1) OFERTAS (igual que ya tenías)

    val grupos = carrito
        .filter { it.producto.oferta != null }
        .groupBy { it.producto.oferta }

    for ((codigoOferta, itemsGrupo) in grupos) {
        val oferta = ofertas.find { o -> o.codigo == codigoOferta } ?: continue
        total += calcularTotalGrupo(itemsGrupo, oferta)
    }

    val sinOferta = carrito.filter { it.producto.oferta == null }
    total += sinOferta.sumOf { it.producto.precio * it.cantidad }

    // 2) CUPONES (MISMA LÓGICA, PERO DESPUÉS)

    for (cupon in cupones) {

        // OJO: aquí el tipo del cupón puede venir en cupon.tipo (como tu ejemplo del fijo)
        // o en cupon.formula["tipo"] (como las ofertas). Damos prioridad al de fórmula si existe.
        val tipo = (cupon.formula?.get("tipo") as? String) ?: cupon.tipo

        when (tipo) {

            // Cupones que funcionan EXACTAMENTE igual que las ofertas (segunda_unidad, n_por_m)
            // pero su "grupo" es TODO el carrito
            "segunda_unidad",
            "n_por_m" -> {
                val totalConCupon = calcularTotalGrupo(carrito, cupon)
                total = totalConCupon
            }

            // Cupón fijo con mínimo (ejemplo que me has pasado)
            // formula:
            //   minimo = 20
            //   valor  = 5
            "fijo" -> {
                val minimo = ((cupon.formula?.get("minimo") ?: 0) as Number).toDouble()
                val valor = ((cupon.formula?.get("valor") ?: 0) as Number).toDouble()

                if (total >= minimo) {
                    total -= valor
                }
            }

            // Cupón porcentaje con mínimo (si lo usas igual que el fijo)
            "porcentaje" -> {
                val minimo = ((cupon.formula?.get("minimo") ?: 0) as Number).toDouble()
                val porcentaje = ((cupon.formula?.get("valor") ?: 0) as Number).toDouble()

                if (total >= minimo) {
                    total *= (1 - porcentaje / 100.0)
                }
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