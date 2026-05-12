package com.example.persistencia.Firestore

import com.example.persistencia.Modelos.Producto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlin.compareTo


class ProductosDao {

    // Instancia de Firestore
    private val db = FirebaseFirestore.getInstance()

    // Referencia a la colección "productos"
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

    // Actualiza un producto en Firestore
    suspend fun actualizarProducto(
        id: String,
        nombre: String,
        precio: Double,
        descripcion: String
    ) {
        coleccion
            .document(id)
            .update(
                mapOf(
                    "nombre" to nombre,
                    "precio" to precio,
                    "descripcion" to descripcion
                )
            )
    }

    // Obtener productos similares al dado
    suspend fun getProductosSimilares(
        productoReferencia: Producto,
        limite: Int = 5,
        excluirIds: Set<String> = emptySet()
    ): List<Producto> {
        val todos = getTodos().filter {
            it.stock > 0 &&
                    it.id != productoReferencia.id &&
                    it.id !in excluirIds
        }

        // Buscar por palabras clave
        val palabras = extraerPalabrasClave(productoReferencia.nombre)
        if (palabras.isNotEmpty()) {
            val candidatos = todos.mapNotNull { prod ->
                val palabrasProd = extraerPalabrasClave(prod.nombre)
                val coincidencias = palabrasProd.count { it in palabras }
                if (coincidencias > 0) prod to coincidencias else null
            }
            if (candidatos.isNotEmpty()) {
                return candidatos
                    .sortedWith(
                        compareByDescending<Pair<Producto, Int>> { it.second }
                            .thenBy { if (it.first.categoria == productoReferencia.categoria) 0 else 1 }
                            .thenBy { if (it.first.subcategoria == productoReferencia.subcategoria) 0 else 1 }
                    )
                    .take(limite)
                    .map { it.first }
            }
        }

        // Misma subcategoría
        val mismaSub = todos.filter { it.subcategoria == productoReferencia.subcategoria }
        if (mismaSub.isNotEmpty()) {
            return mismaSub.take(limite)
        }

        // Misma categoría
        return todos.filter { it.categoria == productoReferencia.categoria }.take(limite)
    }

    //Extrae palabras clave del nombre de un producto
    fun extraerPalabrasClave(texto: String): List<String> {
        // Palabras a ignorar
        val stopWords = setOf(
            "de", "la", "el", "los", "las", "un", "una", "y", "con", "sin",
            "para", "por", "en", "a", "ante", "bajo", "cabe", "contra", "desde",
            "durante", "entre", "hacia", "hasta", "mediante", "según", "tras"
        )
        return texto.lowercase()
            .split(Regex("[^a-zA-ZáéíóúüñÁÉÍÓÚÜÑ]"))  // separar por caracteres no alfabéticos
            .filter { it.length > 2 && it !in stopWords }
            .distinct()
    }

    // Obtiene los productos más recientes ordenados por fecha
    suspend fun getProductosRecientes(limit: Int = 8): List<Producto> {
        return try {
            val snapshot = coleccion
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Producto::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

}

