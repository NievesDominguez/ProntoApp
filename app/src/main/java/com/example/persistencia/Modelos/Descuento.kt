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
    val codigo: String? = null,             // Código del descuento
    val nombre: String? = null,             // Nombre del descuento
    val descripcion: String? = null,        // Descripción del descuento
    val tipo: String? = null,               // Tipo de descuento (oferta o cupón)
    val ambito: String? = null,             // Si se aplica al carrito o a productos seleccionados
    val formula: Map<String, Any>? = null,  // Fórmula usada para calcular el descuento
    val fecha_inicio: String? = null,       // Inicio de vigencia del descuento
    val fecha_fin: String? = null,          // Fin de vigencia del descuento
    val max_descuento: Double? = null       // Máximo dinero que puede descontar el descuento
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