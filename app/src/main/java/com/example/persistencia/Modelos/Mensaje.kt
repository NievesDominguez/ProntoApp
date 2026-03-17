package com.example.persistencia.Modelos


// Representa un mensaje en el chat.
data class Mensaje(
    val content: String, // Texto del mensaje
    val sender: Sender // Emisor del mensaje (USER o BOT)
) {
    enum class Sender {
        USER, BOT
    }
}