package com.example.persistencia

import com.example.persistencia.Modelos.*
import org.junit.Assert.*
import org.junit.Test

class ModelosTest {

    // ── MO-01: Producto con valores por defecto ──
    @Test
    fun `MO-01 producto valores por defecto`() {
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

    // ── MO-02: Producto con campos personalizados ──
    @Test
    fun `MO-02 producto con campos personalizados`() {
        val p = Producto(id = "123", precio = 5.99, stock = 10)
        assertEquals("123", p.id)
        assertEquals(5.99, p.precio, 0.001)
        assertEquals(10, p.stock)
        assertEquals("", p.nombre)  // sigue siendo el default
    }

    // ── MO-03: Descuento con valores por defecto ──
    @Test
    fun `MO-03 descuento valores por defecto`() {
        val d = Descuento()
        assertNull(d.codigo)
        assertNull(d.nombre)
        assertNull(d.tipo)
        assertNull(d.formula)
        assertNull(d.max_descuento)
        assertNull(d.fecha_inicio)
        assertNull(d.fecha_fin)
    }

    // ── MO-04: ProductoTicket con valores por defecto ──
    @Test
    fun `MO-04 productoTicket valores por defecto`() {
        val pt = ProductoTicket()
        assertEquals("", pt.productoId)
        assertEquals("", pt.nombre)
        assertEquals(0.0, pt.cantidad, 0.001)
        assertEquals("ud", pt.unidad)
        assertEquals(0.0, pt.subtotal, 0.001)
    }

    // ── MO-05: DescuentoTicket con valores por defecto ──
    @Test
    fun `MO-05 descuentoTicket valores por defecto`() {
        val dt = DescuentoTicket()
        assertEquals("", dt.codigo)
        assertNull(dt.nombre)
        assertEquals("", dt.tipo)
        assertEquals(0.0, dt.descuentoAplicado, 0.001)
    }

    // ── MO-06: ProductoCarrito se construye correctamente ──
    @Test
    fun `MO-06 productoCarrito se construye correctamente`() {
        val p = Producto(id = "X", precio = 5.0)
        val pc = ProductoCarrito(p, 3.0)
        assertEquals(5.0, pc.producto.precio, 0.001)
        assertEquals(3.0, pc.cantidad, 0.001)
        assertEquals("X", pc.producto.id)
    }
}