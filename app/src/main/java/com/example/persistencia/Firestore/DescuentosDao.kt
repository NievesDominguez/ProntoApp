package com.example.persistencia.Firestore

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.persistencia.Modelos.Descuento
import com.example.persistencia.Modelos.semanaProxima
import com.example.persistencia.Modelos.estaVigente
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class DescuentosDao {

    private val db = FirebaseFirestore.getInstance()

    // Devuelve las ofertas vigentes (filtradas por fecha)
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getOfertas(): List<Descuento> {
        return try {
            val snap = db.collection("descuentos")
                .whereEqualTo("tipo", "oferta")
                .get()
                .await()

            snap.documents.mapNotNull { doc ->
                doc.toObject(Descuento::class.java)?.copy(codigo = doc.id)
            }.filter { it.estaVigente() }

        } catch (e: Exception) {
            emptyList()
        }
    }

    // Devuelve los cupones vigentes (filtrados por fecha)
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getCupones(): List<Descuento> {
        return try {
            val snapshot = Firebase.firestore
                .collection("descuentos")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Descuento::class.java)?.copy(codigo = doc.id)
            }.filter { it.estaVigente() }

        } catch (e: Exception) {
            emptyList()
        }
    }

    // Devuelve los cupones cuya fecha_inicio cae en la próxima semana natural
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getCuponesSemanaProxima(): List<Descuento> {
        return try {
            val snapshot = Firebase.firestore
                .collection("descuentos")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Descuento::class.java)?.copy(codigo = doc.id)
            }.filter { it.semanaProxima() }

        } catch (e: Exception) {
            emptyList()
        }
    }
}