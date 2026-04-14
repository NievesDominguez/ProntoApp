package com.example.persistencia.Firestore

import com.example.persistencia.Modelos.Producto
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class CarritoDao {

    // Obtener todos los productos del carrito del usuario actual
    suspend fun getCarrito(): List<Pair<String, Double>> {
        val user = Firebase.auth.currentUser ?: return emptyList()
        val uid = user.uid

        val snapshot = Firebase.firestore
            .collection("carrito")
            .document(uid)
            .collection("productos")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            // Ahora esperamos un Double en Firestore
            val cantidad = doc.getDouble("cantidad") ?: return@mapNotNull null
            Pair(doc.id, cantidad)
        }
    }

    // Añadir o quitar productos del carrito (cantidad en Double)
    suspend fun addCarrito(producto: Producto, cantidad: Double): Boolean {
        val user = Firebase.auth.currentUser ?: return false
        val uid = user.uid

        val carritoRef = Firebase.firestore
            .collection("carrito")
            .document(uid)
            .collection("productos")
            .document(producto.id)

        return try {
            Firebase.firestore.runTransaction { transaction ->
                val snapshot = transaction.get(carritoRef)
                val cantidadActual = if (snapshot.exists()) {
                    snapshot.getDouble("cantidad") ?: 0.0
                } else 0.0

                val nuevaCantidad = cantidadActual + cantidad

                // Permitimos cantidades negativas para reducir, pero no por debajo de 0
                if (nuevaCantidad < 0.0) {
                    throw IllegalArgumentException("Cantidad negativa no permitida")
                }

                if (nuevaCantidad == 0.0) {
                    transaction.delete(carritoRef)
                } else {
                    val datos = mapOf("cantidad" to nuevaCantidad)
                    transaction.set(carritoRef, datos)
                }
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Devuelve todos los cupones activos en el carrito
    suspend fun getCupones(): List<String> {
        val user = Firebase.auth.currentUser ?: return emptyList()
        val uid = user.uid

        return try {
            val snapshot = Firebase.firestore
                .collection("carrito")
                .document(uid)
                .collection("cupones")
                .get()
                .await()

            snapshot.documents.map { it.id }   // El ID del doc es el código del cupón

        } catch (e: Exception) {
            emptyList()
        }
    }

    // Activar cupón
    suspend fun activarCupon(codigo: String) {

        val user = Firebase.auth.currentUser ?: return
        val uid = user.uid

        val cuponRef = Firebase.firestore
            .collection("carrito")
            .document(uid)
            .collection("cupones")
            .document(codigo)

        // Guardamos un campo activo por claridad, aunque no es necesario
        cuponRef.set(mapOf("activo" to true)).await()
    }

    // Desactivar un cupón
    suspend fun desactivarCupon(codigo: String) {

        val user = Firebase.auth.currentUser ?: return
        val uid = user.uid

        val cuponRef = Firebase.firestore
            .collection("carrito")
            .document(uid)
            .collection("cupones")
            .document(codigo)

        // Se elimina el documento con el ID de ese cupón
        cuponRef.delete().await()
    }

    // Comprobar si un cupón dado está activo en el carrito
    suspend fun comprobarCupon(codigo: String?): Boolean {

        val user = Firebase.auth.currentUser ?: return false
        val uid = user.uid

        val cuponRef = Firebase.firestore
            .collection("carrito")
            .document(uid)
            .collection("cupones")
            .document(codigo ?: return false)

        // Devuelve true si se encuentra el código en el carrito
        return try {
            val snapshot = cuponRef.get().await()
            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    // Vacía el carrito de cupones y productos
    suspend fun vaciarCarrito() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val productosRef = Firebase.firestore.collection("carrito").document(uid).collection("productos")
        val cuponesRef = Firebase.firestore.collection("carrito").document(uid).collection("cupones")

        try {
            val productosSnap = productosRef.get().await()
            for (doc in productosSnap.documents) {
                doc.reference.delete().await()
            }
            val cuponesSnap = cuponesRef.get().await()
            for (doc in cuponesSnap.documents) {
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


