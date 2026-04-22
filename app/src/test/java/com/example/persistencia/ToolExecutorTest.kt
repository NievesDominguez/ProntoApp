package com.example.persistencia

import com.example.persistencia.Firestore.*
import com.example.persistencia.Herramientas.ToolExecutor
import com.example.persistencia.Modelos.*
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ToolExecutorTest {

    private lateinit var carritoDao: CarritoDao
    private lateinit var listasDao: ListasDao
    private lateinit var productosDao: ProductosDao
    private lateinit var descuentosDao: DescuentosDao
    private lateinit var usuariosDao: UsuariosDao
    private lateinit var ticketsDao: TicketsDao
    private lateinit var executor: ToolExecutor

    // Se ejecuta antes de cada test: crea los mocks y el executor
    @Before
    fun setUp() {
        carritoDao = mockk()
        listasDao = mockk()
        productosDao = mockk()
        descuentosDao = mockk()
        usuariosDao = mockk()
        ticketsDao = mockk()
        executor = ToolExecutor(carritoDao, listasDao, productosDao, descuentosDao, usuariosDao, ticketsDao)
    }

    // Helpers para crear objetos de prueba
    private fun producto(
        id: String, nombre: String, precio: Double,
        categoria: String = "", subcategoria: String = "",
        unidad: String? = "ud", oferta: String? = null, stock: Int = 10
    ) = Producto(
        id = id, nombre = nombre, precio = precio,
        categoria = categoria, subcategoria = subcategoria,
        unidad = unidad, oferta = oferta, stock = stock
    )

    // TE-01 Función desconocida devuelve error
    @Test
    fun TE01() = runTest {
        val resultado = executor.execute("funcion_inexistente", null)
        assertEquals("Error: función no soportada", resultado)
    }

    // TE-02 get_carrito vacío
    @Test
    fun TE02() = runTest {
        coEvery { carritoDao.getCarrito() } returns emptyList()

        val resultado = executor.execute("get_carrito", null)
        assertEquals("El carrito está vacío", resultado)
    }

    // TE-03 get_carrito con productos formatea nombre, cantidad, unidad y precio
    @Test
    fun TE03() = runTest {
        coEvery { carritoDao.getCarrito() } returns listOf(Pair("P1", 2.0))
        coEvery { productosDao.getProducto("P1") } returns producto(
            id = "P1", nombre = "Leche entera", precio = 1.50, unidad = "ud"
        )

        val resultado = executor.execute("get_carrito", null)

        // Formato esperado: "Leche entera: 2.0 ud - 3,00€" (o 3.00€ según locale)
        assertTrue(resultado.startsWith("Carrito:\n"))
        assertTrue(resultado.contains("Leche entera"))
        assertTrue(resultado.contains("2.0"))
    }

    // TE-04 get_carrito con producto no encontrado en BD (getProducto devuelve null)
    @Test
    fun TE04() = runTest {
        coEvery { carritoDao.getCarrito() } returns listOf(Pair("P1", 1.0))
        coEvery { productosDao.getProducto("P1") } returns null

        val resultado = executor.execute("get_carrito", null)

        // mapNotNull filtra el null, así que no hay detalles
        assertEquals("Carrito:\n", resultado)
    }

    // TE-05 get_lista_compra vacía
    @Test
    fun TE05() = runTest {
        coEvery { listasDao.getLista() } returns emptyList()

        val resultado = executor.execute("get_lista_compra", null)
        assertEquals("La lista de la compra está vacía", resultado)
    }

    // TE-06 get_lista_compra con item no comprado muestra ❌
    @Test
    fun TE06() = runTest {
        coEvery { listasDao.getLista() } returns listOf(
            ProductoLista(id = "P1", cantidad = 2.0, comprado = false)
        )
        coEvery { productosDao.getProducto("P1") } returns producto(
            id = "P1", nombre = "Pan de molde", precio = 2.0
        )

        val resultado = executor.execute("get_lista_compra", null)

        assertTrue(resultado.startsWith("Lista de la compra:\n"))
        assertTrue(resultado.contains("Pan de molde x2.0 ❌"))
    }

    // TE-07 get_lista_compra con item comprado muestra ✓
    @Test
    fun TE07() = runTest {
        coEvery { listasDao.getLista() } returns listOf(
            ProductoLista(id = "P1", cantidad = 1.0, comprado = true)
        )
        coEvery { productosDao.getProducto("P1") } returns producto(
            id = "P1", nombre = "Pan de molde", precio = 2.0
        )

        val resultado = executor.execute("get_lista_compra", null)

        assertTrue(resultado.contains("Pan de molde x1.0 ✓"))
    }

    // TE-08 get_cupones_usuario sin cupones
    @Test
    fun TE08() = runTest {
        coEvery { usuariosDao.getCupones() } returns emptyList()

        val resultado = executor.execute("get_cupones_usuario", null)
        assertEquals("No tienes cupones activos", resultado)
    }

    // TE-09 get_cupones_usuario con cupones los lista separados por coma
    @Test
    fun TE09() = runTest {
        coEvery { usuariosDao.getCupones() } returns listOf("CUP1", "CUP2")

        val resultado = executor.execute("get_cupones_usuario", null)
        assertEquals("Cupones activos: CUP1, CUP2", resultado)
    }

    // TE-10 get_ofertas sin ofertas disponibles
    @Test
    fun TE10() = runTest {
        coEvery { descuentosDao.getOfertas() } returns emptyList()

        val resultado = executor.execute("get_ofertas", null)
        assertEquals("No hay ofertas disponibles", resultado)
    }

    // TE-11 get_ofertas con ofertas usa nombre o codigo como fallback
    @Test
    fun TE11() = runTest {
        coEvery { descuentosDao.getOfertas() } returns listOf(
            Descuento(codigo = "OF1", nombre = "2a ud al 50%"),
            Descuento(codigo = "OF2", nombre = null)  // sin nombre → usa codigo
        )

        val resultado = executor.execute("get_ofertas", null)

        assertTrue(resultado.contains("Ofertas disponibles:"))
        assertTrue(resultado.contains("2a ud al 50%"))
        assertTrue(resultado.contains("OF2"))
    }

    // TE-12 get_producto_info sin coincidencias
    @Test
    fun TE12() = runTest {
        coEvery { productosDao.getTodos() } returns listOf(
            producto("P1", "Leche entera", 1.50, categoria = "Alimentación")
        )
        val args = JSONObject().put("consulta", "Zapatos")

        val resultado = executor.execute("get_producto_info", args)
        assertEquals("No encontré productos con 'Zapatos'", resultado)
    }

    // TE-13 get_producto_info busca por nombre (ignoreCase)
    @Test
    fun TE13() = runTest {
        coEvery { productosDao.getTodos() } returns listOf(
            producto("P1", "Leche entera", 1.50, categoria = "Alimentación"),
            producto("P2", "Leche desnatada", 1.30, categoria = "Alimentación"),
            producto("P3", "Pan de molde", 2.00, categoria = "Alimentación")
        )
        val args = JSONObject().put("consulta", "leche")  // minúscula → ignoreCase

        val resultado = executor.execute("get_producto_info", args)

        assertTrue(resultado.contains("Leche entera"))
        assertTrue(resultado.contains("Leche desnatada"))
        assertFalse(resultado.contains("Pan de molde"))
    }

    // TE-14 get_producto_info busca también por categoría y subcategoría
    @Test
    fun TE14() = runTest {
        coEvery { productosDao.getTodos() } returns listOf(
            producto("P1", "Coca-Cola", 1.20, categoria = "Alimentación", subcategoria = "Refrescos"),
            producto("P2", "Fanta", 1.10, categoria = "Alimentación", subcategoria = "Refrescos"),
            producto("P3", "Camiseta", 9.99, categoria = "Textil", subcategoria = "Ropa")
        )
        val args = JSONObject().put("consulta", "Refrescos")

        val resultado = executor.execute("get_producto_info", args)

        assertTrue(resultado.contains("Coca-Cola"))
        assertTrue(resultado.contains("Fanta"))
        assertFalse(resultado.contains("Camiseta"))
    }

    // TE-15 get_producto_info devuelve máximo 5 resultados
    @Test
    fun TE15() = runTest {
        // 7 productos que coinciden con "Leche"
        val productos = (1..7).map { i ->
            producto("P$i", "Leche variante $i", 1.0 + i * 0.1, categoria = "Alimentación")
        }
        coEvery { productosDao.getTodos() } returns productos
        val args = JSONObject().put("consulta", "Leche")

        val resultado = executor.execute("get_producto_info", args)

        // Cada producto ocupa una línea; máximo 5 líneas
        val lineas = resultado.split("\n")
        assertEquals(5, lineas.size)
    }

    // TE-16 calcular_total_carrito con carrito vacío
    @Test
    fun TE16() = runTest {
        coEvery { carritoDao.getCarrito() } returns emptyList()

        val resultado = executor.execute("calcular_total_carrito", null)
        assertEquals("El carrito está vacío, total 0€", resultado)
    }

    // TE-17 calcular_total_carrito con productos y oferta segunda unidad
    @Test
    fun TE17() = runTest {
        coEvery { carritoDao.getCarrito() } returns listOf(Pair("P1", 2.0))
        coEvery { productosDao.getProducto("P1") } returns producto(
            id = "P1", nombre = "Leche", precio = 10.0, oferta = "OF1"
        )
        coEvery { descuentosDao.getOfertas() } returns listOf(
            Descuento(codigo = "OF1", formula = mapOf("tipo" to "segunda_unidad", "descuento" to 50))
        )
        coEvery { usuariosDao.getCupones() } returns emptyList()
        coEvery { descuentosDao.getCupones() } returns emptyList()

        val resultado = executor.execute("calcular_total_carrito", null)

        // 10 + 10*0.5 = 15.00€
        assertTrue(resultado.contains("15"))
        assertTrue(resultado.startsWith("Total del carrito:"))
    }

    // TE-18 get_historial_compras sin tickets
    @Test
    fun TE18() = runTest {
        coEvery { ticketsDao.getTickets() } returns emptyList()

        val resultado = executor.execute("get_historial_compras", null)
        assertEquals("No tienes compras previas.", resultado)
    }

    // TE-19 get_historial_compras con tickets formatea fecha, total y nº productos
    @Test
    fun TE19() = runTest {
        val ticket = Ticket(
            id = "T1",
            fecha = Timestamp(1714000000, 0),  // fecha fija para test
            productos = listOf(
                ProductoTicket(productoId = "P1", nombre = "Leche", cantidad = 2.0, subtotal = 3.0),
                ProductoTicket(productoId = "P2", nombre = "Pan", cantidad = 1.0, subtotal = 1.50)
            ),
            total = 4.50
        )
        coEvery { ticketsDao.getTickets() } returns listOf(ticket)

        val resultado = executor.execute("get_historial_compras", null)

        // Verifica que contiene el total formateado y el nº de productos
        assertTrue(resultado.contains("4,50€") || resultado.contains("4.50€"))
        assertTrue(resultado.contains("2 productos"))
        assertTrue(resultado.startsWith("•"))
    }

    // TE-20 get_historial_compras muestra máximo 5 tickets aunque haya más
    @Test
    fun TE20() = runTest {
        // Crear 8 tickets
        val tickets = (1..8).map { i ->
            Ticket(
                id = "T$i",
                fecha = Timestamp(1714000000L + i * 86400, 0),
                productos = listOf(
                    ProductoTicket(productoId = "P1", nombre = "Producto", cantidad = 1.0, subtotal = 5.0)
                ),
                total = 5.0 * i
            )
        }
        coEvery { ticketsDao.getTickets() } returns tickets

        val resultado = executor.execute("get_historial_compras", null)

        // Cada ticket ocupa una línea con "•"; máximo 5
        val lineas = resultado.split("\n").filter { it.startsWith("•") }
        assertEquals(5, lineas.size)
    }
}