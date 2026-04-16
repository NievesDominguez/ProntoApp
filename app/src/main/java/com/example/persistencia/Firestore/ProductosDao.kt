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
        limite: Int = 3,
        excluirIds: Set<String> = emptySet()
    ): List<Producto> {
        val palabras = extraerPalabrasClave(productoReferencia.nombre)
        if (palabras.isEmpty()) return emptyList()

        // Obtener productos candidatos: con stock > 0 y que no estén excluidos
        val todos = getTodos().filter {
            it.stock > 0 &&
                    it.id != productoReferencia.id &&
                    it.id !in excluirIds
        }

        // Puntuar cada producto por coincidencia de palabras clave
        val puntuados = todos.map { prod ->
            val palabrasProd = extraerPalabrasClave(prod.nombre)
            val coincidencias = palabrasProd.count { it in palabras }
            prod to coincidencias
        }.filter { it.second > 0 }

        // Ordenar por coincidencias y luego por cercanía de categoría
        return puntuados
            .sortedWith(
                compareByDescending<Pair<Producto, Int>> { it.second }
                    .thenBy { if (it.first.categoria == productoReferencia.categoria) 0 else 1 }
                    .thenBy { if (it.first.subcategoria == productoReferencia.subcategoria) 0 else 1 }
            )
            .take(limite)
            .map { it.first }
    }

    //Extrae palabras clave del nombre de un producto
    fun extraerPalabrasClave(texto: String): List<String> {
        // Palabras a ignorar
        val stopWords = setOf(
            "de", "la", "el", "los", "las", "un", "una", "y", "con", "sin",
            "para", "por", "en", "a", "ante", "bajo", "cabe", "contra", "desde",
            "durante", "entre", "hacia", "hasta", "mediante", "según", "sobre", "tras"
        )
        return texto.lowercase()
            .split(Regex("[^a-zA-ZáéíóúüñÁÉÍÓÚÜÑ]"))  // separar por caracteres no alfabéticos
            .filter { it.length > 2 && it !in stopWords }
            .distinct()
    }

}

