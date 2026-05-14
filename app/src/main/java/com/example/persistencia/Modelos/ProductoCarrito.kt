package com.example.persistencia.Modelos

// Representación de un producto en el carrito de la compra
data class ProductoCarrito(
    val producto: Producto, // ID del producto
    val cantidad: Double    // Cantidad de producto que se ha añadido al carrito
)
