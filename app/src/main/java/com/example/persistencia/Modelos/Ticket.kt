package com.example.persistencia.Modelos

import com.google.firebase.Timestamp

data class Ticket(
    val id: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val productos: List<ProductoTicket> = emptyList(),
    val descuentos: List<DescuentoTicket> = emptyList(),
    val total: Double = 0.0,
    val metodoPago: String = "Stripe"
)

data class ProductoTicket(
    val productoId: String = "",
    val nombre: String = "",
    val cantidad: Double = 0.0,
    val unidad: String = "ud",
    val subtotal: Double = 0.0
)

data class DescuentoTicket(
    val codigo: String = "",
    val nombre: String? = null,
    val tipo: String = "",
    val descuentoAplicado: Double = 0.0
)