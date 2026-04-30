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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


// Repositorio singleton para productos.
// Centraliza todas las llamadas a ProductosDao y las cachea en memoria
// para evitar llamadas redundantes a Firestore desde distintas pantallas.
//
// Usa un patron "stale-while-revalidate" con TTL:
// - Si los datos tienen menos de TTL milisegundos, se devuelven del cache.
// - Si han expirado, se vuelve a consultar Firestore.
// - Se puede forzar la recarga con forceRefresh = true.
object ProductosRepository {

    private val dao = ProductosDao() // Unica instancia del DAO para no crearlo en cada pantalla
    private var cacheTodos: List<Producto>? = null // Cache de la lista completa de productos
    private var cacheProductos: MutableMap<String, Producto> =
        mutableMapOf() // Cache individual por ID
    private var lastFetchTodos: Long =
        0 // Marca de tiempo de la ultima vez que se cargo la lista completa

    private const val TTL =
        60_000L // Tiempo de vida, tras 60s en la siguiente llamada se llama a Firestore

    // Mutex para proteger el acceso concurrente al cache
    private val mutex = Mutex()

    // Obtiene todos los productos de forma segura entre coroutines
    suspend fun getTodos(forceRefresh: Boolean = false): List<Producto> {
        mutex.withLock {
            val now = System.currentTimeMillis()

            // Comprobar si el cache es valido
            if (!forceRefresh && cacheTodos != null && (now - lastFetchTodos) < TTL) {
                return cacheTodos!! // Devolver datos en memoria
            }

            // Cache expirado o vacio: ir a Firestore
            val productos = dao.getTodos()

            // Guardar en cache
            cacheTodos = productos
            lastFetchTodos = now

            // Poblar tambien el cache individual por ID para que getProducto(id) se beneficie
            productos.forEach { cacheProductos[it.id] = it }

            return productos
        }
    }

    // Obtiene un producto por su ID protegido con mutex
    suspend fun getProducto(id: String, forceRefresh: Boolean = false): Producto? {
        mutex.withLock {
            // Intentar devolver desde cache
            if (!forceRefresh) {
                cacheProductos[id]?.let { return it }
            }

            // No esta en cache: ir a Firestore
            val producto = dao.getProducto(id)
            producto?.let { cacheProductos[id] = it } // Guardar para futuras consultas
            return producto
        }
    }

    // Filtra por categoria usando el cache si esta disponible
    suspend fun getPorCategoria(categoria: String): List<Producto> {
        mutex.withLock {
            // Si ya tenemos todos los productos en cache, filtrar en memoria
            cacheTodos?.let { todos ->
                return todos.filter { it.categoria == categoria }
            }
            // Si no hay cache, ir a Firestore
            return dao.getPorCategoria(categoria)
        }
    }

    // Ordena por precio ascendente
    suspend fun getOrdenPrecioAsc(): List<Producto> {
        mutex.withLock {
            cacheTodos?.let { return it.sortedBy { p -> p.precio } }
            return dao.getOrdenPrecioAsc()
        }
    }

    // Ordena por precio descendente
    suspend fun getOrdenPrecioDesc(): List<Producto> {
        mutex.withLock {
            cacheTodos?.let { return it.sortedByDescending { p -> p.precio } }
            return dao.getOrdenPrecioDesc()
        }
    }

    // Busca productos similares al producto de referencia
    suspend fun getProductosSimilares(
        productoReferencia: Producto,
        limite: Int = 5,
        excluirIds: Set<String> = emptySet()
    ): List<Producto> {
        return dao.getProductosSimilares(productoReferencia, limite, excluirIds)
    }

    // Limpia el cache. Se llama al cerrar sesion o cuando se necesita forzar una recarga completa
    suspend fun invalidar() {
        mutex.withLock {
            cacheTodos = null
            cacheProductos.clear()
            lastFetchTodos = 0
        }
    }
}


// Repositorio singleton para ofertas y cupones
object DescuentosRepository {

    private val dao = DescuentosDao()

    // Caches separados para ofertas y cupones
    private var cacheOfertas: List<Descuento>? = null
    private var cacheCupones: List<Descuento>? = null

    // Timestamps de ultima carga
    private var lastFetchOfertas: Long = 0
    private var lastFetchCupones: Long = 0

    private const val TTL = 60_000L // 60 segundos

    // Mutex para proteger el acceso concurrente al cache
    private val mutex = Mutex()

    // Obtiene las ofertas activas de forma segura
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getOfertas(forceRefresh: Boolean = false): List<Descuento> {
        mutex.withLock {
            val now = System.currentTimeMillis()
            if (!forceRefresh && cacheOfertas != null && (now - lastFetchOfertas) < TTL) {
                return cacheOfertas!!
            }
            val ofertas = dao.getOfertas()
            cacheOfertas = ofertas
            lastFetchOfertas = now
            return ofertas
        }
    }

    // Obtiene todos los cupones disponibles de forma segura
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getCupones(forceRefresh: Boolean = false): List<Descuento> {
        mutex.withLock {
            val now = System.currentTimeMillis()
            if (!forceRefresh && cacheCupones != null && (now - lastFetchCupones) < TTL) {
                return cacheCupones!!
            }
            val cupones = dao.getCupones()
            cacheCupones = cupones
            lastFetchCupones = now
            return cupones
        }
    }

    // Limpia el cache de descuentos de forma segura
    suspend fun invalidar() {
        mutex.withLock {
            cacheOfertas = null
            cacheCupones = null
            lastFetchOfertas = 0
            lastFetchCupones = 0
        }
    }
}


// Repositorio singleton para el carrito del usuario
// No usa TTL porque cambia con frecuencia, se invalida con cada escritura
object CarritoRepository {

    private val dao = CarritoDao()

    // Cache del contenido del carrito (pares de idProducto, cantidad)
    private var cacheCarrito: List<Pair<String, Double>>? = null

    // Cache de los codigos de cupones activos en el carrito
    private var cacheCupones: List<String>? = null

    // Mutex para proteger el acceso concurrente al cache
    private val mutex = Mutex()

    // Obtiene el contenido del carrito de forma segura
    suspend fun getCarrito(forceRefresh: Boolean = false): List<Pair<String, Double>> {
        mutex.withLock {
            if (!forceRefresh && cacheCarrito != null) return cacheCarrito!!
            val carrito = dao.getCarrito()
            cacheCarrito = carrito
            return carrito
        }
    }

    // Modifica la cantidad de un producto en el carrito e invalida el cache
    suspend fun addCarrito(producto: Producto, cantidad: Double): Boolean {
        mutex.withLock {
            val result = dao.addCarrito(producto, cantidad)
            if (result) cacheCarrito = null // Invalidar: los datos han cambiado
            return result
        }
    }

    // Obtiene los codigos de cupones activos en el carrito
    suspend fun getCupones(forceRefresh: Boolean = false): List<String> {
        mutex.withLock {
            if (!forceRefresh && cacheCupones != null) return cacheCupones!!
            val cupones = dao.getCupones()
            cacheCupones = cupones
            return cupones
        }
    }

    // Activa un cupon en el carrito e invalida el cache de cupones
    suspend fun activarCupon(codigo: String) {
        mutex.withLock {
            dao.activarCupon(codigo)
            cacheCupones = null // Invalidar tras escritura
        }
    }

    // Desactiva un cupon del carrito e invalida el cache de cupones
    suspend fun desactivarCupon(codigo: String) {
        mutex.withLock {
            dao.desactivarCupon(codigo)
            cacheCupones = null // Invalidar tras escritura
        }
    }

    // Comprueba si un cupon esta activo
    suspend fun comprobarCupon(codigo: String?): Boolean = dao.comprobarCupon(codigo)

    // Vacia el carrito completo e invalida todos los caches
    suspend fun vaciarCarrito() {
        mutex.withLock {
            dao.vaciarCarrito()
            cacheCarrito = null
            cacheCupones = null
        }
    }

    // Limpia el cache de forma segura
    suspend fun invalidar() {
        mutex.withLock {
            cacheCarrito = null
            cacheCupones = null
        }
    }
}


// Repositorio singleton para datos del usuario (cupones, preferencias)
// Se invalida tras escrituras
object UsuariosRepository {

    private val dao = UsuariosDao()

    // Cache de los cupones del usuario
    private var cacheCupones: List<String>? = null

    // Cache de la preferencia de orden de la lista de la compra
    private var cacheOrden: String? = null

    // Mutex para proteger el acceso concurrente al cache
    private val mutex = Mutex()

    // Obtiene los cupones del usuario de forma segura
    suspend fun getCupones(forceRefresh: Boolean = false): List<String> {
        mutex.withLock {
            if (!forceRefresh && cacheCupones != null) return cacheCupones!!
            val cupones = dao.getCupones()
            cacheCupones = cupones
            return cupones
        }
    }

    // Obtiene la preferencia de orden de la lista
    suspend fun getOrdenLista(forceRefresh: Boolean = false): String {
        mutex.withLock {
            if (!forceRefresh && cacheOrden != null) return cacheOrden!!
            val orden = dao.getOrdenLista()
            cacheOrden = orden
            return orden
        }
    }

    // Guarda la preferencia de orden y actualiza el cache
    suspend fun setOrdenLista(orden: String) {
        mutex.withLock {
            dao.setOrdenLista(orden)
            cacheOrden = orden // Actualizacion optimista del cache
        }
    }

    // Elimina un cupon del usuario e invalida cache
    suspend fun removeCupon(codigo: String) {
        mutex.withLock {
            dao.quitarCupon(codigo)
            cacheCupones = null
        }
    }

    // Agrega cupones al usuario e invalida cache
    suspend fun addCupones(codigos: List<String>) {
        mutex.withLock {
            dao.addCupones(codigos)
            cacheCupones = null
        }
    }

    suspend fun buscarUsuarioPorEmail(email: String): String? = dao.buscarUsuarioPorEmail(email)

    // Limpia el cache de forma segura
    suspend fun invalidar() {
        mutex.withLock {
            cacheCupones = null
            cacheOrden = null
        }
    }
}


// Repositorio singleton para tickets de compra
// Se invalida tras guardar un nuevo ticket (compra completada)
object TicketsRepository {
    private val dao = TicketsDao()

    // Cache de todos los tickets del usuario
    private var cacheTickets: List<Ticket>? = null

    // Cache individual por ID de ticket
    private var cacheTicketById: MutableMap<String, Ticket> = mutableMapOf()

    // Mutex para proteger el acceso concurrente al cache
    private val mutex = Mutex()

    // Obtiene todos los tickets del usuario de forma segura
    suspend fun getTickets(forceRefresh: Boolean = false): List<Ticket> {
        mutex.withLock {
            if (!forceRefresh && cacheTickets != null) return cacheTickets!!
            val tickets = dao.getTickets()
            cacheTickets = tickets
            // Poblar cache individual
            tickets.forEach { cacheTicketById[it.id] = it }
            return tickets
        }
    }

    // Obtiene un ticket por su ID de forma segura
    suspend fun getTicketById(ticketId: String): Ticket? {
        mutex.withLock {
            cacheTicketById[ticketId]?.let { return it }
            val ticket = dao.getTicketById(ticketId)
            ticket?.let { cacheTicketById[ticketId] = it }
            return ticket
        }
    }

    // Obtiene los ultimos N tickets
    suspend fun getUltimosTickets(limit: Int = 5): List<Ticket> {
        mutex.withLock {
            // Si ya tenemos todos los tickets cacheados, devolver los primeros N
            cacheTickets?.let { return it.take(limit) }
            return dao.getUltimosTickets(limit)
        }
    }

    // Guarda un ticket nuevo e invalida el cache de la lista
    suspend fun guardarTicket(ticket: Ticket): String? {
        mutex.withLock {
            val id = dao.guardarTicket(ticket)
            if (id != null) cacheTickets = null // Invalidar: hay un ticket nuevo
            return id
        }
    }

    // Limpia el cache de forma segura
    suspend fun invalidar() {
        mutex.withLock {
            cacheTickets = null
            cacheTicketById.clear()
        }
    }
}


// Repositorio singleton para la lista de la compra
// Se invalida tras cada operacion de escritura
object ListasRepository {
    private val dao = ListasCompartidasDao()

    // Cache de las listas del usuario (metadatos)
    private var cacheListasUsuario: List<ListaCompartida>? = null

    // Cache de los items de la lista actualmente seleccionada
    private var cacheItemsActivos: List<ProductoLista>? = null
    private var currentListId: String? = null

    // ID de la lista activa guardado en SharedPreferences
    private var prefs: android.content.SharedPreferences? = null

    // Mutex para proteger el acceso concurrente al cache
    private val mutex = Mutex()

    fun init(context: android.content.Context) {
        prefs = context.getSharedPreferences("lista_prefs", android.content.Context.MODE_PRIVATE)
        currentListId = prefs?.getString("lista_activa", null)
    }

    // Obtener todas las listas del usuario de forma segura
    suspend fun getListasDeUsuario(
        userId: String,
        forceRefresh: Boolean = false
    ): List<ListaCompartida> {
        mutex.withLock {
            if (!forceRefresh && cacheListasUsuario != null) return cacheListasUsuario!!
            val listas = dao.getListasPorMiembro(userId)
            cacheListasUsuario = listas
            return listas
        }
    }

    // Obtener listas donde estoy invitado (pendientes)
    suspend fun getInvitacionesPendientes(userId: String): List<ListaCompartida> {
        return dao.getListasInvitado(userId)
    }

    // Seleccionar una lista activa (cambia el currentListId y fuerza recarga de items)
    suspend fun seleccionarLista(listId: String) {
        mutex.withLock {
            currentListId = listId
            prefs?.edit()?.putString("lista_activa", listId)?.apply()
            cacheItemsActivos = null // Forzar recarga
        }
    }

    // Obtener el ID de la lista activa actual
    fun getListaActivaId(): String? = currentListId

    // Obtener los items de la lista activa de forma segura
    suspend fun getItems(forceRefresh: Boolean = false): List<ProductoLista> {
        mutex.withLock {
            val listId = currentListId ?: return emptyList()
            if (!forceRefresh && cacheItemsActivos != null) return cacheItemsActivos!!
            val items = dao.getItems(listId)
            cacheItemsActivos = items
            return items
        }
    }

    // Crear una nueva lista y seleccionarla
    suspend fun crearLista(nombre: String, userId: String): String? {
        mutex.withLock {
            val id = dao.crearLista(nombre, userId)
            if (id != null) {
                cacheListasUsuario = null // Invalidar cache de listas
                currentListId = id
                prefs?.edit()?.putString("lista_activa", id)?.apply()
                cacheItemsActivos = null
            }
            return id
        }
    }

    // Invitar a usuario por email (buscar UID y agregar a invitados)
    suspend fun invitarUsuario(listId: String, email: String): Boolean {
        val uid = UsuariosDao().buscarUsuarioPorEmail(email) ?: return false
        dao.invitarUsuario(listId, uid)
        return true
    }

    // Aceptar invitacion
    suspend fun aceptarInvitacion(listId: String, userId: String) {
        mutex.withLock {
            dao.aceptarInvitacion(listId, userId)
            cacheListasUsuario = null // Invalidar cache
            if (currentListId == null) {
                currentListId = listId
                prefs?.edit()?.putString("lista_activa", listId)?.apply()
                cacheItemsActivos = null
            }
        }
    }

    // Rechazar invitacion
    suspend fun rechazarInvitacion(listId: String, userId: String) {
        dao.rechazarInvitacion(listId, userId)
    }

    // Eliminar lista (solo owner)
    suspend fun eliminarLista(listId: String) {
        mutex.withLock {
            dao.eliminarLista(listId)
            cacheListasUsuario = null
            if (currentListId == listId) {
                currentListId = null
                prefs?.edit()?.remove("lista_activa")?.apply()
                cacheItemsActivos = null
            }
        }
    }

    // Agregar item a la lista activa
    suspend fun addItem(idProducto: String, cantidad: Double = 1.0) {
        mutex.withLock {
            val listId = currentListId ?: return
            dao.addItem(listId, idProducto, cantidad)
            cacheItemsActivos = null
        }
    }

    // Eliminar un item de la lista activa
    suspend fun eliminarItem(id: String) {
        mutex.withLock {
            val listId = currentListId ?: return
            dao.eliminarItem(listId, id)
            cacheItemsActivos = null
        }
    }

    // Cambiar el estado (marcado/desmarcado) de un item
    suspend fun cambiarEstado(id: String, estado: Boolean) {
        mutex.withLock {
            val listId = currentListId ?: return
            dao.cambiarEstado(listId, id, estado)
            cacheItemsActivos = null
        }
    }

    // Actualizar la cantidad de un item
    suspend fun actualizarCantidad(id: String, nuevaCantidad: Double) {
        mutex.withLock {
            val listId = currentListId ?: return
            dao.actualizarCantidad(listId, id, nuevaCantidad)
            cacheItemsActivos = null
        }
    }

    // Desmarcar todos los items de la lista activa
    suspend fun desmarcarTodo() {
        mutex.withLock {
            val listId = currentListId ?: return
            dao.desmarcarTodo(listId)
            cacheItemsActivos = null
        }
    }

    // Eliminar todos los items de la lista activa
    suspend fun eliminarTodo() {
        mutex.withLock {
            val listId = currentListId ?: return
            dao.eliminarTodoItems(listId)
            cacheItemsActivos = null
        }
    }

    // Limpiar cache de forma segura
    suspend fun invalidar() {
        mutex.withLock {
            cacheListasUsuario = null
            cacheItemsActivos = null
            currentListId = null
        }
    }
}