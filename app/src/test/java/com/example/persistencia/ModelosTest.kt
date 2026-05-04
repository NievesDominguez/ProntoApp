// app/src/test/java/com/example/persistencia/ModelosTest.kt
package com.example.persistencia

import com.example.persistencia.Modelos.*
import org.junit.Assert.*
import org.junit.Test

class ModelosTest {

    @Test
    fun MO01() {
        val p = Producto()
        assertEquals("", p.id)
        assertEquals("", p.nombre)
        assertEquals(0.0, p.precio, 0.001)
        assertEquals(0, p.stock)
        assertNull(p.oferta)
        assertEquals("ud", p.unidad)
        assertEquals(false, p.al_peso)
        assertEquals(1.0, p.cantidad!!, 0.001)
        assertEquals(emptyList<String>(), p.alergenos_contiene)
        assertEquals(emptyList<String>(), p.alergenos_trazas)
    }

    @Test
    fun MO02() {
        val p = Producto(id = "123", precio = 5.99, stock = 10)
        assertEquals("123", p.id)
        assertEquals(5.99, p.precio, 0.001)
        assertEquals(10, p.stock)
        assertEquals("", p.nombre)
    }

    @Test
    fun MO03() {
        val d = Descuento()
        assertNull(d.codigo)
        assertNull(d.nombre)
        assertNull(d.tipo)
        assertNull(d.formula)
        assertNull(d.max_descuento)
        assertNull(d.fecha_inicio)
        assertNull(d.fecha_fin)
    }

    @Test
    fun MO04() {
        val pt = ProductoTicket()
        assertEquals("", pt.productoId)
        assertEquals("", pt.nombre)
        assertEquals(0.0, pt.cantidad, 0.001)
        assertEquals("ud", pt.unidad)
        assertEquals(0.0, pt.subtotal, 0.001)
    }

    @Test
    fun MO05() {
        val dt = DescuentoTicket()
        assertEquals("", dt.codigo)
        assertNull(dt.nombre)
        assertEquals("", dt.tipo)
        assertEquals(0.0, dt.descuentoAplicado, 0.001)
    }

    @Test
    fun MO06() {
        val p = Producto(id = "X", precio = 5.0)
        val pc = ProductoCarrito(p, 3.0)
        assertEquals(5.0, pc.producto.precio, 0.001)
        assertEquals(3.0, pc.cantidad, 0.001)
        assertEquals("X", pc.producto.id)
    }

    @Test
    fun MO07() {
        val pl = ProductoLista()
        assertEquals("", pl.id)
        assertEquals(1.0, pl.cantidad, 0.001)
        assertFalse(pl.comprado)
    }

    @Test
    fun MO08() {
        val pl = ProductoLista(id = "P1", cantidad = 3.0, comprado = true)
        assertEquals("P1", pl.id)
        assertEquals(3.0, pl.cantidad, 0.001)
        assertTrue(pl.comprado)
    }

    @Test
    fun MO09() {
        val lc = ListaCompartida()
        assertEquals("", lc.id)
        assertEquals("", lc.nombre)
        assertEquals("", lc.ownerId)
        assertEquals(emptyList<String>(), lc.miembros)
        assertEquals(emptyList<String>(), lc.invitados)
        assertNull(lc.creado)
    }

    @Test
    fun MO10() {
        val lc = ListaCompartida(
            id = "L1", nombre = "Mi lista", ownerId = "U1",
            miembros = listOf("U1", "U2"), invitados = listOf("U3")
        )
        assertEquals("L1", lc.id)
        assertEquals("Mi lista", lc.nombre)
        assertEquals("U1", lc.ownerId)
        assertEquals(2, lc.miembros.size)
        assertEquals(1, lc.invitados.size)
    }

    @Test
    fun MO11() {
        val m = Mensaje("Hola", Mensaje.Sender.USER)
        assertEquals("Hola", m.content)
        assertEquals(Mensaje.Sender.USER, m.sender)
    }

    @Test
    fun MO12() {
        val t = Ticket()
        assertEquals("", t.id)
        assertEquals(emptyList<ProductoTicket>(), t.productos)
        assertEquals(emptyList<DescuentoTicket>(), t.descuentos)
        assertEquals(0.0, t.total, 0.001)
        assertEquals("Stripe", t.metodoPago)
    }
}