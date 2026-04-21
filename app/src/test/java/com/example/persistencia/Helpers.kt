package com.example.persistencia

// Helpers reutilizables en todos los archivos de test
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Modelos.ProductoCarrito
import com.example.persistencia.Modelos.Descuento

// Crea un Producto con solo los campos relevantes para testing
fun producto(id: String = "test", precio: Double, oferta: String? = null) = Producto(
    id = id,
    nombre = "Producto $id",
    precio = precio,
    oferta = oferta
)

// Crea un item de carrito
fun itemCarrito(precio: Double, cantidad: Double = 1.0, oferta: String? = null, id: String = "test") =
    ProductoCarrito(producto(id, precio, oferta), cantidad)

// Crea una oferta de segunda unidad
fun ofertaSegundaUnidad(codigo: String = "OF1", descuento: Int = 50) = Descuento(
    codigo = codigo,
    formula = mapOf("tipo" to "segunda_unidad", "descuento" to descuento)
)

// Crea una oferta NxM
fun ofertaNxM(codigo: String = "OF2", n: Int = 3, m: Int = 2) = Descuento(
    codigo = codigo,
    formula = mapOf("tipo" to "n_por_m", "n" to n, "m" to m)
)

// Crea un cupón fijo
fun cuponFijo(codigo: String = "C1", valor: Int, minimo: Int = 0) = Descuento(
    codigo = codigo,
    formula = mapOf("tipo" to "fijo", "valor" to valor, "minimo" to minimo)
)

// Crea un cupón porcentaje
fun cuponPorcentaje(codigo: String = "C2", valor: Int, minimo: Int = 0) = Descuento(
    codigo = codigo,
    formula = mapOf("tipo" to "porcentaje", "valor" to valor, "minimo" to minimo)
)

// Crea un cupón máximo
fun cuponMaximo(codigo: String = "C3", max: Double) = Descuento(
    codigo = codigo,
    formula = mapOf("tipo" to "maximo"),
    max_descuento = max
)