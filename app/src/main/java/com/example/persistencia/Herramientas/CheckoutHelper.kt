package com.example.persistencia.Herramientas

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.persistencia.Firestore.DescuentosDao
import com.example.persistencia.Modelos.Descuento
import com.example.persistencia.Modelos.DescuentoTicket
import com.example.persistencia.Modelos.ProductoCarrito
import com.example.persistencia.Modelos.ProductoTicket
import com.example.persistencia.Modelos.Ticket
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CheckoutHelper {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun finalizarCompra(limpiarCarrito: Boolean = true): String? {
        return withContext(Dispatchers.IO) {
            val itemsCarrito = CarritoRepository.getCarrito()
            if (itemsCarrito.isEmpty()) return@withContext null

            val productosCarrito = itemsCarrito.mapNotNull { (id, cantidad) ->
                ProductosRepository.getProducto(id)?.let { ProductoCarrito(it, cantidad) }
            }

            val ofertas = DescuentosRepository.getOfertas()
            val todosCupones = DescuentosRepository.getCupones()
            val codigosCuponesActivos = CarritoRepository.getCupones()
            val cuponesActivos = todosCupones.filter { it.codigo in codigosCuponesActivos }

            // Calcular total sin cupones (solo ofertas)
            val totalSinCupones = calcularTotalCarrito(productosCarrito, ofertas, emptyList())

            // Calcular ticket real
            val (productosTicket, descuentosTicket, totalConCupones) = calcularTicket(
                productosCarrito, ofertas, cuponesActivos
            )

            // Obtener todos los códigos de descuento que se aplicaron (tanto ofertas como cupones)
            val codigosAplicados = descuentosTicket.map { it.codigo }.toSet()

            // Determinar qué cupones activos se aplicaron:
            // 1. Los que están en codigosAplicados
            // 2. Si no están pero el total bajó, asumimos que fueron los cupones de tipo fijo/porcentaje que no se registraron (por error)
            val hayDescuentoPorCupones = totalConCupones < totalSinCupones
            val cuponesAEliminar = if (hayDescuentoPorCupones) {
                // Si hay códigos aplicados, eliminamos esos; si no, eliminamos todos los activos
                if (codigosAplicados.intersect(codigosCuponesActivos.toSet()).isNotEmpty()) {
                    codigosAplicados.intersect(codigosCuponesActivos.toSet()).toList()
                } else {
                    // Fallback: eliminar todos los activos
                    codigosCuponesActivos
                }
            } else {
                emptyList()
            }

            Log.d("Checkout", "Cupones a eliminar: $cuponesAEliminar")

            val ticket = Ticket(
                fecha = Timestamp.now(),
                productos = productosTicket,
                descuentos = descuentosTicket,
                total = totalConCupones,
                metodoPago = "Stripe"
            )

            val ticketId = TicketsRepository.guardarTicket(ticket) ?: return@withContext null

            if (limpiarCarrito) {
                CarritoRepository.vaciarCarrito()
            }

            for (codigo in cuponesAEliminar) {
                UsuariosRepository.removeCupon(codigo)
            }

            val dao = DescuentosDao()
            val cuponesFuturos = dao.getCuponesSemanaProxima()
            val codigosFuturos = cuponesFuturos.mapNotNull { it.codigo }
            if (codigosFuturos.isNotEmpty()) {
                UsuariosRepository.addCupones(codigosFuturos)
            }
            Log.d("Checkout", "Cupones nuevos asignados: $codigosFuturos")

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

        // Unir ofertas y cupones que sean de tipo "n_por_m" o "segunda_unidad"
        val todosDescuentosProducto = ofertas + cupones.filter { cupon ->
            val tipoFormula = cupon.formula?.get("tipo") as? String
            tipoFormula == "n_por_m" || tipoFormula == "segunda_unidad"
        }

        // Calcular subtotal original
        var subtotalOriginal = 0.0
        for (item in productosCarrito) {
            val subtotal = item.producto.precio * item.cantidad
            productosTicket.add(
                ProductoTicket(
                    productoId = item.producto.id,
                    nombre = item.producto.nombre,
                    cantidad = item.cantidad,
                    unidad = item.producto.unidad ?: "ud",
                    subtotal = subtotal,
                    iva = item.producto.iva
                )
            )
            subtotalOriginal += subtotal
        }

        var totalConOfertas = subtotalOriginal
        val gruposPorOferta = productosCarrito.groupBy { it.producto.oferta }

        for ((codigoOferta, itemsGrupo) in gruposPorOferta) {
            if (codigoOferta == null) continue
            val descuento = todosDescuentosProducto.find { it.codigo == codigoOferta }
            if (descuento != null) {
                val precioOriginalGrupo = itemsGrupo.sumOf { it.producto.precio * it.cantidad }
                val precioConOferta = when (descuento.formula?.get("tipo")) {
                    "segunda_unidad" -> segundaUnidad(itemsGrupo, descuento)
                    "n_por_m" -> aplicarNxM(itemsGrupo, descuento)
                    else -> precioOriginalGrupo
                }
                val ahorro = precioOriginalGrupo - precioConOferta
                if (ahorro > 0) {
                    totalConOfertas -= ahorro
                    // Determinar si es cupón u oferta
                    val tipoDescuento =
                        if (cupones.any { it.codigo == descuento.codigo }) "cupon" else "oferta"
                    descuentosTicket.add(
                        DescuentoTicket(
                            codigo = descuento.codigo!!,
                            nombre = descuento.nombre,
                            tipo = tipoDescuento,
                            descuentoAplicado = ahorro
                        )
                    )
                }
            }
        }

        // Aplicar cupones de total (fijo, porcentaje, maximo) excluyendo los ya procesados
        var totalFinal = totalConOfertas
        for (cupon in cupones) {
            val tipoFormula = cupon.formula?.get("tipo") as? String
            if (tipoFormula == "n_por_m" || tipoFormula == "segunda_unidad") continue // Ya procesado

            val tipo = cupon.tipo
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

}