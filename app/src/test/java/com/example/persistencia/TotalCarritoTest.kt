package com.example.persistencia

import com.example.persistencia.Herramientas.calcularTotalCarrito
import com.example.persistencia.Modelos.*
import org.junit.Assert.assertEquals
import org.junit.Test

class TotalCarritoTest {

    private fun producto(id: String = "test", precio: Double, oferta: String? = null) =
        Producto(id = id, nombre = "Producto $id", precio = precio, oferta = oferta)

    private fun item(
        precio: Double,
        cantidad: Double = 1.0,
        oferta: String? = null,
        id: String = "test"
    ) =
        ProductoCarrito(producto(id, precio, oferta), cantidad)

    @Test
    fun `TC-01 carrito vacio devuelve 0`() {
        val total = calcularTotalCarrito(emptyList(), emptyList())
        assertEquals(0.0, total, 0.001)
    }

    @Test
    fun `TC-02 sin descuentos suma precio por cantidad`() {
        val carrito = listOf(
            item(precio = 2.50, cantidad = 3.0, id = "A"),
            item(precio = 1.00, cantidad = 2.0, id = "B")
        )
        val total = calcularTotalCarrito(carrito, emptyList())
        // 2.50*3 + 1.00*2 = 7.50 + 2.00 = 9.50
        assertEquals(9.50, total, 0.001)
    }

    @Test
    fun `TC-03 con oferta segunda unidad 50 porciento`() {
        val carrito = listOf(
            item(precio = 10.0, cantidad = 2.0, oferta = "OF1", id = "A")
        )
        val ofertas = listOf(
            Descuento(
                codigo = "OF1",
                formula = mapOf("tipo" to "segunda_unidad", "descuento" to 50)
            )
        )
        val total = calcularTotalCarrito(carrito, ofertas)
        // Par: barato=10*0.5=5, caro=10 -> 15
        assertEquals(15.0, total, 0.001)
    }

    @Test
    fun `TC-04 con oferta 3x2`() {
        val carrito = listOf(
            item(precio = 6.0, cantidad = 3.0, oferta = "OF2", id = "A")
        )
        val ofertas = listOf(
            Descuento(codigo = "OF2", formula = mapOf("tipo" to "n_por_m", "n" to 3, "m" to 2))
        )
        val total = calcularTotalCarrito(carrito, ofertas)
        // Grupo completo de 3, paga 2: 6+6=12
        assertEquals(12.0, total, 0.001)
    }

    @Test
    fun `TC-05 productos con y sin oferta`() {
        val carrito = listOf(
            item(precio = 10.0, cantidad = 2.0, oferta = "OF1", id = "A"),
            item(precio = 3.0, cantidad = 1.0, id = "B")  // sin oferta
        )
        val ofertas = listOf(
            Descuento(
                codigo = "OF1",
                formula = mapOf("tipo" to "segunda_unidad", "descuento" to 50)
            )
        )
        val total = calcularTotalCarrito(carrito, ofertas)
        // Oferta: 15.0 + sin oferta: 3.0 = 18.0
        assertEquals(18.0, total, 0.001)
    }

    @Test
    fun `TC-06 cupon fijo minimo alcanzado`() {
        val carrito = listOf(item(precio = 25.0, cantidad = 1.0))
        val cupon = Descuento(
            codigo = "C1",
            formula = mapOf("tipo" to "fijo", "minimo" to 20, "valor" to 5)
        )
        val total = calcularTotalCarrito(carrito, emptyList(), listOf(cupon))
        // 25 >= 20 -> 25 - 5 = 20
        assertEquals(20.0, total, 0.001)
    }

    @Test
    fun `TC-07 cupon fijo minimo no alcanzado`() {
        val carrito = listOf(item(precio = 5.0, cantidad = 1.0))
        val cupon = Descuento(
            codigo = "C1",
            formula = mapOf("tipo" to "fijo", "minimo" to 20, "valor" to 5)
        )
        val total = calcularTotalCarrito(carrito, emptyList(), listOf(cupon))
        // 5 < 20 -> no se aplica, sigue 5.0
        assertEquals(5.0, total, 0.001)
    }

    @Test
    fun `TC-08 cupon porcentaje 10pct minimo alcanzado`() {
        val carrito = listOf(item(precio = 100.0, cantidad = 1.0))
        val cupon = Descuento(
            codigo = "C2",
            formula = mapOf("tipo" to "porcentaje", "minimo" to 50, "valor" to 10)
        )
        val total = calcularTotalCarrito(carrito, emptyList(), listOf(cupon))
        // 100 >= 50 -> 100 * (1 - 10/100) = 90
        assertEquals(90.0, total, 0.001)
    }

    @Test
    fun `TC-09 cupon porcentaje minimo no alcanzado`() {
        val carrito = listOf(item(precio = 10.0, cantidad = 1.0))
        val cupon = Descuento(
            codigo = "C2",
            formula = mapOf("tipo" to "porcentaje", "minimo" to 50, "valor" to 10)
        )
        val total = calcularTotalCarrito(carrito, emptyList(), listOf(cupon))
        // 10 < 50 -> no se aplica
        assertEquals(10.0, total, 0.001)
    }

    @Test
    fun `TC-10 cupon tipo maximo recorta total`() {
        val carrito = listOf(item(precio = 50.0, cantidad = 1.0))
        val cupon = Descuento(
            codigo = "C3",
            formula = mapOf("tipo" to "maximo"),
            max_descuento = 30.0
        )
        val total = calcularTotalCarrito(carrito, emptyList(), listOf(cupon))
        // 50 > 30 -> total = 30
        assertEquals(30.0, total, 0.001)
    }

    @Test
    fun `TC-11 cupon tipo maximo total ya menor no se aplica`() {
        val carrito = listOf(item(precio = 20.0, cantidad = 1.0))
        val cupon = Descuento(
            codigo = "C3",
            formula = mapOf("tipo" to "maximo"),
            max_descuento = 30.0
        )
        val total = calcularTotalCarrito(carrito, emptyList(), listOf(cupon))
        // 20 <= 30 -> no se aplica, sigue 20
        assertEquals(20.0, total, 0.001)
    }

    @Test
    fun `TC-12 cupon fijo deja total negativo se clampea a 0`() {
        val carrito = listOf(item(precio = 3.0, cantidad = 1.0))
        val cupon = Descuento(
            codigo = "C1",
            formula = mapOf("tipo" to "fijo", "minimo" to 0, "valor" to 10)
        )
        val total = calcularTotalCarrito(carrito, emptyList(), listOf(cupon))
        // 3 - 10 = -7 -> clamped a 0.0
        assertEquals(0.0, total, 0.001)
    }

    @Test
    fun `TC-13 oferta segunda unidad mas cupon fijo combinados`() {
        val carrito = listOf(
            item(precio = 10.0, cantidad = 2.0, oferta = "OF1", id = "A")
        )
        val ofertas = listOf(
            Descuento(
                codigo = "OF1",
                formula = mapOf("tipo" to "segunda_unidad", "descuento" to 50)
            )
        )
        val cupones = listOf(
            Descuento(codigo = "C1", formula = mapOf("tipo" to "fijo", "minimo" to 0, "valor" to 5))
        )
        val total = calcularTotalCarrito(carrito, ofertas, cupones)
        // Oferta: 15.0, luego cupón: 15 - 5 = 10
        assertEquals(10.0, total, 0.001)
    }

    @Test
    fun `TC-14 producto con oferta sin descuento asociado suma normal`() {
        val carrito = listOf(
            item(precio = 10.0, cantidad = 2.0, oferta = "OF_INEXISTENTE", id = "A")
        )
        val total = calcularTotalCarrito(carrito, emptyList())
        // No hay descuento con codigo "OF_INEXISTENTE" -> suma normal: 10*2 = 20
        assertEquals(20.0, total, 0.001)
    }

    @Test
    fun `TC-15 multiples cupones en cadena`() {
        val carrito = listOf(item(precio = 100.0, cantidad = 1.0))
        val cupones = listOf(
            Descuento(
                codigo = "C1",
                formula = mapOf("tipo" to "fijo", "minimo" to 0, "valor" to 10)
            ),
            Descuento(
                codigo = "C2",
                formula = mapOf("tipo" to "porcentaje", "minimo" to 0, "valor" to 10)
            )
        )
        val total = calcularTotalCarrito(carrito, emptyList(), cupones)
        // Primero fijo: 100 - 10 = 90. Luego porcentaje: 90 * 0.9 = 81
        assertEquals(81.0, total, 0.001)
    }
}