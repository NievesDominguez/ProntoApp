package com.example.persistencia.Firestore

import com.example.persistencia.Modelos.Producto
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class CarritoDao {

    // Obtener todos los productos del carrito del usuario actual
    suspend fun getCarrito(): List<Pair<String, Int>> {
        // Obtener el usuario actual. Si no hay usuario, devuelve una lista vacía
        val user = Firebase.auth.currentUser ?: return emptyList()
        val uid = user.uid // ID del usuario

        // Obtener los productos del carrito del usuario
        val snapshot = Firebase.firestore
            .collection("carrito")
            .document(uid)
            .collection("productos")
            .get()
            .await()

        // Devuelve una lista de pares (idProducto, cantidad)
        return snapshot.documents.mapNotNull { doc ->
            // Si no hay cantidad, devuelve null
            val cantidad = doc.getLong("cantidad")?.toInt() ?: return@mapNotNull null
            Pair(doc.id, cantidad) // idProducto, cantidad
        }
    }


    // Añadir o quitar productos del carrito
    suspend fun addCarrito(producto: Producto, cantidad: Int): Boolean {
        // Obtener el usuario actual
        val user = Firebase.auth.currentUser ?: return false
        val uid = user.uid

        // Referencia al documento del producto en el carrito
        val carritoRef = Firebase.firestore
            .collection("carrito")
            .document(uid)
            .collection("productos")
            .document(producto.id)

        return try {
            Firebase.firestore.runTransaction { transaction ->

                // Obtener el documento actual del producto en el carrito
                val snapshot = transaction.get(carritoRef)

                // Cantidad actual (0 si no existe)
                val cantidadActual = if (snapshot.exists()) {
                    snapshot.getLong("cantidad")?.toInt() ?: 0
                } else 0

                // Nueva cantidad después de sumar o restar
                val nuevaCantidad = cantidadActual + cantidad

                // No permitir cantidades negativas
                if (nuevaCantidad < 0) {
                    throw IllegalArgumentException("Cantidad negativa no permitida")
                }

                // Si la cantidad queda en 0, eliminar el producto del carrito
                if (nuevaCantidad == 0) {
                    transaction.delete(carritoRef)
                } else {
                    // Guardar solo la cantidad, como pediste
                    val datos = mapOf(
                        "cantidad" to nuevaCantidad
                    )
                    transaction.set(carritoRef, datos)
                }
            }.await()

            true // Operación exitosa

        } catch (e: Exception) {
            false // Operación fallida

        }
    }


}