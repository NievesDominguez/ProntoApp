package com.example.persistencia.Modelos

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ProductoLista(
    val id: String = "",           // ID del documento = Código del producto
    val cantidad: Double = 1.0,
    val comprado: Boolean = false
)