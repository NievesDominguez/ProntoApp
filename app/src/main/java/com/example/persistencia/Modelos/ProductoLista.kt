package com.example.persistencia.Modelos

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ProductoLista(
    val id: String = "",            // ID del producto añadido a la lista
    val cantidad: Double = 1.0,     // Cantidad de producto añadido a la lista
    val comprado: Boolean = false   // Si se ha marcado como comprado o no
)