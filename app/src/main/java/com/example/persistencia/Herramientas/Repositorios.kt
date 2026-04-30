package com.example.persistencia.Herramientas

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.persistencia.Firestore.ProductosDao
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Firestore.DescuentosDao
import com.example.persistencia.Modelos.Descuento
import com.example.persistencia.Firestore.CarritoDao
import com.example.persistencia.Firestore.ListasCompartidasDao
import com.example.persistencia.Firestore.UsuariosDao
import com.example.persistencia.Firestore.TicketsDao
import com.example.persistencia.Modelos.Ticket
import com.example.persistencia.Firestore.ListasDao
import com.example.persistencia.Modelos.ListaCompartida
import com.example.persistencia.Modelos.ProductoLista


/**
 * Repositorio singleton para productos.
 * Centraliza todas las llamadas a ProductosDao y las cachea en memoria
 * para evitar llamadas redundantes a Firestore desde distintas pantallas.
 *
 * Usa un patrón "stale-while-revalidate" con TTL:
 * - Si los datos tienen menos de TTL milisegundos, se devuelven del caché.
 * - Si han expirado, se vuelve a consultar Firestore.
 * - Se puede forzar la recarga con forceRefresh = true.
 */
object ProductosRepository {

    private val dao = ProductosDao() // Única instancia del DAO para no crearlo en cada pantalla
    private var cacheTodos: List<Producto>? = null // Caché de la lista completa de productos
    private var cacheProductos: MutableMap<String, Producto> =
        mutableMapOf() // Caché individual por ID
    private var lastFetchTodos: Long =
        0 // Marca de tiempo de la última vez que se cargó la lista completa

    private const val TTL =
        60_000L // Tiempo de vida, tras 60s en la siguiente llamada se llama a Firestore

    // Obtiene todos los productos
    suspend fun getTodos(forceRefresh: Boolean = false): List<Producto> {
        val now = System.currentTimeMillis()

        // Comprobar si el caché es válido
        if (!forceRefresh && cacheTodos != null && (now - lastFetchTodos) < TTL) {
            return cacheTodos!! // Devolver datos en memoria
        }

        // Caché expirado o vacío: ir a Firestore
        val productos = dao.getTodos()

        // Guardar en caché
        cacheTodos = productos
        lastFetchTodos = now

        // Aprovechar para poblar también el caché individual por ID, así las llamadas a getProducto(id) también se benefician
        productos.forEach { cacheProductos[it.id] = it }

        return productos
    }

    // Obtiene un producto por su ID
    suspend fun getProducto(id: String, forceRefresh: Boolean = false): Producto? {
        // Intentar devolver desde caché
        if (!forceRefresh) {
            cacheProductos[id]?.let { return it }
        }

        // No está en caché: ir a Firestore
        val producto = dao.getProducto(id)
        producto?.let { cacheProductos[id] = it } // Guardar para futuras consultas
        return producto
    }

    // Filtra por categoría
    suspend fun getPorCategoria(categoria: String): List<Producto> {
        // Si ya tenemos todos los productos en caché, filtrar en memoria
        cacheTodos?.let { todos ->
            return todos.filter { it.categoria == categoria }
        }
        // Si no hay caché, ir a Firestore
        return dao.getPorCategoria(categoria)
    }

    // Ordena por precio ascendente
    suspend fun getOrdenPrecioAsc(): List<Producto> {
        cacheTodos?.let { return it.sortedBy { p -> p.precio } }
        return dao.getOrdenPrecioAsc()
    }

    // Ordena por precio descendente
    suspend fun getOrdenPrecioDesc(): List<Producto> {
        cacheTodos?.let { return it.sortedByDescending { p -> p.precio } }
        return dao.getOrdenPrecioDesc()
    }

    // Busca productos similares
    suspend fun getProductosSimilares(
        productoReferencia: Producto,
        limite: Int = 5,
        excluirIds: Set<String> = emptySet()
    ): List<Producto> {
        return dao.getProductosSimilares(productoReferencia, limite, excluirIds)
    }

    // Limpia el caché. Se llama al cerrar sesión o cuando se necesita forzar una recarga completa desde Firestore
    fun invalidar() {
        cacheTodos = null
        cacheProductos.clear()
        lastFetchTodos = 0
    }
}


// Repositorio singleton para ofertas y cupones
object DescuentosRepository {

    private val dao = DescuentosDao()

    // Cachés separados para ofertas y cupones
    private var cacheOfertas: List<Descuento>? = null
    private var cacheCupones: List<Descuento>? = null

    // Timestamps de última carga
    private var lastFetchOfertas: Long = 0
    private var lastFetchCupones: Long = 0

    private const val TTL = 60_000L // 60 segundos

    // Obtiene las ofertas activas
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getOfertas(forceRefresh: Boolean = false): List<Descuento> {
        val now = System.currentTimeMillis()
        if (!forceRefresh && cacheOfertas != null && (now - lastFetchOfertas) < TTL) {
            return cacheOfertas!!
        }
        val ofertas = dao.getOfertas()
        cacheOfertas = ofertas
        lastFetchOfertas = now
        return ofertas
    }

    // Obtiene todos los cupones disponibles
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getCupones(forceRefresh: Boolean = false): List<Descuento> {
        val now = System.currentTimeMillis()
        if (!forceRefresh && cacheCupones != null && (now - lastFetchCupones) < TTL) {
            return cacheCupones!!
        }
        val cupones = dao.getCupones()
        cacheCupones = cupones
        lastFetchCupones = now
        return cupones
    }

    // Limpia el caché de descuentos
    fun invalidar() {
        cacheOfertas = null
        cacheCupones = null
        lastFetchOfertas = 0
        lastFetchCupones = 0
    }
}


/**
 * Repositorio singleton para el carrito del usuario
 * No usa TTL porque cambia con frecuencia, se invalida con cada escritura
 */
object CarritoRepository {

    private val dao = CarritoDao()

    // Caché del contenido del carrito (pares de idProducto, cantidad)
    private var cacheCarrito: List<Pair<String, Double>>? = null

    // Caché de los códigos de cupones activos en el carrito
    private var cacheCupones: List<String>? = null

    // Obtiene el contenido del carrito
    suspend fun getCarrito(forceRefresh: Boolean = false): List<Pair<String, Double>> {
        if (!forceRefresh && cacheCarrito != null) return cacheCarrito!!
        val carrito = dao.getCarrito()
        cacheCarrito = carrito
        return carrito
    }

    // Modifica la cantidad de un producto en el carrito
    suspend fun addCarrito(producto: Producto, cantidad: Double): Boolean {
        val result = dao.addCarrito(producto, cantidad)
        if (result) cacheCarrito = null // Invalidar: los datos han cambiado
        return result
    }

    // Obtiene los códigos de cupones activos en el carrito
    suspend fun getCupones(forceRefresh: Boolean = false): List<String> {
        if (!forceRefresh && cacheCupones != null) return cacheCupones!!
        val cupones = dao.getCupones()
        cacheCupones = cupones
        return cupones
    }

    // Activa un cupón en el carrito e invalida el caché de cupones
    suspend fun activarCupon(codigo: String) {
        dao.activarCupon(codigo)
        cacheCupones = null // Invalidar tras escritura
    }

    // Desactiva un cupón del carrito e invalida el caché de cupones
    suspend fun desactivarCupon(codigo: String) {
        dao.desactivarCupon(codigo)
        cacheCupones = null // Invalidar tras escritura
    }

    // Comprueba si un cupón está activo
    suspend fun comprobarCupon(codigo: String?): Boolean = dao.comprobarCupon(codigo)

    // Vacía el carrito completo e invalida todos los cachés
    suspend fun vaciarCarrito() {
        dao.vaciarCarrito()
        cacheCarrito = null
        cacheCupones = null
    }

    // Limpia el caché
    fun invalidar() {
        cacheCarrito = null
        cacheCupones = null
    }
}


/**
 * Repositorio singleton para datos del usuario (cupones, preferencias)
 * Se invalida tras escrituras
 */
object UsuariosRepository {

    private val dao = UsuariosDao()

    // Caché de los cupones del usuario
    private var cacheCupones: List<String>? = null

    // Caché de la preferencia de orden de la lista de la compra
    private var cacheOrden: String? = null

    // Obtiene los cupones del usuario
    suspend fun getCupones(forceRefresh: Boolean = false): List<String> {
        if (!forceRefresh && cacheCupones != null) return cacheCupones!!
        val cupones = dao.getCupones()
        cacheCupones = cupones
        return cupones
    }

    // Obtiene la preferencia de orden de la lista
    suspend fun getOrdenLista(forceRefresh: Boolean = false): String {
        if (!forceRefresh && cacheOrden != null) return cacheOrden!!
        val orden = dao.getOrdenLista()
        cacheOrden = orden
        return orden
    }

    // Guarda la preferencia de orden
    suspend fun setOrdenLista(orden: String) {
        dao.setOrdenLista(orden)
        cacheOrden = orden // Actualización optimista del caché
    }

    // Elimina un cupón del usuario e invalida caché
    suspend fun removeCupon(codigo: String) {
        dao.quitarCupon(codigo)
        cacheCupones = null
    }

    // Añade cupones al usuario e invalida caché
    suspend fun addCupones(codigos: List<String>) {
        dao.addCupones(codigos)
        cacheCupones = null
    }

    suspend fun buscarUsuarioPorEmail(email: String): String? = dao.buscarUsuarioPorEmail(email)

    // Limpia el caché
    fun invalidar() {
        cacheCupones = null
        cacheOrden = null
    }
}


/**
 * Repositorio singleton para tickets de compra
 * Se invalida tras guardar un nuevo ticket (compra completada)
 */
object TicketsRepository {
    private val dao = TicketsDao()

    // Caché de todos los tickets del usuario
    private var cacheTickets: List<Ticket>? = null

    // Caché individual por ID de ticket
    private var cacheTicketById: MutableMap<String, Ticket> = mutableMapOf()

    // Obtiene todos los tickets del usuario
    suspend fun getTickets(forceRefresh: Boolean = false): List<Ticket> {
        if (!forceRefresh && cacheTickets != null) return cacheTickets!!
        val tickets = dao.getTickets()
        cacheTickets = tickets
        // Poblar caché individual
        tickets.forEach { cacheTicketById[it.id] = it }
        return tickets
    }

    // Obtiene un ticket por su ID
    suspend fun getTicketById(ticketId: String): Ticket? {
        cacheTicketById[ticketId]?.let { return it }
        val ticket = dao.getTicketById(ticketId)
        ticket?.let { cacheTicketById[ticketId] = it }
        return ticket
    }

    // Obtiene los últimos N tickets
    suspend fun getUltimosTickets(limit: Int = 5): List<Ticket> {
        // Si ya tenemos todos los tickets cacheados, devolver los primeros N
        cacheTickets?.let { return it.take(limit) }
        return dao.getUltimosTickets(limit)
    }

    // Guarda un ticket nuevo e invalida el caché de la lista
    suspend fun guardarTicket(ticket: Ticket): String? {
        val id = dao.guardarTicket(ticket)
        if (id != null) cacheTickets = null // Invalidar: hay un ticket nuevo
        return id
    }

    // Limpia el caché
    fun invalidar() {
        cacheTickets = null
        cacheTicketById.clear()
    }
}


// Repositorio singleton para la lista de la compra, se invalida tras cada operación de escritura
object ListasRepository {
    private val dao = ListasCompartidasDao()

    // Caché de las listas del usuario (metadatos)
    private var cacheListasUsuario: List<ListaCompartida>? = null

    // Caché de los items de la lista actualmente seleccionada
    private var cacheItemsActivos: List<ProductoLista>? = null
    private var currentListId: String? = null

    // ID de la lista activa guardado en SharedPreferences
    private var prefs: android.content.SharedPreferences? = null

    fun init(context: android.content.Context) {
        prefs = context.getSharedPreferences("lista_prefs", android.content.Context.MODE_PRIVATE)
        currentListId = prefs?.getString("lista_activa", null)
    }

    // Obtener todas las listas del usuario
    suspend fun getListasDeUsuario(
        userId: String,
        forceRefresh: Boolean = false
    ): List<ListaCompartida> {
        if (!forceRefresh && cacheListasUsuario != null) return cacheListasUsuario!!
        val listas = dao.getListasPorMiembro(userId)
        cacheListasUsuario = listas
        return listas
    }

    // Obtener listas donde estoy invitado (pendientes)
    suspend fun getInvitacionesPendientes(userId: String): List<ListaCompartida> {
        return dao.getListasInvitado(userId)
    }

    // Seleccionar una lista activa (cambia el currentListId y carga sus items)
    suspend fun seleccionarLista(listId: String) {
        currentListId = listId
        prefs?.edit()?.putString("lista_activa", listId)?.apply()
        cacheItemsActivos = null // forzar recarga
    }

    // Obtener el ID de la lista activa actual
    fun getListaActivaId(): String? = currentListId

    // Obtener los items de la lista activa
    suspend fun getItems(forceRefresh: Boolean = false): List<ProductoLista> {
        val listId = currentListId ?: return emptyList()
        if (!forceRefresh && cacheItemsActivos != null) return cacheItemsActivos!!
        val items = dao.getItems(listId)
        cacheItemsActivos = items
        return items
    }

    // Crear una nueva lista y seleccionarla
    suspend fun crearLista(nombre: String, userId: String): String? {
        val id = dao.crearLista(nombre, userId)
        if (id != null) {
            cacheListasUsuario = null // invalidar caché de listas
            seleccionarLista(id)
        }
        return id
    }

    // Invitar a usuario por email (buscar UID y añadir a invitados)
    suspend fun invitarUsuario(listId: String, email: String): Boolean {
        val uid = UsuariosDao().buscarUsuarioPorEmail(email) ?: return false
        dao.invitarUsuario(listId, uid)
        return true
    }

    // Aceptar invitación
    suspend fun aceptarInvitacion(listId: String, userId: String) {
        dao.aceptarInvitacion(listId, userId)
        cacheListasUsuario = null // invalidar caché
        if (currentListId == null) {
            seleccionarLista(listId)
        }
    }

    // Rechazar invitación
    suspend fun rechazarInvitacion(listId: String, userId: String) {
        dao.rechazarInvitacion(listId, userId)
    }

    // Eliminar lista (solo owner)
    suspend fun eliminarLista(listId: String) {
        dao.eliminarLista(listId)
        cacheListasUsuario = null
        if (currentListId == listId) {
            currentListId = null
            prefs?.edit()?.remove("lista_activa")?.apply()
            cacheItemsActivos = null
        }
    }

    // Añadir item a la lista activa
    suspend fun addItem(idProducto: String, cantidad: Double = 1.0) {
        val listId = currentListId ?: return
        dao.addItem(listId, idProducto, cantidad)
        cacheItemsActivos = null
    }

    suspend fun eliminarItem(id: String) {
        val listId = currentListId ?: return
        dao.eliminarItem(listId, id)
        cacheItemsActivos = null
    }

    suspend fun cambiarEstado(id: String, estado: Boolean) {
        val listId = currentListId ?: return
        dao.cambiarEstado(listId, id, estado)
        cacheItemsActivos = null
    }

    suspend fun actualizarCantidad(id: String, nuevaCantidad: Double) {
        val listId = currentListId ?: return
        dao.actualizarCantidad(listId, id, nuevaCantidad)
        cacheItemsActivos = null
    }

    suspend fun desmarcarTodo() {
        val listId = currentListId ?: return
        dao.desmarcarTodo(listId)
        cacheItemsActivos = null
    }

    suspend fun eliminarTodo() {
        val listId = currentListId ?: return
        dao.eliminarTodoItems(listId)
        cacheItemsActivos = null
    }

    // Limpiar caché
    fun invalidar() {
        cacheListasUsuario = null
        cacheItemsActivos = null
        currentListId = null
    }
}