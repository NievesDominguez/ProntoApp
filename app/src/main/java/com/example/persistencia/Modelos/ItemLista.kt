package com.example.persistencia.Modelos

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ItemLista(
    val id: String = "",           // ID del documento = Código del producto
    val cantidad: Int = 1,
    val comprado: Boolean = false
)