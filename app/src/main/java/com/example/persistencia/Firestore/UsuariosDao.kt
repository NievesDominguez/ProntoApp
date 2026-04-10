package com.example.persistencia.Firestore

import com.example.persistencia.Modelos.Descuento
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await


class UsuariosDao {
    // Obtiene la preferencia de orden guardada en el perfil del usuario
    suspend fun getOrdenLista(): String {
        val uid = Firebase.auth.currentUser?.uid ?: return "fecha"
        return try {
            val doc = Firebase.firestore.collection("usuarios").document(uid).get().await()
            doc.getString("preferencia_orden") ?: "fecha"
        } catch (e: Exception) {
            "fecha"
        }
    }

    // Guarda la preferencia de orden en el perfil del usuario
    suspend fun setOrdenLista(orden: String) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        try {
            Firebase.firestore.collection("usuarios").document(uid)
                .update("preferencia_orden", orden).await()
        } catch (e: Exception) {
        }
    }

    suspend fun getCupones(): List<String> {
        val user = Firebase.auth.currentUser ?: return emptyList()
        val uid = user.uid

        return try {
            val doc = Firebase.firestore
                .collection("usuarios")
                .document(uid)
                .get()
                .await()

            // El campo "cupones" es un array de Strings
            doc.get("cupones") as? List<String> ?: emptyList()

        } catch (e: Exception) {
            emptyList()
        }
    }

}