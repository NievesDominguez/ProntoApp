package com.example.persistencia

import com.example.persistencia.Herramientas.segundaUnidad
import com.example.persistencia.Herramientas.aplicarNxM
import com.example.persistencia.Herramientas.calcularTotalGrupo
import com.example.persistencia.Herramientas.calcularPrecioProducto
import com.example.persistencia.Herramientas.precioUnitario2Ud
import com.example.persistencia.Herramientas.precioUnitarioNxM
import com.example.persistencia.Modelos.*
import org.junit.Assert.assertEquals
import org.junit.Test

class CalcularPreciosTest {

    // Helpers
    private fun producto(id: String = "test", precio: Double, oferta: String? = null) =
        Producto(id = id, nombre = "Producto $id", precio = precio, oferta = oferta)

    private fun item(
        precio: Double,
        cantidad: Double = 1.0,
        oferta: String? = null,
        id: String = "test"
    ) =
        ProductoCarrito(producto(id, precio, oferta), cantidad)

    private fun ofertaSU(codigo: String = "OF1", descuento: Int = 50) =
        Descuento(
            codigo = codigo,
            formula = mapOf("tipo" to "segunda_unidad", "descuento" to descuento)
        )

    private fun ofertaNM(codigo: String = "OF2", n: Int = 3, m: Int = 2) =
        Descuento(codigo = codigo, formula = mapOf("tipo" to "n_por_m", "n" to n, "m" to m))


    // SU: aplicarSegundaUnidadCombinable
    // SU-01 dos unidades iguales 50 porciento segunda unidad
    @Test
    fun SU01() {
        val items = listOf(item(precio = 10.0, cantidad = 2.0))
        val resultado = segundaUnidad(items, ofertaSU(descuento = 50))
        // Ordenado: [10, 10]. Par: barato=10*0.5=5, caro=10. Total=15
        assertEquals(15.0, resultado, 0.001)
    }

    // SU-02 dos unidades distintas 50 porciento segunda unidad
    @Test
    fun SU02() {
        val items = listOf(
            item(precio = 5.0, cantidad = 1.0, id = "A"),
            item(precio = 10.0, cantidad = 1.0, id = "B")
        )
        val resultado = segundaUnidad(items, ofertaSU(descuento = 50))
        // Ordenado: [5, 10]. Par: barato=5*0.5=2.5, caro=10. Total=12.5
        assertEquals(12.5, resultado, 0.001)
    }

    // SU-03 tres unidades impar 50 porciento segunda unidad
    @Test
    fun SU03() {
        val items = listOf(item(precio = 10.0, cantidad = 3.0))
        val resultado = segundaUnidad(items, ofertaSU(descuento = 50))
        // Ordenado: [10, 10, 10]. Par: 10*0.5+10=15, sobrante: 10. Total=25
        assertEquals(25.0, resultado, 0.001)
    }

    // SU-04 una sola unidad sin par
    @Test
    fun SU04() {
        val items = listOf(item(precio = 10.0, cantidad = 1.0))
        val resultado = segundaUnidad(items, ofertaSU(descuento = 50))
        // Solo 1 unidad, no hay par -> paga completo
        assertEquals(10.0, resultado, 0.001)
    }

    // SU-05 cuatro unidades 100 porciento segunda unidad gratis
    @Test
    fun SU05() {
        val items = listOf(item(precio = 8.0, cantidad = 4.0))
        val resultado = segundaUnidad(items, ofertaSU(descuento = 100))
        // 2 pares: (8*0 + 8) + (8*0 + 8) = 16
        assertEquals(16.0, resultado, 0.001)
    }

    // SU-06 dos unidades 0 porciento descuento
    @Test
    fun SU06() {
        val items = listOf(item(precio = 10.0, cantidad = 2.0))
        val resultado = segundaUnidad(items, ofertaSU(descuento = 0))
        // 0% descuento = paga todo: 10+10=20
        assertEquals(20.0, resultado, 0.001)
    }

    // SU-07 lista vacia
    @Test
    fun SU07() {
        val resultado = segundaUnidad(emptyList(), ofertaSU(descuento = 50))
        assertEquals(0.0, resultado, 0.001)
    }


    // NM: aplicarNxMCombinable
    // NM-01 3x2 con exactamente 3 unidades iguales
    @Test
    fun NM01() {
        val items = listOf(item(precio = 6.0, cantidad = 3.0))
        val resultado = aplicarNxM(items, ofertaNM(n = 3, m = 2))
        // Ordenado: [6,6,6]. Grupo completo de 3 -> paga 2 más baratos: 6+6=12
        assertEquals(12.0, resultado, 0.001)
    }

    // NM-02 3x2 con 5 unidades grupo incompleto
    @Test
    fun NM02() {
        val items = listOf(item(precio = 6.0, cantidad = 5.0))
        val resultado = aplicarNxM(items, ofertaNM(n = 3, m = 2))
        // Grupo1 (3 uds): take(2)=6+6=12. Grupo2 (2 uds, incompleto): 6+6=12. Total=24
        assertEquals(24.0, resultado, 0.001)
    }

    // NM-03 3x2 con 6 unidades dos grupos completos
    @Test
    fun NM03() {
        val items = listOf(item(precio = 5.0, cantidad = 6.0))
        val resultado = aplicarNxM(items, ofertaNM(n = 3, m = 2))
        // 2 grupos de 3: cada uno paga 2 -> (5+5)+(5+5)=20
        assertEquals(20.0, resultado, 0.001)
    }

    // NM-04 4x3 con 4 unidades
    @Test
    fun NM04() {
        val items = listOf(item(precio = 10.0, cantidad = 4.0))
        val resultado = aplicarNxM(items, ofertaNM(n = 4, m = 3))
        // Grupo de 4, paga 3: 10+10+10=30
        assertEquals(30.0, resultado, 0.001)
    }

    // NM-05 3x2 con precios distintos
    @Test
    fun NM05() {
        val items = listOf(
            item(precio = 2.0, cantidad = 1.0, id = "A"),
            item(precio = 5.0, cantidad = 1.0, id = "B"),
            item(precio = 8.0, cantidad = 1.0, id = "C")
        )
        val resultado = aplicarNxM(items, ofertaNM(n = 3, m = 2))
        // Ordenado: [2, 5, 8]. Grupo completo -> take(2) = 2+5 = 7
        assertEquals(7.0, resultado, 0.001)
    }

    // NM-06 3x2 con 1 sola unidad grupo incompleto
    @Test
    fun NM06() {
        val items = listOf(item(precio = 6.0, cantidad = 1.0))
        val resultado = aplicarNxM(items, ofertaNM(n = 3, m = 2))
        // Grupo incompleto (1 < 3) -> paga todo: 6
        assertEquals(6.0, resultado, 0.001)
    }

    // NM-07 lista vacia
    @Test
    fun NM07() {
        val resultado = aplicarNxM(emptyList(), ofertaNM(n = 3, m = 2))
        assertEquals(0.0, resultado, 0.001)
    }

    // TG: calcularTotalGrupo
// TG-01 tipo segunda_unidad delega correctamente
    @Test
    fun TG01() {
        val items = listOf(item(precio = 10.0, cantidad = 2.0))
        val oferta = Descuento(
            codigo = "OF1",
            formula = mapOf("tipo" to "segunda_unidad", "descuento" to 50)
        )
        assertEquals(15.0, calcularTotalGrupo(items, oferta), 0.001)
    }

    // TG-02 tipo n_por_m delega correctamente
    @Test
    fun TG02() {
        val items = listOf(item(precio = 6.0, cantidad = 3.0))
        val oferta =
            Descuento(codigo = "OF2", formula = mapOf("tipo" to "n_por_m", "n" to 3, "m" to 2))
        assertEquals(12.0, calcularTotalGrupo(items, oferta), 0.001)
    }

    // TG-03 tipo desconocido suma precio por cantidad
    @Test
    fun TG03() {
        val items = listOf(item(precio = 10.0, cantidad = 2.0))
        val oferta = Descuento(codigo = "OF3", formula = mapOf("tipo" to "otro"))
        assertEquals(20.0, calcularTotalGrupo(items, oferta), 0.001)
    }

    // TG-04 formula null suma precio por cantidad
    @Test
    fun TG04() {
        val items = listOf(item(precio = 5.0, cantidad = 3.0))
        val oferta = Descuento(codigo = "OF4", formula = null)
        assertEquals(15.0, calcularTotalGrupo(items, oferta), 0.001)
    }

    // PP: calcularPrecioProducto
// PP-01 segunda unidad 50pct producto barato paga con descuento
    @Test
    fun PP01() {
        val pA = producto("A", 5.0)
        val pB = producto("B", 10.0)
        val itemA = ProductoCarrito(pA, 1.0)
        val itemB = ProductoCarrito(pB, 1.0)
        val grupo = listOf(itemA, itemB)
        val oferta = Descuento(
            codigo = "OF1",
            formula = mapOf("tipo" to "segunda_unidad", "descuento" to 50)
        )

        // A (5€) es el barato del par -> paga 5*0.5 = 2.5
        val resultado = calcularPrecioProducto(itemA, grupo, oferta)
        assertEquals(2.5, resultado, 0.001)
    }

    // PP-02 segunda unidad 50pct producto caro paga completo
    @Test
    fun PP02() {
        val pA = producto("A", 5.0)
        val pB = producto("B", 10.0)
        val itemA = ProductoCarrito(pA, 1.0)
        val itemB = ProductoCarrito(pB, 1.0)
        val grupo = listOf(itemA, itemB)
        val oferta = Descuento(
            codigo = "OF1",
            formula = mapOf("tipo" to "segunda_unidad", "descuento" to 50)
        )

        // B (10€) es el caro del par -> paga 10
        val resultado = calcularPrecioProducto(itemB, grupo, oferta)
        assertEquals(10.0, resultado, 0.001)
    }

    // PP-03 3x2 producto mas barato es gratis
    @Test
    fun PP03() {
        val pA = producto("A", 2.0)
        val pB = producto("B", 5.0)
        val pC = producto("C", 8.0)
        val itemA = ProductoCarrito(pA, 1.0)
        val itemB = ProductoCarrito(pB, 1.0)
        val itemC = ProductoCarrito(pC, 1.0)
        val grupo = listOf(itemA, itemB, itemC)
        val oferta =
            Descuento(codigo = "OF2", formula = mapOf("tipo" to "n_por_m", "n" to 3, "m" to 2))

        // A (2€) es el más barato -> gratis (0.0)
        val resultado = calcularPrecioProducto(itemA, grupo, oferta)
        assertEquals(0.0, resultado, 0.001)
    }

    // PP-04 sin oferta paga precio por cantidad
    @Test
    fun PP04() {
        val p = producto("A", 7.0)
        val itemP = ProductoCarrito(p, 2.0)
        val grupo = listOf(itemP)
        val oferta = Descuento(codigo = "X", formula = null)

        // Sin tipo reconocido -> 7*2 = 14
        val resultado = calcularPrecioProducto(itemP, grupo, oferta)
        assertEquals(14.0, resultado, 0.001)
    }

    // PU: precioUnitario2Ud
// PU-01 dos unidades iguales 50pct
    @Test
    fun PU01() {
        val items = listOf(item(precio = 10.0, cantidad = 2.0))
        val oferta = Descuento(
            codigo = "OF1",
            formula = mapOf("tipo" to "segunda_unidad", "descuento" to 50)
        )
        val resultado = precioUnitario2Ud(items, oferta)

        // 2 unidades: la primera (index 0) es la barata (descuento), la segunda (index 1) paga completo
        // Ordenadas por precio: ambas valen 10. La de index 0 en ordenadas es la de index 0 original.
        // numDescuentos = 2/2 = 1 -> la primera en ordenadas tiene descuento
        assertEquals(2, resultado.size)
        assertEquals(5.0, resultado[0], 0.001)  // con descuento
        assertEquals(10.0, resultado[1], 0.001) // sin descuento
    }

    // PU-02 tres unidades iguales 50pct
    @Test
    fun PU02() {
        val items = listOf(item(precio = 10.0, cantidad = 3.0))
        val oferta = Descuento(
            codigo = "OF1",
            formula = mapOf("tipo" to "segunda_unidad", "descuento" to 50)
        )
        val resultado = precioUnitario2Ud(items, oferta)

        // 3 unidades, numDescuentos = 3/2 = 1 -> solo 1 con descuento
        assertEquals(3, resultado.size)
        assertEquals(5.0, resultado[0], 0.001)   // con descuento (la primera en orden)
        assertEquals(10.0, resultado[1], 0.001)  // sin descuento
        assertEquals(10.0, resultado[2], 0.001)  // sin descuento
    }

    // PU-03 cuatro unidades 100pct gratis
    @Test
    fun PU03() {
        val items = listOf(item(precio = 8.0, cantidad = 4.0))
        val oferta = Descuento(
            codigo = "OF1",
            formula = mapOf("tipo" to "segunda_unidad", "descuento" to 100)
        )
        val resultado = precioUnitario2Ud(items, oferta)

        // 4 unidades, numDescuentos = 4/2 = 2 -> 2 gratis, 2 pagan
        assertEquals(4, resultado.size)
        assertEquals(0.0, resultado[0], 0.001)
        assertEquals(0.0, resultado[1], 0.001)
        assertEquals(8.0, resultado[2], 0.001)
        assertEquals(8.0, resultado[3], 0.001)
    }

    // PNM: precioUnitarioNxM
// PNM-01 3x2 con 3 unidades iguales
    @Test
    fun PNM01() {
        val items = listOf(item(precio = 6.0, cantidad = 3.0))
        val oferta = Descuento(
            codigo = "OF2",
            formula = mapOf("tipo" to "n_por_m", "n" to 3, "m" to 2)
        )
        val resultado = precioUnitarioNxM(items, oferta)

        // 3 uds, gratis = 3/3*(3-2) = 1. La más barata en ordenadas (index 0) es gratis.
        assertEquals(3, resultado.size)
        assertEquals(0.0, resultado[0], 0.001)   // gratis
        assertEquals(6.0, resultado[1], 0.001)   // paga
        assertEquals(6.0, resultado[2], 0.001)   // paga
    }

    // PNM-02 3x2 con 3 unidades precios distintos
    @Test
    fun PNM02() {
        val items = listOf(
            item(precio = 2.0, cantidad = 1.0, id = "A"),
            item(precio = 5.0, cantidad = 1.0, id = "B"),
            item(precio = 8.0, cantidad = 1.0, id = "C")
        )
        val oferta = Descuento(
            codigo = "OF2",
            formula = mapOf("tipo" to "n_por_m", "n" to 3, "m" to 2)
        )
        val resultado = precioUnitarioNxM(items, oferta)

        // Ordenadas por precio: [2(A), 5(B), 8(C)]. gratis=1 -> A es gratis
        assertEquals(3, resultado.size)
        assertEquals(0.0, resultado[0], 0.001)   // A: gratis (el más barato)
        assertEquals(5.0, resultado[1], 0.001)   // B: paga
        assertEquals(8.0, resultado[2], 0.001)   // C: paga
    }

    // PNM-03 3x2 con 5 unidades grupo incompleto
    @Test
    fun PNM03() {
        val items = listOf(item(precio = 6.0, cantidad = 5.0))
        val oferta = Descuento(
            codigo = "OF2",
            formula = mapOf("tipo" to "n_por_m", "n" to 3, "m" to 2)
        )
        val resultado = precioUnitarioNxM(items, oferta)

        // 5 uds, gratis = 5/3*(3-2) = 1. Solo 1 gratis (la más barata en ordenadas)
        assertEquals(5, resultado.size)
        val gratis = resultado.count { it == 0.0 }
        val pagan = resultado.count { it == 6.0 }
        assertEquals(1, gratis)
        assertEquals(4, pagan)
    }
}
