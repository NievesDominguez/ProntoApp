package com.example.persistencia.Firestore

import com.example.persistencia.Modelos.Producto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

// Repositorio encargado de obtener los productos desde Firestore.
// Devuelve un Flow para que Compose pueda reaccionar a los cambios en tiempo real.
class RepositorioProductos {

    private val db = FirebaseFirestore.getInstance()

    // Devuelve un flujo de productos que se actualiza automáticamente
    fun obtenerProductosFlow(): Flow<List<Producto>> = callbackFlow {

        // Listener en tiempo real sobre la colección "productos"
        val listener = db.collection("productos")
            .addSnapshotListener { snapshot, error ->

                // Si hay error, cerramos el flujo
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                // Convertimos cada documento en un ProductoModelo
                val productos = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Producto::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                // Enviamos la lista al flujo
                trySend(productos)
            }

        // Cuando el flujo se cierra, eliminamos el listener
        awaitClose { listener.remove() }
    }
}
