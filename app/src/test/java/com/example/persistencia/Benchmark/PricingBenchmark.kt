package com.example.persistencia.Benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.persistencia.Herramientas.segundaUnidad
import com.example.persistencia.Herramientas.aplicarNxM
import com.example.persistencia.Herramientas.calcularTotalCarrito
import com.example.persistencia.Herramientas.CheckoutHelper
import com.example.persistencia.Modelos.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PricingBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    // Helpers para crear datos de prueba
    private fun producto(id: String = "test", precio: Double, oferta: String? = null) =
        Producto(id = id, nombre = "Producto $id", precio = precio, oferta = oferta)

    private fun item(precio: Double, cantidad: Double = 1.0, oferta: String? = null, id: String = "test") =
        ProductoCarrito(producto(id, precio, oferta), cantidad)

    private fun ofertaSU(codigo: String = "OF1", descuento: Int = 50) =
        Descuento(codigo = codigo, formula = mapOf("tipo" to "segunda_unidad", "descuento" to descuento))

    private fun ofertaNM(codigo: String = "OF2", n: Int = 3, m: Int = 2) =
        Descuento(codigo = codigo, formula = mapOf("tipo" to "n_por_m", "n" to n, "m" to m))

    // Benchmark: segundaUnidad con 2 items
    @Test
    fun benchmarkSegundaUnidad_2items() {
        val items = listOf(item(precio = 10.0, cantidad = 2.0))
        val oferta = ofertaSU(descuento = 50)

        benchmarkRule.measureRepeated {
            segundaUnidad(items, oferta)
        }
    }

    // Benchmark: segundaUnidad con 10 items
    @Test
    fun benchmarkSegundaUnidad_10items() {
        val items = listOf(item(precio = 10.0, cantidad = 10.0))
        val oferta = ofertaSU(descuento = 50)

        benchmarkRule.measureRepeated {
            segundaUnidad(items, oferta)
        }
    }

    // Benchmark: segundaUnidad con 50 items
    @Test
    fun benchmarkSegundaUnidad_50items() {
        val items = listOf(item(precio = 10.0, cantidad = 50.0))
        val oferta = ofertaSU(descuento = 50)

        benchmarkRule.measureRepeated {
            segundaUnidad(items, oferta)
        }
    }

    // Benchmark: aplicarNxM con 3 items (3x2)
    @Test
    fun benchmarkAplicarNxM_3items() {
        val items = listOf(item(precio = 6.0, cantidad = 3.0))
        val oferta = ofertaNM(n = 3, m = 2)

        benchmarkRule.measureRepeated {
            aplicarNxM(items, oferta)
        }
    }

    // Benchmark: aplicarNxM con 15 items
    @Test
    fun benchmarkAplicarNxM_15items() {
        val items = listOf(item(precio = 6.0, cantidad = 15.0))
        val oferta = ofertaNM(n = 3, m = 2)

        benchmarkRule.measureRepeated {
            aplicarNxM(items, oferta)
        }
    }

    // Benchmark: aplicarNxM con 75 items
    @Test
    fun benchmarkAplicarNxM_75items() {
        val items = listOf(item(precio = 6.0, cantidad = 75.0))
        val oferta = ofertaNM(n = 3, m = 2)

        benchmarkRule.measureRepeated {
            aplicarNxM(items, oferta)
        }
    }

    // Benchmark: calcularTotalCarrito con 5 productos
    @Test
    fun benchmarkCalcularTotalCarrito_5productos() {
        val carrito = listOf(
            item(precio = 10.0, cantidad = 2.0, oferta = "OF1", id = "A"),
            item(precio = 5.0, cantidad = 1.0, oferta = "OF1", id = "B"),
            item(precio = 8.0, cantidad = 3.0, id = "C"),
            item(precio = 12.0, cantidad = 1.0, id = "D"),
            item(precio = 3.0, cantidad = 5.0, id = "E")
        )
        val ofertas = listOf(ofertaSU(descuento = 50))
        val cupones = emptyList<Descuento>()

        benchmarkRule.measureRepeated {
            calcularTotalCarrito(carrito, ofertas, cupones)
        }
    }

    // Benchmark: calcularTotalCarrito con 20 productos
    @Test
    fun benchmarkCalcularTotalCarrito_20productos() {
        val carrito = (1..20).map { i ->
            item(precio = (i * 2.0), cantidad = 1.0, oferta = if (i % 2 == 0) "OF1" else null, id = "P$i")
        }
        val ofertas = listOf(ofertaSU(descuento = 50))
        val cupones = emptyList<Descuento>()

        benchmarkRule.measureRepeated {
            calcularTotalCarrito(carrito, ofertas, cupones)
        }
    }

    // Benchmark: calcularTotalCarrito con 100 productos
    @Test
    fun benchmarkCalcularTotalCarrito_100productos() {
        val carrito = (1..100).map { i ->
            item(precio = (i * 2.0), cantidad = 1.0, oferta = if (i % 2 == 0) "OF1" else null, id = "P$i")
        }
        val ofertas = listOf(ofertaSU(descuento = 50))
        val cupones = emptyList<Descuento>()

        benchmarkRule.measureRepeated {
            calcularTotalCarrito(carrito, ofertas, cupones)
        }
    }

    // Benchmark: CheckoutHelper.calcularTicket con ofertas y cupones combinados
    @Test
    fun benchmarkCalcularTicket_conOfertasYCupones() {
        val productos = listOf(
            item(precio = 10.0, cantidad = 2.0, oferta = "OF1", id = "A"),
            item(precio = 5.0, cantidad = 3.0, oferta = "OF2", id = "B"),
            item(precio = 8.0, cantidad = 1.0, id = "C")
        )
        val ofertas = listOf(
            ofertaSU(codigo = "OF1", descuento = 50),
            ofertaNM(codigo = "OF2", n = 3, m = 2)
        )
        val cupones = listOf(
            Descuento(
                codigo = "C1",
                tipo = "porcentaje",
                formula = mapOf("tipo" to "porcentaje", "minimo" to 0, "valor" to 10)
            )
        )

        benchmarkRule.measureRepeated {
            CheckoutHelper.calcularTicket(productos, ofertas, cupones)
        }
    }
}