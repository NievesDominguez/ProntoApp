package com.example.persistencia.Modelos

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Descuento(
    val codigo: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val tipo: String? = null,
    val ambito: String? = null,
    val formula: Map<String, Any>? = null,
    val fecha_inicio: String? = null,
    val fecha_fin: String? = null,
    val max_descuento: Double? = null
)

