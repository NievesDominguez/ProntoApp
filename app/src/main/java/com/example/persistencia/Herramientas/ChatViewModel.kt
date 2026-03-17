package com.example.persistencia.Herramientas

import com.example.persistencia.Modelos.Mensaje
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatViewModel : ViewModel() {

    var messages = mutableStateOf<List<Mensaje>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
        private set

    private lateinit var groqApiService: GroqApiService

    init {
        // Mensaje de bienvenida
        messages.value = listOf(
            Mensaje("¡Hola! Bienvenido a Pronto. Soy tu asistente virtual. ¿En qué puedo ayudarte hoy?", Mensaje.Sender.BOT)
        )
    }

    fun initService(context: Context) {
        if (!::groqApiService.isInitialized) {
            groqApiService = GroqApiService(context)
        }
    }

    fun sendMessage(text: String, context: Context) {
        val userMessage = Mensaje(text, Mensaje.Sender.USER)
        messages.value = messages.value + userMessage
        isLoading.value = true

        viewModelScope.launch {
            try {
                initService(context)
                // Enviamos la lista completa de mensajes (incluyendo el historial)
                val reply = withContext(Dispatchers.IO) {
                    groqApiService.sendMessage(messages.value)  // Pasamos toda la lista
                }
                if (reply != null) {
                    messages.value = messages.value + Mensaje(reply, Mensaje.Sender.BOT)
                } else {
                    messages.value = messages.value + Mensaje("Lo siento, no pude obtener respuesta.", Mensaje.Sender.BOT)
                }
            } catch (e: Exception) {
                messages.value = messages.value + Mensaje("Error: ${e.message}", Mensaje.Sender.BOT)
            } finally {
                isLoading.value = false
            }
        }
    }
}