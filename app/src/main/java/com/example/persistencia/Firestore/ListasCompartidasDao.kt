package com.example.persistencia.Firestore

import com.example.persistencia.Modelos.ListaCompartida
import com.example.persistencia.Modelos.ProductoLista
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class ListasCompartidasDao {
    private val db = Firebase.firestore

    // Referencia a la colección principal
    private fun listasRef() = db.collection("listas_compartidas")

    // Subcolección de items dentro de una lista
    private fun itemsRef(listId: String) = listasRef().document(listId).collection("items")

    // =========== CRUD de listas ===========

    // Crear una nueva lista. Devuelve el ID generado.
    suspend fun crearLista(nombre: String, ownerId: String): String? {
        val data = mapOf(
            "nombre" to nombre,
            "ownerId" to ownerId,
            "miembros" to listOf(ownerId),
            "invitados" to emptyList<String>(),
            "creado" to FieldValue.serverTimestamp()
        )
        return try {
            val docRef = listasRef().add(data).await()
            docRef.id
        } catch (e: Exception) {
            null
        }
    }

    // Obtener todas las listas en las que el usuario es miembro
    suspend fun getListasPorMiembro(userId: String): List<ListaCompartida> {
        return try {
            val snapshot = listasRef()
                .whereArrayContains("miembros", userId)
                .get()  // sin orderBy
                .await()
            val listas = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ListaCompartida::class.java)?.copy(id = doc.id)
            }
            // Ordenar por creado descendente en memoria (más reciente primero)
            listas.sortedByDescending { it.creado?.toDate()?.time ?: 0L }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Obtener listas donde el usuario está invitado (aún no aceptó)
    suspend fun getListasInvitado(userId: String): List<ListaCompartida> {
        return try {
            val snapshot = listasRef()
                .whereArrayContains("invitados", userId)
                .get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ListaCompartida::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Obtener una lista por su ID
    suspend fun getListaById(listId: String): ListaCompartida? {
        return try {
            val doc = listasRef().document(listId).get().await()
            doc.toObject(ListaCompartida::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    // Invitar a un usuario (agrega su UID al array invitados)
    suspend fun invitarUsuario(listId: String, userId: String) {
        listasRef().document(listId)
            .update("invitados", FieldValue.arrayUnion(userId)).await()
    }

    // Aceptar invitación: mueve el UID de invitados a miembros
    suspend fun aceptarInvitacion(listId: String, userId: String) {
        val docRef = listasRef().document(listId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val miembros = snapshot.get("miembros") as? MutableList<String> ?: mutableListOf()
            val invitados = snapshot.get("invitados") as? MutableList<String> ?: mutableListOf()
            if (invitados.contains(userId)) {
                miembros.add(userId)
                invitados.remove(userId)
                transaction.update(
                    docRef, mapOf(
                        "miembros" to miembros,
                        "invitados" to invitados
                    )
                )
            }
        }.await()
    }

    // Rechazar invitación: elimina el UID de invitados
    suspend fun rechazarInvitacion(listId: String, userId: String) {
        listasRef().document(listId)
            .update("invitados", FieldValue.arrayRemove(userId)).await()
    }

    // Eliminar una lista completa (solo el owner)
    suspend fun eliminarLista(listId: String) {
        // Primero eliminamos todos los items (en batch)
        val itemsSnapshot = itemsRef(listId).get().await()
        db.runBatch { batch ->
            for (doc in itemsSnapshot.documents) {
                batch.delete(doc.reference)
            }
        }.await()
        // Luego eliminamos el documento principal
        listasRef().document(listId).delete().await()
    }

    // =========== CRUD de items (dentro de una lista) ===========

    suspend fun getItems(listId: String): List<ProductoLista> {
        return try {
            val snapshot = itemsRef(listId).get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ProductoLista::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addItem(listId: String, productoId: String, cantidad: Double = 1.0) {
        val ref = itemsRef(listId).document(productoId)
        try {
            val doc = ref.get().await()
            if (doc.exists()) {
                val nuevaCant = (doc.getDouble("cantidad") ?: 0.0) + cantidad
                ref.update("cantidad", nuevaCant).await()
            } else {
                val nuevo = ProductoLista(id = productoId, cantidad = cantidad, comprado = false)
                ref.set(nuevo).await()
            }
        } catch (e: Exception) {
        }
    }

    suspend fun eliminarItem(listId: String, productoId: String) {
        itemsRef(listId).document(productoId).delete().await()
    }

    suspend fun cambiarEstado(listId: String, productoId: String, estado: Boolean) {
        itemsRef(listId).document(productoId).update("comprado", estado).await()
    }

    suspend fun actualizarCantidad(listId: String, productoId: String, nuevaCantidad: Double) {
        if (nuevaCantidad <= 0) {
            eliminarItem(listId, productoId)
        } else {
            itemsRef(listId).document(productoId).update("cantidad", nuevaCantidad).await()
        }
    }

    // Desmarcar todos los items como no comprados
    suspend fun desmarcarTodo(listId: String) {
        val snapshot = itemsRef(listId).get().await()
        db.runBatch { batch ->
            for (doc in snapshot.documents) {
                batch.update(doc.reference, "comprado", false)
            }
        }.await()
    }

    // Eliminar todos los items de la lista
    suspend fun eliminarTodoItems(listId: String) {
        val snapshot = itemsRef(listId).get().await()
        db.runBatch { batch ->
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
        }.await()
    }
}