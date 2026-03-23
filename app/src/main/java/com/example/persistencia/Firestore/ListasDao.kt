package com.example.persistencia.Firestore

import com.example.persistencia.Modelos.ItemLista
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class ListasDao {
    private val db = Firebase.firestore
    private fun getUid() = Firebase.auth.currentUser?.uid

    private fun getItemsRef() = getUid()?.let {
        db.collection("listas").document(it).collection("items")
    }

    suspend fun getLista(): List<ItemLista> {
        val ref = getItemsRef() ?: return emptyList()
        return try {
            val snapshot = ref.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ItemLista::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) { emptyList() }
    }

    // Ahora recibe el ID (código) del producto directamente
    suspend fun agregarOIncrementar(idProducto: String, cantidad: Int = 1) {
        val ref = getItemsRef()?.document(idProducto) ?: return
        try {
            val doc = ref.get().await()
            if (doc.exists()) {
                val nuevaCant = (doc.getLong("cantidad") ?: 0).toInt() + cantidad
                ref.update("cantidad", nuevaCant).await()
            } else {
                // Si no existe, creamos el documento con el ID del producto
                ref.set(ItemLista(id = idProducto, cantidad = cantidad, comprado = false)).await()
            }
        } catch (e: Exception) { }
    }

    suspend fun eliminarItem(id: String) {
        getItemsRef()?.document(id)?.delete()?.await()
    }

    suspend fun cambiarEstadoComprado(id: String, estado: Boolean) {
        getItemsRef()?.document(id)?.update("comprado", estado)?.await()
    }

    suspend fun actualizarCantidad(id: String, nuevaCantidad: Int) {
        if (nuevaCantidad > 0) {
            getItemsRef()?.document(id)?.update("cantidad", nuevaCantidad)?.await()
        } else {
            eliminarItem(id)
        }
    }
}