package com.example.persistencia.Firestore

import com.example.persistencia.Modelos.Descuento
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class DescuentosDao {

    private val db = FirebaseFirestore.getInstance()

    // Devuelve las ofertas activas
    suspend fun getOfertas(): List<Descuento> {
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

    // Devuelve los cupones activos
    suspend fun getCupones(): List<Descuento> {
        return try {
            val snapshot = Firebase.firestore
                .collection("descuentos")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Descuento::class.java)?.copy(codigo = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

}