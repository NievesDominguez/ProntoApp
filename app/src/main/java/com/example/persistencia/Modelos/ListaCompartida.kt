package com.example.persistencia.Modelos

import com.google.firebase.Timestamp

data class ListaCompartida(
    val id: String = "",                        // ID de la lista
    val nombre: String = "",                    // Nombre dado a la lista
    val ownerId: String = "",                   // ID del creador de la lista
    val miembros: List<String> = emptyList(),   // IDs de usuarios que pueden acceder a la lista
    val invitados: List<String> = emptyList(),  // IDs de usuarios invitados a la lista
    val creado: Timestamp? = null               // Fecha de creación de la lista
)