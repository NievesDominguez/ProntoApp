package com.example.persistencia.Herramientas

import com.example.persistencia.Firestore.CarritoDao
import com.example.persistencia.Firestore.ListasDao
import com.example.persistencia.Firestore.ProductosDao
import com.example.persistencia.Firestore.TicketsDao
import com.example.persistencia.Firestore.UsuariosDao
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Modelos.ProductoLista
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListaCompraViewModel : ViewModel() {
    private val listasDao = ListasDao()
    private val productosDao = ProductosDao()
    private val usuariosDao = UsuariosDao()
    private val carritoDao = CarritoDao()
    private val ticketsDao = TicketsDao()

    // Estado
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

    init {
        cargarDatosIniciales()
    }

    fun cargarDatosIniciales() {
        viewModelScope.launch {
            _isLoading.value = true
            _itemsLista.value = listasDao.getLista()
            _productosCatalogo.value = productosDao.getTodos()
            _ordenActual.value = usuariosDao.getOrdenLista()
            _carritoCantidades.value = carritoDao.getCarrito().associate { it.first to it.second }
            _isLoading.value = false
        }
    }

    fun actualizarOrden(orden: String) {
        _ordenActual.value = orden
        viewModelScope.launch {
            usuariosDao.setOrdenLista(orden)
        }
    }

    fun generarSugerencias() {
        viewModelScope.launch {
            _isLoading.value = true
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            // Obtener últimos 5 tickets
            val ultimosTickets = ticketsDao.getUltimosTickets(5)

            // Contar frecuencia de productos
            val frecuencia = mutableMapOf<String, Int>()
            ultimosTickets.forEach { ticket ->
                ticket.productos.forEach { item ->
                    frecuencia[item.productoId] = frecuencia.getOrDefault(item.productoId, 0) + 1
                }
            }

            // Filtrar productos que ya están en la lista
            val idsEnLista = _itemsLista.value.map { it.id }.toSet()
            val idsSugeridos = frecuencia.keys.filter { it !in idsEnLista }

            // Obtener productos sugeridos
            val productosSugeridos = idsSugeridos
                .mapNotNull { productosDao.getProducto(it) }
                .filter { it.stock > 0 }
                .sortedByDescending { frecuencia[it.id] ?: 0 }
                .take(10)

            _sugerencias.value = productosSugeridos
            _isLoading.value = false
        }
    }

    suspend fun getAlternativas(producto: Producto): List<Producto> {
        val idsExcluidos = _itemsLista.value.map { it.id }.toSet()
        return productosDao.getProductosSimilares(
            productoReferencia = producto,
            limite = 5,
            excluirIds = idsExcluidos
        )
    }

    suspend fun addItem(idProducto: String, cantidad: Double = 1.0) {
        listasDao.addItem(idProducto, cantidad)
        _itemsLista.value = listasDao.getLista()
    }

    suspend fun eliminarItem(id: String) {
        listasDao.eliminarItem(id)
        _itemsLista.value = listasDao.getLista()
    }

    suspend fun cambiarEstado(id: String, estado: Boolean) {
        listasDao.cambiarEstado(id, estado)
        _itemsLista.value = listasDao.getLista()
    }

    suspend fun actualizarCantidad(id: String, nuevaCantidad: Double) {
        listasDao.actualizarCantidad(id, nuevaCantidad)
        _itemsLista.value = listasDao.getLista()
    }

    suspend fun desmarcarTodo() {
        listasDao.desmarcarTodo()
        _itemsLista.value = listasDao.getLista()
    }

    suspend fun eliminarTodo() {
        listasDao.eliminarTodo()
        _itemsLista.value = emptyList()
    }

    fun limpiarSugerencias() {
        _sugerencias.value = emptyList()
    }
}