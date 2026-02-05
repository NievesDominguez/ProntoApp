package com.example.persistencia.Firestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persistencia.Modelos.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CatalogoVistaModelo : ViewModel() {

    private val dao = ProductosDao()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos

    init {
        cargarProductos()
    }

    // Carga de productos (todos)
    fun cargarProductos() {
        viewModelScope.launch {
            _productos.value = dao.getTodos()
        }
    }

    // Cargar productos por categoría
    fun cargarPorCategoria(categoria: String) {
        viewModelScope.launch {
            _productos.value = dao.getPorCategoria(categoria)
        }
    }

    // Orden ascendente (todos)
    fun ordenarAsc() {
        viewModelScope.launch {
            _productos.value = dao.getOrdenPrecioAsc()
        }
    }

    // Orden descendente (todos)
    fun ordenarDesc() {
        viewModelScope.launch {
            _productos.value = dao.getOrdenPrecioDesc()
        }
    }

    // Obtener un producto concreto por ID
    suspend fun getProducto(id: String): Producto? {
        return dao.getProducto(id)
    }
}
