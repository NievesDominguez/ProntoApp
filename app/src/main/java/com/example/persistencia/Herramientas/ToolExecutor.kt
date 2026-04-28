// app/src/main/java/com/example/persistencia/Herramientas/ToolExecutor.kt
package com.example.persistencia.Herramientas

import android.util.Log
import com.example.persistencia.Modelos.*
import org.json.JSONObject

/**
 * Ejecuta las "tool calls" que el modelo de Groq solicita.
 *
 * ANTES: recibía 6 DAOs por constructor, cada uno una instancia nueva.
 * AHORA: usa los repositorios singleton directamente, sin necesidad de parámetros.
 * Esto simplifica la creación del ToolExecutor y aprovecha el caché compartido.
 */
class ToolExecutor {

    suspend fun execute(functionName: String, arguments: JSONObject?): String {
        return when (functionName) {
            "get_carrito" -> getCarrito()
            "get_lista_compra" -> getListaCompra()
            "get_cupones_usuario" -> getCuponesUsuario()
            "get_ofertas" -> getOfertas()
            "get_producto_info" -> getProductoInfo(arguments?.optString("consulta") ?: "")
            "calcular_total_carrito" -> calcularTotalCarrito()
            "get_historial_compras" -> getHistorialCompras()
            else -> "Error: función no soportada"
        }
    }

    private suspend fun getCarrito(): String {
        // Usa CarritoRepository — si el carrito ya se cargó en la pantalla Carrito, sale del caché
        val items = CarritoRepository.getCarrito()
        if (items.isEmpty()) return "El carrito está vacío"
        val detalles = items.mapNotNull { (id, cantidad) ->
            ProductosRepository.getProducto(id)?.let { prod ->
                "${prod.nombre}: ${cantidad} ${prod.unidad ?: "ud"} - ${"%.2f".format(prod.precio * cantidad)}€"
            }
        }
        return "Carrito:\n" + detalles.joinToString("\n")
    }

    private suspend fun getListaCompra(): String {
        val items = ListasRepository.getLista()
        if (items.isEmpty()) return "La lista de la compra está vacía"
        val detalles = items.mapNotNull { item ->
            ProductosRepository.getProducto(item.id)?.let { prod ->
                "${prod.nombre} x${item.cantidad} ${if (item.comprado) "✓" else "❌"}"
            }
        }
        Log.d("ToolExecutor", "Lista items: $items")
        return "Lista de la compra:\n" + detalles.joinToString("\n")
    }

    private suspend fun getCuponesUsuario(): String {
        val cupones = UsuariosRepository.getCupones()
        return if (cupones.isNotEmpty()) "Cupones activos: ${cupones.joinToString(", ")}"
        else "No tienes cupones activos"
    }

    private suspend fun getOfertas(): String {
        val ofertas = DescuentosRepository.getOfertas()
        return if (ofertas.isNotEmpty()) {
            "Ofertas disponibles:\n" + ofertas.mapNotNull { it.nombre ?: it.codigo }.joinToString("\n")
        } else "No hay ofertas disponibles"
    }

    private suspend fun getProductoInfo(consulta: String): String {
        // getTodos() probablemente ya está cacheado si el usuario visitó el Catálogo
        val productos = ProductosRepository.getTodos()
        val coincidencias = productos.filter {
            it.nombre.contains(consulta, ignoreCase = true) ||
                    it.categoria.contains(consulta, ignoreCase = true) ||
                    it.subcategoria.contains(consulta, ignoreCase = true)
        }.take(5)

        if (coincidencias.isEmpty()) return "No encontré productos con '$consulta'"
        return coincidencias.joinToString("\n") {
            "${it.nombre} - ${"%.2f".format(it.precio)}€ (${it.categoria})"
        }
    }

    private suspend fun calcularTotalCarrito(): String {
        val carrito = CarritoRepository.getCarrito()
        if (carrito.isEmpty()) return "El carrito está vacío, total 0€"
        val productosCarrito = carrito.mapNotNull { (id, cantidad) ->
            ProductosRepository.getProducto(id)?.let { ProductoCarrito(it, cantidad) }
        }
        val ofertas = DescuentosRepository.getOfertas()
        val cuponesActivos = UsuariosRepository.getCupones()
        val cuponesCompletos = DescuentosRepository.getCupones().filter { it.codigo in cuponesActivos }
        val total = calcularTotalCarrito(productosCarrito, ofertas, cuponesCompletos)
        return "Total del carrito: ${"%.2f".format(total)}€"
    }

    private suspend fun getHistorialCompras(): String {
        val tickets = TicketsRepository.getTickets()
        if (tickets.isEmpty()) return "No tienes compras previas."
        return tickets.take(5).joinToString("\n") { ticket ->
            val fecha = ticket.fecha.toFormattedString()
            "• $fecha - Total: ${"%.2f".format(ticket.total)}€ (${ticket.productos.size} productos)"
        }
    }
}