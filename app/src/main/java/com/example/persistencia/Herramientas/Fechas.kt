package com.example.persistencia.Herramientas

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

fun Timestamp.toFormattedString(): String {
    val date = this.toDate()
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}