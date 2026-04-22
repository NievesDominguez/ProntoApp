package com.example.persistencia

import com.example.persistencia.Herramientas.CheckoutHelper
import com.example.persistencia.Modelos.*
import org.junit.Assert.assertEquals
import org.junit.Test

class CalcularTicketTest {

    private fun producto(id: String, precio: Double, oferta: String? = null) =
        Producto(id = id, nombre = "P $id", precio = precio, oferta = oferta)

    private fun item(id: String, precio: Double, cantidad: Double, oferta: String? = null) =
        ProductoCarrito(producto(id, precio, oferta), cantidad)

    // CT-01: Ticket sin descuentos
    @Test
    fun CT01() {
        val productos = listOf(item("A", 10.0, 2.0))
        val (productosTicket, descuentosTicket, total) =
            CheckoutHelper.calcularTicket(productos, emptyList(), emptyList())

        assertEquals(1, productosTicket.size)
        assertEquals("A", productosTicket[0].productoId)
        assertEquals(20.0, productosTicket[0].subtotal, 0.001)
        assertEquals(0, descuentosTicket.size)
        assertEquals(20.0, total, 0.001)
    }

    // CT-02: Ticket con oferta segunda unidad
    @Test
    fun CT02() {
        val productos = listOf(item("A", 10.0, 2.0, oferta = "OF1"))
        val ofertas = listOf(
            Descuento(codigo = "OF1", formula = mapOf("tipo" to "segunda_unidad", "descuento" to 50))
        )
        val (_, descuentosTicket, total) =
            CheckoutHelper.calcularTicket(productos, ofertas, emptyList())

        assertEquals(15.0, total, 0.001)
        assertEquals(1, descuentosTicket.size)
        assertEquals("OF1", descuentosTicket[0].codigo)
        assertEquals(5.0, descuentosTicket[0].descuentoAplicado, 0.001)
    }

    // CT-03: Ticket con cupón fijo
    @Test
    fun CT03() {
        val productos = listOf(item("A", 30.0, 1.0))
        val cupones = listOf(
            Descuento(codigo = "C1", formula = mapOf("tipo" to "fijo", "minimo" to 20, "valor" to 5))
        )
        val (_, descuentosTicket, total) =
            CheckoutHelper.calcularTicket(productos, emptyList(), cupones)

        assertEquals(25.0, total, 0.001)
        assertEquals(1, descuentosTicket.size)
        assertEquals(5.0, descuentosTicket[0].descuentoAplicado, 0.001)
    }

    // CT-04: Cupón fijo mayor que total
    @Test
    fun CT04() {
        val productos = listOf(item("A", 3.0, 1.0))
        val cupones = listOf(
            Descuento(codigo = "C1", formula = mapOf("tipo" to "fijo", "minimo" to 0, "valor" to 10))
        )
        val (_, descuentosTicket, total) =
            CheckoutHelper.calcularTicket(productos, emptyList(), cupones)

        // CheckoutHelper limita: descuentoReal = min(valor, total) = 3.0
        assertEquals(0.0, total, 0.001)
        assertEquals(3.0, descuentosTicket[0].descuentoAplicado, 0.001)
    }

    // CT-05: Oferta + cupón combinados
    @Test
    fun CT05() {
        val productos = listOf(item("A", 10.0, 2.0, oferta = "OF1"))
        val ofertas = listOf(
            Descuento(codigo = "OF1", formula = mapOf("tipo" to "segunda_unidad", "descuento" to 50))
        )
        val cupones = listOf(
            Descuento(codigo = "C2", formula = mapOf("tipo" to "porcentaje", "minimo" to 0, "valor" to 10))
        )
        val (_, descuentosTicket, total) =
            CheckoutHelper.calcularTicket(productos, ofertas, cupones)

        // Oferta: 20 - 5 = 15. Cupón 10%: 15 * 0.9 = 13.5
        assertEquals(13.5, total, 0.001)
        assertEquals(2, descuentosTicket.size)
    }
}