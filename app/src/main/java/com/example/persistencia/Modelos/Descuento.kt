package com.example.persistencia.Modelos

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@IgnoreExtraProperties
data class Descuento(
    val codigo: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val tipo: String? = null,
    val ambito: String? = null,
    val formula: Map<String, Any>? = null,
    val fecha_inicio: String? = null,
    val fecha_fin: String? = null,
    val max_descuento: Double? = null
)

@RequiresApi(Build.VERSION_CODES.O)
private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

// Comprueba si el descuento está vigente hoy
@RequiresApi(Build.VERSION_CODES.O)
fun Descuento.estaVigente(): Boolean {
    val hoy = LocalDate.now()
    val inicio = fecha_inicio?.let { LocalDate.parse(it, formatter) }
    val fin = fecha_fin?.let { LocalDate.parse(it, formatter) }
    if (inicio != null && hoy.isBefore(inicio)) return false
    if (fin != null && hoy.isAfter(fin)) return false
    return true
}

// Comprueba si la fecha_inicio del descuento cae en la próxima semana natural
@RequiresApi(Build.VERSION_CODES.O)
fun Descuento.semanaProxima(): Boolean {
    val hoy = LocalDate.now()
    val lunesProximo = hoy.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
    val domingoProximo = lunesProximo.plusDays(6)
    val inicio = fecha_inicio?.let { LocalDate.parse(it, formatter) } ?: return false
    return !inicio.isBefore(lunesProximo) && !inicio.isAfter(domingoProximo)
}