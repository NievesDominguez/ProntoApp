package com.example.persistencia.Herramientas

import com.example.persistencia.Firestore.CarritoDao
import com.example.persistencia.Firestore.DescuentosDao
import com.example.persistencia.Firestore.ProductosDao
import com.example.persistencia.Firestore.TicketsDao
import com.example.persistencia.Modelos.*
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CheckoutHelper {

    suspend fun finalizarCompra(limpiarCarrito: Boolean = true): String? {
        return withContext(Dispatchers.IO) {
            val carritoDao = CarritoDao()
            val productosDao = ProductosDao()
            val descuentosDao = DescuentosDao()
            val ticketsDao = TicketsDao()

            // Obtener carrito actual
            val itemsCarrito = carritoDao.getCarrito()
            if (itemsCarrito.isEmpty()) return@withContext null

            // Convertir a ProductoCarrito
            val productosCarrito = itemsCarrito.mapNotNull { (id, cantidad) ->
                productosDao.getProducto(id)?.let { producto ->
                    ProductoCarrito(producto, cantidad)
                }
            }

            // Obtener todas las ofertas y cupones disponibles
            val ofertas = descuentosDao.getOfertas()
            val todosCupones = descuentosDao.getCupones()

            // Obtener los códigos de cupones activos en el carrito del usuario
            val codigosCuponesActivos = carritoDao.getCupones()
            val cuponesActivos = todosCupones.filter { it.codigo in codigosCuponesActivos }

            // Calcular productos, descuentos y total final
            val (productosTicket, descuentosTicket, total) = calcularTicket(
                productosCarrito, ofertas, cuponesActivos
            )

            // Crear ticket
            val ticket = Ticket(
                fecha = Timestamp.now(),
                productos = productosTicket,
                descuentos = descuentosTicket,
                total = total,
                metodoPago = "Stripe"
            )

            // Guardar en Firestore
            val ticketId = ticketsDao.guardarTicket(ticket) ?: return@withContext null

            // Limpiar carrito si se solicita
            if (limpiarCarrito) {
                limpiarCarrito(carritoDao)
            }

            ticketId
        }
    }

    fun calcularTicket(
        productosCarrito: List<ProductoCarrito>,
        ofertas: List<Descuento>,
        cupones: List<Descuento>
    ): Triple<List<ProductoTicket>, List<DescuentoTicket>, Double> {
        val productosTicket = mutableListOf<ProductoTicket>()
        val descuentosTicket = mutableListOf<DescuentoTicket>()

        // Calcular subtotal original (precio * cantidad) para cada producto
        var subtotalOriginal = 0.0
        for (item in productosCarrito) {
            val subtotal = item.producto.precio * item.cantidad
            productosTicket.add(
                ProductoTicket(
                    productoId = item.producto.id,
                    nombre = item.producto.nombre,
                    cantidad = item.cantidad,
                    unidad = item.producto.unidad ?: "ud",
                    subtotal = subtotal
                )
            )
            subtotalOriginal += subtotal
        }

        // Aplicar ofertas de producto (segunda unidad, NxM) y acumular descuentos
        var totalConOfertas = subtotalOriginal
        val gruposPorOferta = productosCarrito.groupBy { it.producto.oferta }

        for ((codigoOferta, itemsGrupo) in gruposPorOferta) {
            if (codigoOferta == null) continue
            val oferta = ofertas.find { it.codigo == codigoOferta }
            if (oferta != null && oferta.formula?.get("tipo") in listOf("segunda_unidad", "n_por_m")) {
                val precioOriginalGrupo = itemsGrupo.sumOf { it.producto.precio * it.cantidad }
                val precioConOferta = when (oferta.formula?.get("tipo")) {
                    "segunda_unidad" -> segundaUnidad(itemsGrupo, oferta)
                    "n_por_m" -> aplicarNxM(itemsGrupo, oferta)
                    else -> precioOriginalGrupo
                }
                val ahorro = precioOriginalGrupo - precioConOferta
                if (ahorro > 0) {
                    totalConOfertas -= ahorro
                    descuentosTicket.add(
                        DescuentoTicket(
                            codigo = oferta.codigo!!,
                            nombre = oferta.nombre,
                            tipo = "oferta",
                            descuentoAplicado = ahorro
                        )
                    )
                }
            }
        }

        // Aplicar cupones sobre el total después de ofertas
        var totalFinal = totalConOfertas
        for (cupon in cupones) {
            val tipo = cupon.formula?.get("tipo") as? String ?: cupon.tipo
            when (tipo) {
                "fijo" -> {
                    val minimo = (cupon.formula?.get("minimo") as? Number)?.toDouble() ?: 0.0
                    val valor = (cupon.formula?.get("valor") as? Number)?.toDouble() ?: 0.0
                    if (totalFinal >= minimo) {
                        val descuentoReal = if (valor > totalFinal) totalFinal else valor
                        totalFinal -= descuentoReal
                        descuentosTicket.add(
                            DescuentoTicket(
                                codigo = cupon.codigo!!,
                                nombre = cupon.nombre,
                                tipo = "cupon",
                                descuentoAplicado = descuentoReal
                            )
                        )
                    }
                }
                "porcentaje" -> {
                    val minimo = (cupon.formula?.get("minimo") as? Number)?.toDouble() ?: 0.0
                    val porcentaje = (cupon.formula?.get("valor") as? Number)?.toDouble() ?: 0.0
                    if (totalFinal >= minimo) {
                        val descuento = totalFinal * (porcentaje / 100.0)
                        totalFinal -= descuento
                        descuentosTicket.add(
                            DescuentoTicket(
                                codigo = cupon.codigo!!,
                                nombre = cupon.nombre,
                                tipo = "cupon",
                                descuentoAplicado = descuento
                            )
                        )
                    }
                }
                "maximo" -> {
                    val max = cupon.max_descuento ?: totalFinal
                    if (totalFinal > max) {
                        val descuento = totalFinal - max
                        totalFinal = max
                        descuentosTicket.add(
                            DescuentoTicket(
                                codigo = cupon.codigo!!,
                                nombre = cupon.nombre,
                                tipo = "cupon",
                                descuentoAplicado = descuento
                            )
                        )
                    }
                }
            }
        }

        if (totalFinal < 0) totalFinal = 0.0

        return Triple(productosTicket, descuentosTicket, totalFinal)
    }

    private suspend fun limpiarCarrito(carritoDao: CarritoDao) {
        carritoDao.vaciarCarrito()
    }
}