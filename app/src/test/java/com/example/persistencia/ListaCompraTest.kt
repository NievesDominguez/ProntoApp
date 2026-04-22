package com.example.persistencia

import com.example.persistencia.Firestore.*
import com.example.persistencia.Herramientas.ListaCompraViewModel
import com.example.persistencia.Modelos.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListaCompraTest {

    private lateinit var listasDao: ListasDao
    private lateinit var productosDao: ProductosDao
    private lateinit var usuariosDao: UsuariosDao
    private lateinit var carritoDao: CarritoDao
    private lateinit var ticketsDao: TicketsDao

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        listasDao = mockk(relaxed = true)
        productosDao = mockk(relaxed = true)
        usuariosDao = mockk(relaxed = true)
        carritoDao = mockk(relaxed = true)
        ticketsDao = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Helpers ──

    private fun producto(
        id: String, nombre: String, precio: Double,
        stock: Int = 10, categoria: String = "", subcategoria: String = ""
    ) = Producto(id = id, nombre = nombre, precio = precio, stock = stock,
        categoria = categoria, subcategoria = subcategoria)

    private fun itemLista(id: String, cantidad: Double = 1.0, comprado: Boolean = false) =
        ProductoLista(id = id, cantidad = cantidad, comprado = comprado)

    /**
     * Configura los mocks para la carga inicial (init → cargarDatosIniciales).
     * Debe llamarse ANTES de crear el ViewModel.
     */
    private fun mockearCargaInicial(
        lista: List<ProductoLista> = emptyList(),
        catalogo: List<Producto> = emptyList(),
        orden: String = "fecha",
        carrito: List<Pair<String, Double>> = emptyList()
    ) {
        coEvery { listasDao.getLista() } returns lista
        coEvery { productosDao.getTodos() } returns catalogo
        coEvery { usuariosDao.getOrdenLista() } returns orden
        coEvery { carritoDao.getCarrito() } returns carrito
    }

    /**
     * Crea el ViewModel con los mocks inyectados.
     * El init se ejecuta aquí, por eso mockearCargaInicial() debe ir antes.
     */
    private fun crearViewModel(
        authProvider: () -> String? = { "test-uid" }
    ): ListaCompraViewModel {
        return ListaCompraViewModel(
            listasDao = listasDao,
            productosDao = productosDao,
            usuariosDao = usuariosDao,
            carritoDao = carritoDao,
            ticketsDao = ticketsDao,
            authProvider = authProvider
        )
    }

    // ══════════════════════════════════════════════
    // LV-01 Carga inicial rellena lista, catálogo, orden y carrito
    // ══════════════════════════════════════════════
    @Test
    fun LV01() = runTest {
        mockearCargaInicial(
            lista = listOf(itemLista("P1", 2.0, false)),
            catalogo = listOf(producto("P1", "Leche", 1.50)),
            orden = "nombre",
            carrito = listOf(Pair("P1", 2.0))
        )
        val vm = crearViewModel()
        advanceUntilIdle()

        assertEquals(1, vm.itemsLista.value.size)
        assertEquals("P1", vm.itemsLista.value[0].id)
        assertEquals(1, vm.productosCatalogo.value.size)
        assertEquals("nombre", vm.ordenActual.value)
        assertEquals(2.0, vm.carritoCantidades.value["P1"]!!, 0.001)
        assertFalse(vm.isLoading.value)
    }

    // ══════════════════════════════════════════════
    // LV-02 Carga inicial con todo vacío
    // ══════════════════════════════════════════════
    @Test
    fun LV02() = runTest {
        mockearCargaInicial()
        val vm = crearViewModel()
        advanceUntilIdle()

        assertTrue(vm.itemsLista.value.isEmpty())
        assertTrue(vm.productosCatalogo.value.isEmpty())
        assertEquals("fecha", vm.ordenActual.value)
        assertTrue(vm.carritoCantidades.value.isEmpty())
        assertFalse(vm.isLoading.value)
    }

    // ══════════════════════════════════════════════
    // LV-03 Actualizar orden cambia estado y persiste en DAO
    // ══════════════════════════════════════════════
    @Test
    fun LV03() = runTest {
        mockearCargaInicial()
        val vm = crearViewModel()
        advanceUntilIdle()

        vm.actualizarOrden("nombre")
        advanceUntilIdle()

        assertEquals("nombre", vm.ordenActual.value)
        coVerify { usuariosDao.setOrdenLista("nombre") }
    }

    // ══════════════════════════════════════════════
    // LV-04 Añadir item delega al DAO y recarga lista
    // ══════════════════════════════════════════════
    @Test
    fun LV04() = runTest {
        mockearCargaInicial(lista = listOf(itemLista("P1")))
        val vm = crearViewModel()
        advanceUntilIdle()

        // Después de addItem, getLista devuelve 2 items
        coEvery { listasDao.getLista() } returns listOf(
            itemLista("P1"), itemLista("P2", 3.0)
        )

        vm.addItem("P2", 3.0)
        advanceUntilIdle()

        coVerify { listasDao.addItem("P2", 3.0) }
        assertEquals(2, vm.itemsLista.value.size)
        assertTrue(vm.itemsLista.value.any { it.id == "P2" })
    }

    // ══════════════════════════════════════════════
    // LV-05 Eliminar item delega al DAO y recarga lista
    // ══════════════════════════════════════════════
    @Test
    fun LV05() = runTest {
        mockearCargaInicial(lista = listOf(itemLista("P1"), itemLista("P2")))
        val vm = crearViewModel()
        advanceUntilIdle()

        // Después de eliminar, getLista devuelve solo P2
        coEvery { listasDao.getLista() } returns listOf(itemLista("P2"))

        vm.eliminarItem("P1")
        advanceUntilIdle()

        coVerify { listasDao.eliminarItem("P1") }
        assertEquals(1, vm.itemsLista.value.size)
        assertEquals("P2", vm.itemsLista.value[0].id)
    }

    // ══════════════════════════════════════════════
    // LV-06 Cambiar estado delega al DAO y recarga lista
    // ══════════════════════════════════════════════
    @Test
    fun LV06() = runTest {
        mockearCargaInicial(lista = listOf(itemLista("P1", comprado = false)))
        val vm = crearViewModel()
        advanceUntilIdle()

        coEvery { listasDao.getLista() } returns listOf(itemLista("P1", comprado = true))

        vm.cambiarEstado("P1", true)
        advanceUntilIdle()

        coVerify { listasDao.cambiarEstado("P1", true) }
        assertTrue(vm.itemsLista.value[0].comprado)
    }

    // ══════════════════════════════════════════════
    // LV-07 Actualizar cantidad delega al DAO y recarga lista
    // ══════════════════════════════════════════════
    @Test
    fun LV07() = runTest {
        mockearCargaInicial(lista = listOf(itemLista("P1", 1.0)))
        val vm = crearViewModel()
        advanceUntilIdle()

        coEvery { listasDao.getLista() } returns listOf(itemLista("P1", 5.0))

        vm.actualizarCantidad("P1", 5.0)
        advanceUntilIdle()

        coVerify { listasDao.actualizarCantidad("P1", 5.0) }
        assertEquals(5.0, vm.itemsLista.value[0].cantidad, 0.001)
    }

    // ══════════════════════════════════════════════
    // LV-08 Desmarcar todo delega al DAO y recarga lista
    // ══════════════════════════════════════════════
    @Test
    fun LV08() = runTest {
        mockearCargaInicial(lista = listOf(itemLista("P1", comprado = true)))
        val vm = crearViewModel()
        advanceUntilIdle()

        coEvery { listasDao.getLista() } returns listOf(itemLista("P1", comprado = false))

        vm.desmarcarTodo()
        advanceUntilIdle()

        coVerify { listasDao.desmarcarTodo() }
        assertFalse(vm.itemsLista.value[0].comprado)
    }

    // ══════════════════════════════════════════════
    // LV-09 Eliminar todo delega al DAO y vacía la lista directamente
    // ══════════════════════════════════════════════
    @Test
    fun LV09() = runTest {
        mockearCargaInicial(lista = listOf(itemLista("P1"), itemLista("P2")))
        val vm = crearViewModel()
        advanceUntilIdle()

        vm.eliminarTodo()
        advanceUntilIdle()

        coVerify { listasDao.eliminarTodo() }
        // eliminarTodo() asigna emptyList() directamente, no llama a getLista()
        assertTrue(vm.itemsLista.value.isEmpty())
    }

    // ══════════════════════════════════════════════
    // LV-10 Limpiar sugerencias vacía la lista de sugerencias
    // ══════════════════════════════════════════════
    @Test
    fun LV10() = runTest {
        mockearCargaInicial()
        val vm = crearViewModel()
        advanceUntilIdle()

        vm.limpiarSugerencias()

        assertTrue(vm.sugerencias.value.isEmpty())
    }

    // ══════════════════════════════════════════════
    // LV-11 Sin usuario autenticado no genera sugerencias
    // ══════════════════════════════════════════════
    @Test
    fun LV11() = runTest {
        mockearCargaInicial()
        // authProvider devuelve null → usuario no autenticado
        val vm = crearViewModel(authProvider = { null })
        advanceUntilIdle()

        vm.generarSugerencias()
        advanceUntilIdle()

        assertTrue(vm.sugerencias.value.isEmpty())
        assertFalse(vm.isLoading.value)
    }

    // ══════════════════════════════════════════════
    // LV-12 Sin tickets previos ni agotados devuelve vacío
    // ══════════════════════════════════════════════
    @Test
    fun LV12() = runTest {
        mockearCargaInicial(
            lista = listOf(itemLista("P1")),
            catalogo = listOf(producto("P1", "Leche", 1.50, stock = 10))
        )
        val vm = crearViewModel()
        advanceUntilIdle()

        coEvery { ticketsDao.getUltimosTickets(5) } returns emptyList()

        vm.generarSugerencias()
        advanceUntilIdle()

        // Sin historial y sin agotados → sin sugerencias
        assertTrue(vm.sugerencias.value.isEmpty())
        assertFalse(vm.isLoading.value)
    }

    // ══════════════════════════════════════════════
    // LV-13 Sugiere productos frecuentes del historial que no están en la lista
    // ══════════════════════════════════════════════
    @Test
    fun LV13() = runTest {
        mockearCargaInicial(
            lista = listOf(itemLista("P1")),
            catalogo = listOf(
                producto("P1", "Leche", 1.50),
                producto("P2", "Pan", 2.00),
                producto("P3", "Agua", 0.80)
            )
        )
        val vm = crearViewModel()
        advanceUntilIdle()

        // Historial: P2 aparece 3 veces, P3 aparece 1 vez
        val ticket1 = Ticket(
            productos = listOf(
                ProductoTicket(productoId = "P2", nombre = "Pan", cantidad = 1.0, subtotal = 2.0),
                ProductoTicket(productoId = "P2", nombre = "Pan", cantidad = 1.0, subtotal = 2.0)
            ),
            total = 4.0
        )
        val ticket2 = Ticket(
            productos = listOf(
                ProductoTicket(productoId = "P2", nombre = "Pan", cantidad = 1.0, subtotal = 2.0),
                ProductoTicket(productoId = "P3", nombre = "Agua", cantidad = 1.0, subtotal = 0.80)
            ),
            total = 2.80
        )
        coEvery { ticketsDao.getUltimosTickets(5) } returns listOf(ticket1, ticket2)
        coEvery { productosDao.getProducto("P2") } returns producto("P2", "Pan", 2.00)
        coEvery { productosDao.getProducto("P3") } returns producto("P3", "Agua", 0.80)

        vm.generarSugerencias()
        advanceUntilIdle()

        val ids = vm.sugerencias.value.map { it.id }
        // P2 y P3 están en el historial y NO en la lista → sugeridos
        assertTrue(ids.contains("P2"))
        assertTrue(ids.contains("P3"))
        // P1 está en la lista → NO sugerido
        assertFalse(ids.contains("P1"))
        // P2 es más frecuente (3 veces) → aparece antes que P3
        assertTrue(ids.indexOf("P2") < ids.indexOf("P3"))
        assertFalse(vm.isLoading.value)
    }

    // ══════════════════════════════════════════════
    // LV-14 Excluye productos sin stock de las sugerencias frecuentes
    // ══════════════════════════════════════════════
    @Test
    fun LV14() = runTest {
        mockearCargaInicial(
            lista = listOf(itemLista("P1")),
            catalogo = listOf(
                producto("P1", "Leche", 1.50),
                producto("P2", "Pan", 2.00, stock = 0),
                producto("P3", "Agua", 0.80, stock = 5)
            )
        )
        val vm = crearViewModel()
        advanceUntilIdle()

        val ticket = Ticket(
            productos = listOf(
                ProductoTicket(productoId = "P2", nombre = "Pan", cantidad = 1.0, subtotal = 2.0),
                ProductoTicket(productoId = "P3", nombre = "Agua", cantidad = 1.0, subtotal = 0.80)
            ),
            total = 2.80
        )
        coEvery { ticketsDao.getUltimosTickets(5) } returns listOf(ticket)
        // P2 tiene stock=0 → será filtrado
        coEvery { productosDao.getProducto("P2") } returns producto("P2", "Pan", 2.00, stock = 0)
        coEvery { productosDao.getProducto("P3") } returns producto("P3", "Agua", 0.80, stock = 5)

        vm.generarSugerencias()
        advanceUntilIdle()

        val ids = vm.sugerencias.value.map { it.id }
        // P3 tiene stock → sugerido
        assertTrue(ids.contains("P3"))
        // P2 sin stock → excluido
        assertFalse(ids.contains("P2"))
    }

    // ══════════════════════════════════════════════
    // LV-15 Sugiere alternativas para productos agotados en la lista
    // ══════════════════════════════════════════════
    @Test
    fun LV15() = runTest {
        // P1 está en la lista y tiene stock=0 en el catálogo
        val p1Agotado = producto("P1", "Leche entera", 1.50, stock = 0,
            categoria = "Alimentación", subcategoria = "Lácteos")
        mockearCargaInicial(
            lista = listOf(itemLista("P1")),
            catalogo = listOf(p1Agotado)
        )
        val vm = crearViewModel()
        advanceUntilIdle()

        // Sin tickets → no hay sugerencias por frecuencia
        coEvery { ticketsDao.getUltimosTickets(5) } returns emptyList()

        // Alternativas similares a P1 agotado
        val p4 = producto("P4", "Leche semidesnatada", 1.40, stock = 20,
            categoria = "Alimentación", subcategoria = "Lácteos")
        val p5 = producto("P5", "Leche sin lactosa", 1.60, stock = 15,
            categoria = "Alimentación", subcategoria = "Lácteos")
        coEvery {
            productosDao.getProductosSimilares(
                productoReferencia = p1Agotado,
                limite = 3,
                excluirIds = setOf("P1")
            )
        } returns listOf(p4, p5)

        vm.generarSugerencias()
        advanceUntilIdle()

        val ids = vm.sugerencias.value.map { it.id }
        assertTrue(ids.contains("P4"))
        assertTrue(ids.contains("P5"))
        assertFalse(vm.isLoading.value)
    }
}