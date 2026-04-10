package com.example.persistencia.Modelos

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.serialization.Serializable

// Modelo de datos para un producto del catálogo
@IgnoreExtraProperties // Para que ignore los campos que sobren (hasta que los añada)
data class Producto(
    val id: String = "",                                    // Código de barras (ID del documento en Firestore)
    val nombre: String = "",                                // Nombre del producto
    val descripcion: String = "",                           // Descripción del producto
    val precio: Double = 0.0,                               // Precio en euros
    val categoria: String = "",                             // Categoría general (Alimentación, Textil…)
    val subcategoria: String = "",                          // Subcategoría (Refrescos, Camisetas…)
    val stock: Int = 0,                                     // Unidades en stock
    val imagenUrl: String = "",                             // URL de la imagen del producto
    val oferta: String? = null,                             // ID de oferta opcional
    val fecha: Timestamp? = null,                           // Fecha de subida (timestamp)
    val cantidad: Double? = 1.toDouble(),                   // Cantidad de producto
    val unidad: String? = "ud",                             // Unidad de medida para la cantidad de producto
    val alergenos_contiene: List<String>? = emptyList(),    // Lista de alérgenos que contiene el producto
    val alergenos_trazas: List<String>? = emptyList(),      // Lista de trazas de alérgenos que puede contener
    val al_peso: Boolean? = false                           // Indica si es un producto cuyo precio depende del peso
)