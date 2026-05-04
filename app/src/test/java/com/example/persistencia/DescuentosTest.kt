package com.example.persistencia

import com.example.persistencia.Modelos.Descuento
import com.example.persistencia.Modelos.estaVigente
import com.example.persistencia.Modelos.semanaProxima
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class DescuentosTest {

    private val fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val hoy = LocalDate.now()

    private fun fecha(date: LocalDate): String = date.format(fmt)

    // Descuento con inicio ayer y fin manana: vigente hoy
    @Test
    fun DV01() {
        val d = Descuento(
            fecha_inicio = fecha(hoy.minusDays(1)),
            fecha_fin = fecha(hoy.plusDays(1))
        )
        assertTrue(d.estaVigente())
    }

    // Descuento con inicio manana: aun no vigente
    @Test
    fun DV02() {
        val d = Descuento(
            fecha_inicio = fecha(hoy.plusDays(1)),
            fecha_fin = fecha(hoy.plusDays(10))
        )
        assertFalse(d.estaVigente())
    }

    // Descuento con fin ayer: ya caducado
    @Test
    fun DV03() {
        val d = Descuento(
            fecha_inicio = fecha(hoy.minusDays(10)),
            fecha_fin = fecha(hoy.minusDays(1))
        )
        assertFalse(d.estaVigente())
    }

    // Descuento sin fechas: siempre vigente
    @Test
    fun DV04() {
        val d = Descuento()
        assertTrue(d.estaVigente())
    }

    // Solo fecha_inicio pasada sin fecha_fin: vigente
    @Test
    fun DV05() {
        val d = Descuento(fecha_inicio = fecha(hoy.minusDays(5)))
        assertTrue(d.estaVigente())
    }

    // semanaProxima con inicio en proximo lunes: true
    @Test
    fun DV06() {
        val lunesProximo = hoy.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        val d = Descuento(fecha_inicio = fecha(lunesProximo))
        assertTrue(d.semanaProxima())
    }

    // semanaProxima con inicio hoy: false (hoy es antes del proximo lunes)
    @Test
    fun DV07() {
        val d = Descuento(fecha_inicio = fecha(hoy))
        assertFalse(d.semanaProxima())
    }
}