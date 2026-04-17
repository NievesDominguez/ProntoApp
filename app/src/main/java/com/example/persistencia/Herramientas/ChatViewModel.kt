package com.example.persistencia.Herramientas

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persistencia.Firestore.*
import com.example.persistencia.Modelos.Mensaje
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Gestiona el estado de la conversación, la comunicación con Groq  y la obtención del contexto del usuario
class ChatViewModel : ViewModel() {

    // Lista de mensajes del chat
    var messages = mutableStateOf<List<Mensaje>>(emptyList())
        private set

    // Indica si se está esperando una respuesta del asistente
    var isLoading = mutableStateOf(false)
        private set

    // Servicio que se comunicará con la API de Groq
    private lateinit var groqApiService: GroqApiService

    // DAOs para obtener datos del usuario
    private val carritoDao = CarritoDao()
    private val listasDao = ListasDao()
    private val productosDao = ProductosDao()
    private val descuentosDao = DescuentosDao()
    private val usuariosDao = UsuariosDao()

    // Mensaje de bienvenida
    init {
        messages.value = listOf(
            Mensaje(
                "¡Hola! Bienvenido a Pronto. Soy tu asistente virtual. ¿En qué puedo ayudarte hoy?",
                Mensaje.Sender.BOT
            )
        )
    }

    // Inicializa el servicio de Groq si aún no se ha hecho
    fun initService(context: Context) {
        if (!::groqApiService.isInitialized) {
            groqApiService = GroqApiService(context)
        }
    }

    // Obtiene toda la información relevante del usuario para inyectar en el prompt
    private suspend fun getUserContext(): String {
        val user = FirebaseAuth.getInstance().currentUser ?: return ""
        val uid = user.uid

        // Carrito
        val carritoItems = carritoDao.getCarrito()
        val carritoTexto = if (carritoItems.isNotEmpty()) {
            val productosCarrito = mutableListOf<String>()
            for ((id, cantidad) in carritoItems) {
                val prod = productosDao.getProducto(id)
                prod?.let {
                    val esAlPeso = prod.al_peso == true
                    val texto = if (esAlPeso) {
                        // Producto al peso: siempre mostrar kg
                        "${it.nombre} (${"%.2f".format(cantidad)} kg)"
                    } else {
                        // Producto normal: mostrar xN solo si cantidad > 1
                        val cantidadInt = cantidad.toInt()
                        if (cantidadInt == 1) it.nombre
                        else "${it.nombre} (x$cantidadInt)"
                    }
                    productosCarrito.add(texto)
                }
            }
            "Carrito: ${productosCarrito.joinToString(", ")}"
        } else {
            "🛒 Carrito vacío"
        }

        // Lista de la compra
        val listaItems = listasDao.getLista()
        val listaTexto = if (listaItems.isNotEmpty()) {
            val productosLista = mutableListOf<String>()
            for (item in listaItems) {
                val prod = productosDao.getProducto(item.id)
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
        val cuponesUsuario = usuariosDao.getCupones()
        val cuponesTexto = if (cuponesUsuario.isNotEmpty()) {
            "Tus cupones activos: ${cuponesUsuario.joinToString(", ")}"
        } else "No tienes cupones activos"

        // Ofertas
        val ofertas = descuentosDao.getOfertas()
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
    fun sendMessage(text: String, context: Context) {
        // Crear el mensaje del usuario y añadirlo a la lista de mensajes
        val userMessage = Mensaje(text, Mensaje.Sender.USER)
        messages.value += userMessage
        isLoading.value = true

        viewModelScope.launch {
            try {
                // Asegurar que el servicio de Groq esté inicializado
                initService(context)

                // Obtener toda la información personalizada del usuario
                val userContext = getUserContext()

                // Llamar a Groq en un hilo de IO para no bloquear la UI
                val reply = withContext(Dispatchers.IO) {
                    groqApiService.sendMessage(messages.value, userContext)
                }

                // Añadir la respuesta del bot o un mensaje de error
                if (reply != null) {
                    messages.value += Mensaje(reply, Mensaje.Sender.BOT)
                } else {
                    messages.value += Mensaje(
                        "Lo siento, no pude obtener respuesta.",
                        Mensaje.Sender.BOT
                    )
                }
            } catch (e: Exception) {
                messages.value += Mensaje("Error: ${e.message}", Mensaje.Sender.BOT)
            } finally {
                isLoading.value = false
            }
        }
    }
}