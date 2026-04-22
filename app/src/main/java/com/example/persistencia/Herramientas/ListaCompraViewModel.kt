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
    private val listasDao: ListasDao = ListasDao(),
    private val productosDao: ProductosDao = ProductosDao(),
    private val usuariosDao: UsuariosDao = UsuariosDao(),
    private val carritoDao: CarritoDao = CarritoDao(),
    private val ticketsDao: TicketsDao = TicketsDao(),
    private val authProvider: () -> String? = { Firebase.auth.currentUser?.uid }
) : ViewModel() {

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

            //val userId = Firebase.auth.currentUser?.uid
            val userId = authProvider() // Así para que funcionen los tests
            if (userId == null) {
                _isLoading.value = false
                return@launch
            }

            // Sugerencias basadas en historial de compras
            val ultimosTickets = ticketsDao.getUltimosTickets(5)
            val frecuencia = mutableMapOf<String, Int>()
            ultimosTickets.forEach { ticket ->
                ticket.productos.forEach { prod ->
                    frecuencia[prod.productoId] = frecuencia.getOrDefault(prod.productoId, 0) + 1
                }
            }

            val idsEnLista = _itemsLista.value.map { it.id }.toSet()
            val idsSugeridos = frecuencia.keys.filter { it !in idsEnLista }

            val productosFrecuentes = idsSugeridos
                .mapNotNull { productosDao.getProducto(it) }
                .filter { it.stock > 0 }
                .sortedByDescending { frecuencia[it.id] ?: 0 }
                .take(10)

            // Alternativas para productos agotados en la lista
            Log.d("ListaCompraVM", "Iniciando generación de sugerencias")

            val productosAgotados = _itemsLista.value
                .mapNotNull { item -> productosCatalogo.value.find { it.id == item.id } }
                .filter { it.stock == 0 }

            Log.d("ListaCompraVM", "Productos agotados en lista: ${productosAgotados.size}")
            productosAgotados.forEach {
                Log.d(
                    "ListaCompraVM",
                    " - ${it.nombre} (stock: ${it.stock})"
                )
            }

            val alternativasAgotados = mutableListOf<Producto>()
            val idsYaEnLista = _itemsLista.value.map { it.id }.toSet()
            val idsProcesados = mutableSetOf<String>()

            for (prodAgotado in productosAgotados) {
                Log.d("ListaCompraVM", "Buscando similares para: ${prodAgotado.nombre}")
                val similares = productosDao.getProductosSimilares(
                    productoReferencia = prodAgotado,
                    limite = 3,
                    excluirIds = idsYaEnLista + idsProcesados
                )
                Log.d("ListaCompraVM", "Similares encontrados: ${similares.size}")
                similares.forEach { Log.d("ListaCompraVM", "   -> ${it.nombre}") }
                alternativasAgotados.addAll(similares)
                idsProcesados.addAll(similares.map { it.id })
            }

            // Combinar listas (primero alternativas por agotado, luego frecuentes)
            val todasSugerencias = (alternativasAgotados + productosFrecuentes).distinctBy { it.id }
            _sugerencias.value = todasSugerencias
            _isLoading.value = false
        }
    }

    fun limpiarSugerencias() {
        _sugerencias.value = emptyList()
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
}