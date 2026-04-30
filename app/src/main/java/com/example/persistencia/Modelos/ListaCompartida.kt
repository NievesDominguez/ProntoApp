package com.example.persistencia.Modelos

import com.google.firebase.Timestamp

data class ListaCompartida(
    val id: String = "",
    val nombre: String = "",
    val ownerId: String = "",
    val miembros: List<String> = emptyList(),
    val invitados: List<String> = emptyList(),
    val creado: Timestamp? = null
)