package com.example.persistencia.Herramientas

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persistencia.Firestore.*
import com.example.persistencia.Modelos.Mensaje
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Gestiona el estado de la conversación, la comunicación con Groq y la obtención del contexto del usuario
class ChatViewModel : ViewModel() {

    // Lista de mensajes del chat expuesta como StateFlow para consistencia con otros ViewModels
    private val _messages = MutableStateFlow<List<Mensaje>>(emptyList())
    val messages: StateFlow<List<Mensaje>> = _messages

    // Indica si se esta esperando una respuesta del asistente
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Servicio que se comunicará con la API de Groq
    private lateinit var groqApiService: GroqApiService

    // Mensaje de bienvenida
    init {
        _messages.value = listOf(
            Mensaje(
                "¡Hola! Bienvenido a Pronto. Soy tu asistente virtual. ¿En qué puedo ayudarte hoy?",
                Mensaje.Sender.BOT
            )
        )
    }

    // Inicializa el servicio de Groq si aun no se ha hecho
    fun initService(context: Context) {
        if (!::groqApiService.isInitialized) {
            groqApiService = GroqApiService(context)
        }
    }

    // Obtiene toda la información relevante del usuario para inyectar en el prompt
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getUserContext(): String {
        val user = FirebaseAuth.getInstance().currentUser ?: return ""
        val uid = user.uid

        // Carrito
        val carritoItems = CarritoRepository.getCarrito()
        val carritoTexto = if (carritoItems.isNotEmpty()) {
            val productosCarrito = mutableListOf<String>()
            for ((id, cantidad) in carritoItems) {
                val prod = ProductosRepository.getProducto(id)
                prod?.let {
                    val esAlPeso = prod.al_peso == true
                    val texto = if (esAlPeso) {
                        "${it.nombre} (${"%.2f".format(cantidad)} kg)"
                    } else {
                        val cantidadInt = cantidad.toInt()
                        if (cantidadInt == 1) it.nombre
                        else "${it.nombre} (x$cantidadInt)"
                    }
                    productosCarrito.add(texto)
                }
            }
            "Carrito: ${productosCarrito.joinToString(", ")}"
        } else {
            "Carrito vacío"
        }

        // Lista de la compra
        val listaItems = try {
            ListasRepository.getItems()
        } catch (e: Exception) {
            emptyList()
        }
        val listaTexto = if (listaItems.isNotEmpty()) {
            val productosLista = mutableListOf<String>()
            for (item in listaItems) {
                val prod = ProductosRepository.getProducto(item.id)
                prod?.let {
                    val esAlPeso = prod.al_peso == true
                    val texto = if (esAlPeso) {
                        "${it.nombre} (${"%.2f".format(item.cantidad)} kg)"
                    } else {
                        val cantidadInt = item.cantidad.toInt()
                        if (cantidadInt == 1) it.nombre
                        else "${it.nombre} (x$cantidadInt)"
                    }
                    productosLista.add(texto)
                }
            }
            "Lista de la compra: ${productosLista.joinToString(", ")}"
        } else {
            "Lista de la compra vacía"
        }

        // Cupones del usuario
        val cuponesUsuario = UsuariosRepository.getCupones()
        val cuponesTexto = if (cuponesUsuario.isNotEmpty()) {
            "Tus cupones activos: ${cuponesUsuario.joinToString(", ")}"
        } else "No tienes cupones activos"

        // Ofertas
        val ofertas = DescuentosRepository.getOfertas()
        val ofertasTexto = if (ofertas.isNotEmpty()) {
            val descOfertas = ofertas.mapNotNull { it.nombre ?: it.codigo }
            "Ofertas actuales: ${descOfertas.joinToString(", ")}"
        } else "No hay ofertas especiales ahora"

        // Construir el contexto final como un bloque de texto
        return buildString {
            appendLine("Contexto del usuario $uid:")
            appendLine(carritoTexto)
            appendLine(listaTexto)
            appendLine(cuponesTexto)
            appendLine(ofertasTexto)
        }
    }

    // Envía un mensaje del usuario al asistente
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(text: String, context: Context) {
        val userMessage = Mensaje(text, Mensaje.Sender.USER)
        _messages.value += userMessage
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Asegurar que el servicio de Groq esté inicializado
                initService(context)
                // Obtener toda la información personalizada del usuario
                val userContext = getUserContext()
                // Llamar a Groq en un hilo de IO para no bloquear la UI
                val reply = withContext(Dispatchers.IO) {
                    groqApiService.sendMessage(_messages.value, userContext)
                }
                // Agregar la respuesta del bot o un mensaje de error
                if (reply != null) {
                    _messages.value += Mensaje(reply, Mensaje.Sender.BOT)
                } else {
                    _messages.value += Mensaje(
                        "Lo siento, no pude obtener respuesta :(.",
                        Mensaje.Sender.BOT
                    )
                }
            } catch (e: Exception) {
                _messages.value += Mensaje("Error: ${e.message}", Mensaje.Sender.BOT)
            } finally {
                _isLoading.value = false
            }
        }
    }
}