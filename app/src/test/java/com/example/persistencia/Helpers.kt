// app/src/test/java/com/example/persistencia/Helpers.kt
package com.example.persistencia

import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Modelos.ProductoCarrito
import com.example.persistencia.Modelos.Descuento

fun producto(id: String = "test", precio: Double, oferta: String? = null) = Producto(
    id = id,
    nombre = "Producto $id",
    precio = precio,
    oferta = oferta
)

fun itemCarrito(precio: Double, cantidad: Double = 1.0, oferta: String? = null, id: String = "test") =
    ProductoCarrito(producto(id, precio, oferta), cantidad)

fun ofertaSegundaUnidad(codigo: String = "OF1", descuento: Int = 50) = Descuento(
    codigo = codigo,
    formula = mapOf("tipo" to "segunda_unidad", "descuento" to descuento)
)

fun ofertaNxM(codigo: String = "OF2", n: Int = 3, m: Int = 2) = Descuento(
    codigo = codigo,
    formula = mapOf("tipo" to "n_por_m", "n" to n, "m" to m)
)

// Añadido tipo = "fijo" para compatibilidad con CheckoutHelper.calcularTicket
fun cuponFijo(codigo: String = "C1", valor: Int, minimo: Int = 0) = Descuento(
    codigo = codigo,
    tipo = "fijo",
    formula = mapOf("tipo" to "fijo", "valor" to valor, "minimo" to minimo)
)

// Añadido tipo = "porcentaje"
fun cuponPorcentaje(codigo: String = "C2", valor: Int, minimo: Int = 0) = Descuento(
    codigo = codigo,
    tipo = "porcentaje",
    formula = mapOf("tipo" to "porcentaje", "valor" to valor, "minimo" to minimo)
)

// Añadido tipo = "maximo"
fun cuponMaximo(codigo: String = "C3", max: Double) = Descuento(
    codigo = codigo,
    tipo = "maximo",
    formula = mapOf("tipo" to "maximo"),
    max_descuento = max
)