package com.example.persistencia.Firestore

import com.example.persistencia.Modelos.Producto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await


class ProductosDao {

    // Instancia de Firestore
    private val db = FirebaseFirestore.getInstance()

    // Referencia directa a la colección "productos"
    // Evita repetir db.collection("productos") en cada función
    private val coleccion = db.collection("productos")


    //Obtener todos los productos
    suspend fun getTodos(): List<Producto> {
        return try {
            val snapshot = coleccion.get().await() // Ejecuta la consulta y espera el resultado

            // Si no es nulo, convierte cada documento en Producto
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Producto::class.java)?.copy(id = doc.id)
            }

        } catch (e: Exception) {
            // Si Firestore falla, devolvemos lista vacía para evitar crasheos
            emptyList()
        }
    }


    // Obtiene un producto concreto por su ID
    suspend fun getProducto(id: String): Producto? {
        return try {
            val doc = coleccion.document(id).get().await() // Lee un único documento
            doc.toObject(Producto::class.java)?.copy(id = doc.id)

            // Si no existe, devuelve null.
        } catch (e: Exception) {
            null
        }
    }


    // Obtiene productos filtrados por categoría
    suspend fun getPorCategoria(categoria: String): List<Producto> {
        return try {
            val snapshot = coleccion
                // Filtra en Firestore antes de traer los datos, es más eficiente que filtrar en memoria
                .whereEqualTo("categoria", categoria)
                // Se ordena en Firestore por subcategoria
                .orderBy("subcategoria", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Producto::class.java)?.copy(id = doc.id)
            }

        } catch (e: Exception) {
            emptyList()
        }
    }


    // Obtiene productos ordenados por precio ascendente
    suspend fun getOrdenPrecioAsc(): List<Producto> {
        return try {
            val snapshot = coleccion
                .orderBy("precio", Query.Direction.ASCENDING) // Se ordena en Firestore
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Producto::class.java)?.copy(id = doc.id)
            }

            // Si no se encuentra nada, devuelve lista vacía
        } catch (e: Exception) {
            emptyList()
        }
    }


    // Obtiene productos ordenados por precio descendente
    suspend fun getOrdenPrecioDesc(): List<Producto> {
        return try {
            val snapshot = coleccion
                .orderBy("precio", Query.Direction.DESCENDING) // Se ordena en Firestore
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Producto::class.java)?.copy(id = doc.id)
            }

            // Si no se encuentra nada, devuelve lista vacía
        } catch (e: Exception) {
            emptyList()
        }
    }
}
