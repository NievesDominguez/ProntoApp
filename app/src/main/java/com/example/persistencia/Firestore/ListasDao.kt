package com.example.persistencia.Firestore

import com.example.persistencia.Modelos.ItemLista
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class ListasDao {
    private val db = Firebase.firestore
    private fun getUid() = Firebase.auth.currentUser?.uid

    // Centralizamos la referencia para no cometer errores de escritura
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

    suspend fun addItem(idProducto: String, cantidad: Int = 1) {
        val ref = getItemsRef()?.document(idProducto) ?: return
        try {
            val doc = ref.get().await()
            if (doc.exists()) {
                val nuevaCant = (doc.getLong("cantidad") ?: 0).toInt() + cantidad
                ref.update("cantidad", nuevaCant).await()
            } else {
                ref.set(ItemLista(id = idProducto, cantidad = cantidad, comprado = false)).await()
            }
        } catch (e: Exception) { }
    }

    suspend fun eliminarItem(id: String) {
        getItemsRef()?.document(id)?.delete()?.await()
    }

    suspend fun cambiarEstado(id: String, estado: Boolean) {
        getItemsRef()?.document(id)?.update("comprado", estado)?.await()
    }

    suspend fun actualizarCantidad(id: String, nuevaCantidad: Int) {
        if (nuevaCantidad > 0) {
            getItemsRef()?.document(id)?.update("cantidad", nuevaCantidad)?.await()
        } else {
            eliminarItem(id)
        }
    }

    // Marca todos los productos como no comprados
    suspend fun desmarcarTodo() {
        val ref = getItemsRef() ?: return
        try {
            val snapshot = ref.get().await()
            db.runBatch { batch ->
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "comprado", false)
                }
            }.await()
        } catch (e: Exception) { }
    }

    // Borra todos los productos de la lista
    suspend fun eliminarTodo() {
        val ref = getItemsRef() ?: return
        try {
            val snapshot = ref.get().await()
            db.runBatch { batch ->
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
            }.await()
        } catch (e: Exception) { }
    }

}