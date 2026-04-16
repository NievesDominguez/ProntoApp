package com.example.persistencia.Firestore

import android.content.ContentValues.TAG
import android.util.Log
import com.example.persistencia.Modelos.Ticket
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class TicketsDao {
    private val db = Firebase.firestore

    private fun getTicketsCollection() =
        Firebase.auth.currentUser?.uid?.let { uid ->
            db.collection("compras").document(uid).collection("tickets")
        }

    suspend fun guardarTicket(ticket: Ticket): String? {
        val collection = getTicketsCollection() ?: return null
        return try {
            val docRef = collection.add(ticket).await()
            docRef.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getTickets(): List<Ticket> {
        val collection = getTicketsCollection()
        if (collection == null) {
            Log.e(TAG, "No se pudo obtener la colección (usuario no autenticado?)")
            return emptyList()
        }
        return try {
            val snapshot = collection
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()
            Log.d(TAG, "Documentos obtenidos: ${snapshot.size()}")
            snapshot.documents.mapNotNull { doc ->
                try {
                    val ticket = doc.toObject(Ticket::class.java)?.copy(id = doc.id)
                    if (ticket == null) {
                        Log.w(TAG, "No se pudo convertir documento ${doc.id}")
                    }
                    ticket
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir documento ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener tickets", e)
            emptyList()
        }
    }

    suspend fun getTicketById(ticketId: String): Ticket? {
        val collection = getTicketsCollection() ?: return null
        return try {
            val doc = collection.document(ticketId).get().await()
            doc.toObject(Ticket::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    // Devuelve los tickets más recientes
    suspend fun getUltimosTickets(limit: Int = 5): List<Ticket> {
        val collection = getTicketsCollection() ?: return emptyList()
        return try {
            val snapshot = collection
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Ticket::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}