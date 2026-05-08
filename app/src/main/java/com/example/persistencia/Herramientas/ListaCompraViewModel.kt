package com.example.persistencia.Herramientas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persistencia.Modelos.ListaCompartida
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Modelos.ProductoLista
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListaCompraViewModel(
    private val authProvider: () -> String? = { Firebase.auth.currentUser?.uid }
) : ViewModel() {

    // Estados existentes
    private val _itemsLista = MutableStateFlow<List<ProductoLista>>(emptyList())
    val itemsLista: StateFlow<List<ProductoLista>> = _itemsLista.asStateFlow()

    private val _productosCatalogo = MutableStateFlow<List<Producto>>(emptyList())
    val productosCatalogo: StateFlow<List<Producto>> = _productosCatalogo.asStateFlow()

    private val _carritoCantidades = MutableStateFlow<Map<String, Double>>(emptyMap())
    val carritoCantidades: StateFlow<Map<String, Double>> = _carritoCantidades.asStateFlow()

    private val _sugerencias = MutableStateFlow<List<Producto>>(emptyList())
    val sugerencias: StateFlow<List<Producto>> = _sugerencias.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _ordenActual = MutableStateFlow("fecha")
    val ordenActual: StateFlow<String> = _ordenActual.asStateFlow()

    // Nuevos estados para listas compartidas
    private val _listasUsuario = MutableStateFlow<List<ListaCompartida>>(emptyList())
    val listasUsuario: StateFlow<List<ListaCompartida>> = _listasUsuario.asStateFlow()

    private val _listaActivaId = MutableStateFlow<String?>(null)
    val listaActivaId: StateFlow<String?> = _listaActivaId.asStateFlow()

    private val _nombreListaActiva = MutableStateFlow("Listas de la Compra")
    val nombreListaActiva: StateFlow<String> = _nombreListaActiva.asStateFlow()

    // Propiedad calculada: indica si el usuario actual es el dueño de la lista activa
    val esOwner: Boolean
        get() {
            val userId = authProvider() ?: return false
            val lista = _listasUsuario.value.find { it.id == _listaActivaId.value }
            return lista?.ownerId == userId
        }

    init {
        cargarListas()
    }

    // Carga las listas del usuario (metadatos) y selecciona la activa según preferencia
    fun cargarListas() {
        viewModelScope.launch {
            val userId = authProvider() ?: return@launch
            _isLoading.value = true

            // Obtener metadatos de listas
            _listasUsuario.value = ListasRepository.getListasDeUsuario(userId, forceRefresh = true)

            // Determinar lista activa
            val activaId = ListasRepository.getListaActivaId()
            if (activaId != null && _listasUsuario.value.any { it.id == activaId }) {
                seleccionarLista(activaId)
            } else if (_listasUsuario.value.isNotEmpty()) {
                seleccionarLista(_listasUsuario.value.first().id)
            } else {
                // Si no hay listas, mostrar interfaz vacía
                _listaActivaId.value = null
                _nombreListaActiva.value = "Listas de la compra"
                _itemsLista.value = emptyList()
                _productosCatalogo.value = ProductosRepository.getTodos()
                _carritoCantidades.value = CarritoRepository.getCarrito().associate { it.first to it.second }
                _ordenActual.value = UsuariosRepository.getOrdenLista()
            }
            _isLoading.value = false
        }
    }

    // Elimina la lista
    fun eliminarLista() {
        viewModelScope.launch {
            val listId = _listaActivaId.value ?: return@launch
            _isLoading.value = true
            ListasRepository.eliminarLista(listId)
            cargarListas()
        }
    }

    // Cambia la lista activa y carga sus items y datos adicionales
    fun seleccionarLista(listId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            ListasRepository.seleccionarLista(listId)
            _listaActivaId.value = listId
            _nombreListaActiva.value = _listasUsuario.value.find { it.id == listId }?.nombre ?: ""
            cargarItems()
            _ordenActual.value = UsuariosRepository.getOrdenLista()
            _isLoading.value = false
        }
    }

    // Crea una nueva lista y la establece como activa
    fun crearLista(nombre: String) {
        viewModelScope.launch {
            val userId = authProvider() ?: return@launch
            _isLoading.value = true
            ListasRepository.crearLista(nombre, userId)
            // Recargar metadatos y seleccionar la nueva
            cargarListas()
            _isLoading.value = false
        }
    }

    // Invita a un usuario por email a la lista activa. Retorna true si el email existe.
    suspend fun invitarUsuario(email: String): Boolean {
        val listId = _listaActivaId.value ?: return false
        return ListasRepository.invitarUsuario(listId, email)
    }

    // Recarga los items de la lista activa y catálogo/carrito
    suspend fun cargarItems() {
        _isLoading.value = true
        _itemsLista.value = ListasRepository.getItems(forceRefresh = true)
        _productosCatalogo.value = ProductosRepository.getTodos()
        _carritoCantidades.value = CarritoRepository.getCarrito().associate { it.first to it.second }
        _isLoading.value = false
    }

    // ============ Operaciones sobre items (mantienen compatibilidad) ============
    suspend fun addItem(idProducto: String, cantidad: Double = 1.0) {
        if (_listaActivaId.value == null) return
        ListasRepository.addItem(idProducto, cantidad)
        _itemsLista.value = ListasRepository.getItems(forceRefresh = true)
    }

    suspend fun eliminarItem(id: String) {
        if (_listaActivaId.value == null) return
        ListasRepository.eliminarItem(id)
        _itemsLista.value = ListasRepository.getItems(forceRefresh = true)
    }

    suspend fun cambiarEstado(id: String, estado: Boolean) {
        if (_listaActivaId.value == null) return
        ListasRepository.cambiarEstado(id, estado)
        _itemsLista.value = ListasRepository.getItems(forceRefresh = true)
    }

    suspend fun actualizarCantidad(id: String, nuevaCantidad: Double) {
        if (_listaActivaId.value == null) return
        ListasRepository.actualizarCantidad(id, nuevaCantidad)
        _itemsLista.value = ListasRepository.getItems(forceRefresh = true)
    }

    suspend fun desmarcarTodo() {
        if (_listaActivaId.value == null) return
        ListasRepository.desmarcarTodo()
        _itemsLista.value = ListasRepository.getItems(forceRefresh = true)
    }

    suspend fun eliminarTodo() {
        if (_listaActivaId.value == null) return
        ListasRepository.eliminarTodo()
        _itemsLista.value = emptyList()
    }

    // Actualiza la preferencia de orden
    fun actualizarOrden(orden: String) {
        _ordenActual.value = orden
        viewModelScope.launch {
            UsuariosRepository.setOrdenLista(orden)
        }
    }

    // Generación de sugerencias (sin cambios)
    fun generarSugerencias() {
        viewModelScope.launch {
            _isLoading.value = true

            val userId = authProvider()
            if (userId == null) {
                _isLoading.value = false
                return@launch
            }

            val ultimosTickets = TicketsRepository.getUltimosTickets(5)
            val frecuencia = mutableMapOf<String, Int>()
            ultimosTickets.forEach { ticket ->
                ticket.productos.forEach { prod ->
                    frecuencia[prod.productoId] = frecuencia.getOrDefault(prod.productoId, 0) + 1
                }
            }

            val idsEnLista = _itemsLista.value.map { it.id }.toSet()
            val idsSugeridos = frecuencia.keys.filter { it !in idsEnLista }

            val productosFrecuentes = idsSugeridos
                .mapNotNull { ProductosRepository.getProducto(it) }
                .filter { it.stock > 0 }
                .sortedByDescending { frecuencia[it.id] ?: 0 }
                .take(10)

            val productosAgotados = _itemsLista.value
                .mapNotNull { item -> productosCatalogo.value.find { it.id == item.id } }
                .filter { it.stock == 0 }

            val alternativasAgotados = mutableListOf<Producto>()
            val idsYaEnLista = _itemsLista.value.map { it.id }.toSet()
            val idsProcesados = mutableSetOf<String>()

            for (prodAgotado in productosAgotados) {
                val similares = ProductosRepository.getProductosSimilares(
                    productoReferencia = prodAgotado,
                    limite = 3,
                    excluirIds = idsYaEnLista + idsProcesados
                )
                alternativasAgotados.addAll(similares)
                idsProcesados.addAll(similares.map { it.id })
            }

            val todasSugerencias = (alternativasAgotados + productosFrecuentes).distinctBy { it.id }
            _sugerencias.value = todasSugerencias
            _isLoading.value = false
        }
    }

    fun limpiarSugerencias() {
        _sugerencias.value = emptyList()
    }
}