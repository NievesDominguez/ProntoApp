package com.example.persistencia.Firestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persistencia.Modelos.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel encargado de gestionar el estado del catálogo.
// Se conecta al repositorio y expone los productos a la UI mediante StateFlow.
class CatalogoVistaModelo : ViewModel() {

    private val repositorio = RepositorioProductos()

    // Estado interno mutable
    private val _productos = MutableStateFlow<List<Producto>>(emptyList())

    // Estado expuesto a la UI (inmutable)
    val productos = _productos.asStateFlow()

    init {
        // Al iniciar el ViewModel, empezamos a escuchar los cambios en Firestore
        viewModelScope.launch {
            repositorio.obtenerProductosFlow().collect { lista ->
                _productos.value = lista
            }
        }
    }
}

