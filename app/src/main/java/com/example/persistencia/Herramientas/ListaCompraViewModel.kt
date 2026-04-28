package com.example.persistencia.Herramientas

import android.util.Log
import com.example.persistencia.Firestore.CarritoDao
import com.example.persistencia.Firestore.ListasDao
import com.example.persistencia.Firestore.ProductosDao
import com.example.persistencia.Firestore.TicketsDao
import com.example.persistencia.Firestore.UsuariosDao
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Modelos.ProductoLista
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListaCompraViewModel(
    private val authProvider: () -> String? = { Firebase.auth.currentUser?.uid }
) : ViewModel() {

    // Estado de la lista de la compra
    private val _itemsLista = MutableStateFlow<List<ProductoLista>>(emptyList())
    val itemsLista: StateFlow<List<ProductoLista>> = _itemsLista.asStateFlow()

    // Catálogo completo de productos
    private val _productosCatalogo = MutableStateFlow<List<Producto>>(emptyList())
    val productosCatalogo: StateFlow<List<Producto>> = _productosCatalogo.asStateFlow()

    // Cantidades en el carrito
    private val _carritoCantidades = MutableStateFlow<Map<String, Double>>(emptyMap())
    val carritoCantidades: StateFlow<Map<String, Double>> = _carritoCantidades.asStateFlow()

    // Sugerencias de productos basadas en historial
    private val _sugerencias = MutableStateFlow<List<Producto>>(emptyList())
    val sugerencias: StateFlow<List<Producto>> = _sugerencias.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Preferencia de orden del usuario
    private val _ordenActual = MutableStateFlow("fecha")
    val ordenActual: StateFlow<String> = _ordenActual.asStateFlow()

    init {
        cargarDatosIniciales()
    }

    fun cargarDatosIniciales() {
        viewModelScope.launch {
            _isLoading.value = true
            // Si el usuario ya visitó el Catálogo, getTodos() sale del caché instantáneamente
            _itemsLista.value = ListasRepository.getLista()
            _productosCatalogo.value = ProductosRepository.getTodos()
            _ordenActual.value = UsuariosRepository.getOrdenLista()
            _carritoCantidades.value =
                CarritoRepository.getCarrito().associate { it.first to it.second }
            _isLoading.value = false
        }
    }

    fun actualizarOrden(orden: String) {
        _ordenActual.value = orden
        viewModelScope.launch {
            // Guarda la preferencia y actualiza el caché del repositorio internamente
            UsuariosRepository.setOrdenLista(orden)
        }
    }

    fun generarSugerencias() {
        viewModelScope.launch {
            _isLoading.value = true

            val userId = authProvider()
            if (userId == null) {
                _isLoading.value = false
                return@launch
            }

            // Sugerencias basadas en historial de compras
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
                .mapNotNull { ProductosRepository.getProducto(it) } // Usa caché individual
                .filter { it.stock > 0 }
                .sortedByDescending { frecuencia[it.id] ?: 0 }
                .take(10)

            // Alternativas para productos agotados en la lista
            Log.d("ListaCompraVM", "Iniciando generación de sugerencias")

            val productosAgotados = _itemsLista.value
                .mapNotNull { item -> productosCatalogo.value.find { it.id == item.id } }
                .filter { it.stock == 0 }

            Log.d("ListaCompraVM", "Productos agotados en lista: ${productosAgotados.size}")

            val alternativasAgotados = mutableListOf<Producto>()
            val idsYaEnLista = _itemsLista.value.map { it.id }.toSet()
            val idsProcesados = mutableSetOf<String>()

            for (prodAgotado in productosAgotados) {
                Log.d("ListaCompraVM", "Buscando similares para: ${prodAgotado.nombre}")
                val similares = ProductosRepository.getProductosSimilares(
                    productoReferencia = prodAgotado,
                    limite = 3,
                    excluirIds = idsYaEnLista + idsProcesados
                )
                Log.d("ListaCompraVM", "Similares encontrados: ${similares.size}")
                alternativasAgotados.addAll(similares)
                idsProcesados.addAll(similares.map { it.id })
            }

            // Combinar listas de alternativas
            val todasSugerencias = (alternativasAgotados + productosFrecuentes).distinctBy { it.id }
            _sugerencias.value = todasSugerencias
            _isLoading.value = false
        }
    }

    fun limpiarSugerencias() {
        _sugerencias.value = emptyList()
    }

    // Operaciones de escritura
    suspend fun addItem(idProducto: String, cantidad: Double = 1.0) {
        ListasRepository.addItem(idProducto, cantidad)
        _itemsLista.value =
            ListasRepository.getLista(forceRefresh = true) // Forzar recarga tras escritura
    }

    suspend fun eliminarItem(id: String) {
        ListasRepository.eliminarItem(id)
        _itemsLista.value = ListasRepository.getLista(forceRefresh = true)
    }

    suspend fun cambiarEstado(id: String, estado: Boolean) {
        ListasRepository.cambiarEstado(id, estado)
        _itemsLista.value = ListasRepository.getLista(forceRefresh = true)
    }

    suspend fun actualizarCantidad(id: String, nuevaCantidad: Double) {
        ListasRepository.actualizarCantidad(id, nuevaCantidad)
        _itemsLista.value = ListasRepository.getLista(forceRefresh = true)
    }

    suspend fun desmarcarTodo() {
        ListasRepository.desmarcarTodo()
        _itemsLista.value = ListasRepository.getLista(forceRefresh = true)
    }

    suspend fun eliminarTodo() {
        ListasRepository.eliminarTodo()
        _itemsLista.value = emptyList()
    }
}