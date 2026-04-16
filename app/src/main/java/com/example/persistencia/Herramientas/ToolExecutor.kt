package com.example.persistencia.Herramientas

import android.util.Log
import com.example.persistencia.Firestore.*
import com.example.persistencia.Modelos.*
import org.json.JSONObject

class ToolExecutor(
    private val carritoDao: CarritoDao,
    private val listasDao: ListasDao,
    private val productosDao: ProductosDao,
    private val descuentosDao: DescuentosDao,
    private val usuariosDao: UsuariosDao,
    private val ticketsDao: TicketsDao
) {
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
        val items = carritoDao.getCarrito()
        if (items.isEmpty()) return "El carrito está vacío"
        val detalles = items.mapNotNull { (id, cantidad) ->
            val prod = productosDao.getProducto(id)
            prod?.let { "${it.nombre}: ${cantidad} ${it.unidad ?: "ud"} - ${"%.2f".format(it.precio * cantidad)}€" }
        }
        return "Carrito:\n" + detalles.joinToString("\n")
    }

    private suspend fun getListaCompra(): String {
        val items = listasDao.getLista()
        if (items.isEmpty()) return "La lista de la compra está vacía"
        val detalles = items.mapNotNull { item ->
            val prod = productosDao.getProducto(item.id)
            prod?.let { "${it.nombre} x${item.cantidad} ${if (item.comprado) "✓" else "❌"}" }
        }
        Log.d("ToolExecutor", "Lista items: $items")
        return "Lista de la compra:\n" + detalles.joinToString("\n")
    }

    private suspend fun getCuponesUsuario(): String {
        val cupones = usuariosDao.getCupones()
        return if (cupones.isNotEmpty()) "Cupones activos: ${cupones.joinToString(", ")}" else "No tienes cupones activos"
    }

    private suspend fun getOfertas(): String {
        val ofertas = descuentosDao.getOfertas()
        return if (ofertas.isNotEmpty()) {
            "Ofertas disponibles:\n" + ofertas.mapNotNull { it.nombre ?: it.codigo }.joinToString("\n")
        } else "No hay ofertas disponibles"
    }

    private suspend fun getProductoInfo(consulta: String): String {
        val productos = productosDao.getTodos()
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
        val carrito = carritoDao.getCarrito()
        if (carrito.isEmpty()) return "El carrito está vacío, total 0€"
        val productosCarrito = carrito.mapNotNull { (id, cantidad) ->
            productosDao.getProducto(id)?.let { ProductoCarrito(it, cantidad) }
        }
        val ofertas = descuentosDao.getOfertas()
        val cuponesActivos = usuariosDao.getCupones()
        val cuponesCompletos = descuentosDao.getCupones().filter { it.codigo in cuponesActivos }
        val total = calcularTotalCarrito(productosCarrito, ofertas, cuponesCompletos)
        return "Total del carrito: ${"%.2f".format(total)}€"
    }

    private suspend fun getHistorialCompras(): String {
        val tickets = ticketsDao.getTickets()
        if (tickets.isEmpty()) return "No tienes compras previas."
        return tickets.take(5).joinToString("\n") { ticket ->
            val fecha = ticket.fecha.toFormattedString()
            "• $fecha - Total: ${"%.2f".format(ticket.total)}€ (${ticket.productos.size} productos)"
        }
    }
}