package com.example.persistencia.Herramientas

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

// Convierte un timestamp a string
fun Timestamp.toFormattedString(): String {
    val date = this.toDate()  // Convierte el Timestamp de Firebase a un objeto Date de Java
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())  // Define el formato de fecha
    return format.format(date)  // Aplica el formato al Date y devuelve la cadena resultante
}