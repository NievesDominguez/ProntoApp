package com.example.persistencia.Modelos

// Modelo de datos para un producto del catálogo
data class Producto(
    val id: String = "",            // Código de barras (ID del documento en Firestore)
    val nombre: String = "",        // Nombre del producto
    val descripcion: String = "",   // Descripción del producto
    val precio: Double = 0.0,       // Precio en euros
    val categoria: String = "",     // Categoría general (Alimentación, Textil…)
    val subcategoria: String = "",  // Subcategoría (Refrescos, Camisetas…)
    val stock: Int = 0,             // Unidades en stock
    val imagenUrl: String = "",     // URL de la imagen del producto
    val oferta: String? = null,     // ID de oferta opcional
    val fecha: Long = 0L            // Fecha de subida (timestamp)
) {

    // Conversión opcional a Map para subir a Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "precio" to precio,
            "categoria" to categoria,
            "subcategoria" to subcategoria,
            "stock" to stock,
            "imagenUrl" to imagenUrl,
            "oferta" to oferta,
            "fecha" to fecha
        )
    }
}
