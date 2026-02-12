package com.example.persistencia.Modelos

// Representación de un producto en el carrito de la compra
data class ProductoCarrito(
    val producto: Producto, // El producto en sí
    val cantidad: Int // La cantidad que se ha añadido
)
