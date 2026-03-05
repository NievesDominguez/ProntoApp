package com.example.persistencia.Firestore

import com.example.persistencia.Modelos.Descuento
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DescuentosDao {

    private val db = FirebaseFirestore.getInstance()
    suspend fun getDescuentos(): List<Descuento> {
        return try {
            val snap = db.collection("descuentos")
                .whereEqualTo("tipo", "oferta")
                .get()
                .await()

            snap.documents.map { doc ->
                val oferta = doc.toObject(Descuento::class.java)
                oferta?.copy(codigo = doc.id) // El ID del documento es el código de la oferta
            }.filterNotNull()

        } catch (e: Exception) {
            emptyList()
        }
    }
}