package com.example.persistencia.Modelos

import com.google.firebase.Timestamp

data class Ticket(
    val id: String = "",                                    // ID del ticket
    val fecha: Timestamp = Timestamp.now(),                 // Fecha de compra
    val productos: List<ProductoTicket> = emptyList(),      // Lista de productos comprados
    val descuentos: List<DescuentoTicket> = emptyList(),    // Lista de descuentos aplicados
    val total: Double = 0.0,                                // Precio total de la compra
    val metodoPago: String = "Stripe"                       // Método de pago
)

// Estos datos se guardan por si los datos de este producto o descuento cambiasen en el futuro
data class ProductoTicket(
    val productoId: String = "",    // ID del producto
    val nombre: String = "",        // Nombre del producto
    val cantidad: Double = 0.0,     // Cantidad de producto comprada
    val unidad: String = "ud",      // Unidad de medida del producto
    val subtotal: Double = 0.0,     // Total del precio del producto
    val iva: Int? = null            // Porcentaje de IVA del producto
)

data class DescuentoTicket(
    val codigo: String = "",                // ID del descuento
    val nombre: String? = null,             // Nombre del descuento
    val tipo: String = "",                  // Tipo de descuento
    val descuentoAplicado: Double = 0.0     // Precio descontado por el descuento
)