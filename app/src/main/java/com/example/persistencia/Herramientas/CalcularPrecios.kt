package com.example.persistencia.Herramientas

import com.example.persistencia.Modelos.Descuento
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Modelos.ProductoCarrito

// Segunda unidad combinable
fun segundaUnidad(
    items: List<ProductoCarrito>,
    oferta: Descuento
): Double {
    // Extrae el porcentaje de descuento del mapa de fórmula
    val descuento = ((oferta.formula?.get("descuento") ?: 0) as Number).toDouble()

    // Expande las cantidades en unidades individuales y ordena por precio
    val unidades = items.flatMap { item ->
        List(item.cantidad.toInt()) { item.producto.precio }
    }.sorted()

    var total = 0.0
    var index = 0

    // Procesa las unidades en pares
    while (index < unidades.size) {
        if (index + 1 < unidades.size) {
            // Hay par: el más barato tiene descuento, el caro paga completo
            val barato = unidades[index]
            val caro = unidades[index + 1]
            total += barato * (1 - descuento / 100)  // Barato con descuento
            total += caro                            // Caro sin descuento
            index += 2
        } else {
            // Unidad sobrante sin par: paga completo
            total += unidades[index]
            index += 1
        }
    }

    return total
}

// n por m combinable (3x2)
fun aplicarNxM(
    items: List<ProductoCarrito>,
    oferta: Descuento
): Double {
    // Extrae n (unidades necesarias) y m (unidades a pagar)
    val n = ((oferta.formula?.get("n") ?: 0) as Number).toInt()
    val m = ((oferta.formula?.get("m") ?: 0) as Number).toInt()

    // Expande y ordena las unidades por precio
    val unidades = items.flatMap { item ->
        List(item.cantidad.toInt()) { item.producto.precio }
    }.sorted()

    var total = 0.0
    var index = 0

    // Procesa en grupos de n unidades
    while (index < unidades.size) {
        val grupo = unidades.drop(index).take(n)

        total += if (grupo.size == n) {
            // Grupo completo: paga solo las m más baratas
            grupo.take(m).sum()
        } else {
            // Grupo incompleto: paga todas
            grupo.sum()
        }

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
        "segunda_unidad" -> segundaUnidad(items, oferta)
        "n_por_m" -> aplicarNxM(items, oferta)
        else -> items.sumOf { it.producto.precio * it.cantidad }  // Sin oferta
    }
}

// Calcula el precio unitario de cada unidad para oferta de segunda unidad
fun precioUnitario2Ud(
    items: List<ProductoCarrito>,
    oferta: Descuento
): List<Double> {
    val descuento = ((oferta.formula?.get("descuento") ?: 0) as Number).toDouble()

    // Data class para rastrear cada unidad con su índice original
    data class Unidad(val producto: Producto, val index: Int)

    // Expande las unidades manteniendo el índice original
    val unidades = mutableListOf<Unidad>()
    var idx = 0
    for (item in items) {
        repeat(item.cantidad.toInt()) {
            unidades.add(Unidad(item.producto, idx))
            idx++
        }
    }

    // Ordena por precio para identificar cuáles tienen descuento
    val ordenadas = unidades.sortedBy { u -> u.producto.precio }
    val numDescuentos = ordenadas.size / 2  // La mitad de las unidades tienen descuento

    // Marca las unidades más baratas con descuento
    val descuentoAplicado = BooleanArray(unidades.size)
    for (i in 0 until numDescuentos) {
        val unidadConDescuento = ordenadas[i]
        descuentoAplicado[unidadConDescuento.index] = true
    }

    // Calcula el precio final de cada unidad en orden original
    val preciosFinales = MutableList(unidades.size) { 0.0 }
    for ((pos, unidad) in unidades.withIndex()) {
        val base = unidad.producto.precio
        preciosFinales[pos] = if (descuentoAplicado[pos]) base * (1 - descuento / 100) else base
    }

    return preciosFinales
}

// Calcula el precio unitario de cada unidad para oferta n por m
fun precioUnitarioNxM(
    items: List<ProductoCarrito>,
    oferta: Descuento
): List<Double> {
    val n = ((oferta.formula?.get("n") ?: 0) as Number).toInt()
    val m = ((oferta.formula?.get("m") ?: 0) as Number).toInt()

    data class Unidad(val producto: Producto, val index: Int)

    // Expande las unidades manteniendo el índice original
    val unidades = mutableListOf<Unidad>()
    var idx = 0
    for (item in items) {
        repeat(item.cantidad.toInt()) {
            unidades.add(Unidad(item.producto, idx))
            idx++
        }
    }

    // Ordena por precio
    val ordenadas = unidades.sortedBy { it.producto.precio }
    val total = unidades.size
    // Calcula cuántas unidades son gratis
    val gratis = total / n * (n - m)

    // Marca las unidades más baratas como gratis
    val paga = BooleanArray(unidades.size) { true }
    for (i in 0 until gratis) {
        paga[ordenadas[i].index] = false
    }

    // Calcula el precio final (0 si es gratis, precio normal si paga)
    val preciosFinales = MutableList(unidades.size) { 0.0 }
    for ((pos, unidad) in unidades.withIndex()) {
        preciosFinales[pos] = if (paga[pos]) unidad.producto.precio else 0.0
    }

    return preciosFinales
}

// Calcula el precio total de un producto específico dentro de un grupo con oferta
fun calcularPrecioProducto(
    item: ProductoCarrito,
    itemsGrupo: List<ProductoCarrito>,
    oferta: Descuento
): Double {
    // Obtiene los precios unitarios según el tipo de oferta
    val preciosUnitarios: List<Double> = when (oferta.formula?.get("tipo")) {
        "segunda_unidad" -> precioUnitario2Ud(itemsGrupo, oferta)
        "n_por_m" -> precioUnitarioNxM(itemsGrupo, oferta)
        else -> {
            // Sin oferta: precio normal para todas las unidades
            val lista = mutableListOf<Double>()
            for (ig in itemsGrupo) {
                repeat(ig.cantidad.toInt()) {
                    lista.add(ig.producto.precio)
                }
            }
            lista
        }
    }

    // Expande las unidades del grupo
    val unidades = mutableListOf<Producto>()
    for (ig in itemsGrupo) {
        repeat(ig.cantidad.toInt()) {
            unidades.add(ig.producto)
        }
    }

    // Suma solo los precios correspondientes al producto específico
    var total = 0.0
    for (i in unidades.indices) {
        val prod = unidades[i]
        if (prod.id == item.producto.id) {
            total += preciosUnitarios[i]
        }
    }

    return total
}

// Calcula el total del carrito aplicando ofertas y cupones
fun calcularTotalCarrito(
    carrito: List<ProductoCarrito>,
    ofertas: List<Descuento>,
    cupones: List<Descuento> = emptyList()
): Double {
    var total = 0.0

    // Filtra descuentos de tipo producto (segunda unidad o n por m)
    val descuentosProducto = (ofertas + cupones).filter {
        val tipo = (it.formula?.get("tipo") as? String) ?: it.tipo
        tipo == "segunda_unidad" || tipo == "n_por_m"
    }

    // Agrupa productos por código de oferta
    val grupos = carrito
        .filter { it.producto.oferta != null }
        .groupBy { it.producto.oferta }

    // Calcula el total de cada grupo con su oferta correspondiente
    for ((codigo, itemsGrupo) in grupos) {
        val descuento = descuentosProducto.find { it.codigo == codigo }
        if (descuento != null) {
            total += calcularTotalGrupo(itemsGrupo, descuento)
        } else {
            total += itemsGrupo.sumOf { it.producto.precio * it.cantidad }
        }
    }

    // Suma productos sin oferta
    val sinDescuento = carrito.filter { it.producto.oferta == null }
    total += sinDescuento.sumOf { it.producto.precio * it.cantidad }

    // Aplica cupones al total
    for (cupon in cupones) {
        val tipo = (cupon.formula?.get("tipo") as? String) ?: cupon.tipo

        when (tipo) {
            "fijo" -> {
                // Descuento fijo si se cumple el mínimo
                val minimo = ((cupon.formula?.get("minimo") ?: 0) as Number).toDouble()
                val valor = ((cupon.formula?.get("valor") ?: 0) as Number).toDouble()
                if (total >= minimo) total -= valor
            }
            "porcentaje" -> {
                // Descuento porcentual si se cumple el mínimo
                val minimo = ((cupon.formula?.get("minimo") ?: 0) as Number).toDouble()
                val porcentaje = ((cupon.formula?.get("valor") ?: 0) as Number).toDouble()
                if (total >= minimo) total *= (1 - porcentaje / 100.0)
            }
            "maximo" -> {
                // Limita el total a un máximo
                val max = cupon.max_descuento ?: total
                if (total > max) total = max
            }
        }
    }

    // Evita totales negativos
    if (total < 0) total = 0.0
    return total
}