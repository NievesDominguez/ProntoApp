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

class ChatViewModel : ViewModel() {

    var messages = mutableStateOf<List<Mensaje>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
        private set

    private lateinit var groqApiService: GroqApiService

    // DAOs para obtener datos del usuario
    private val carritoDao = CarritoDao()
    private val listasDao = ListasDao()
    private val productosDao = ProductosDao()
    private val descuentosDao = DescuentosDao()
    private val usuariosDao = UsuariosDao()

    init {
        messages.value = listOf(
            Mensaje(
                "¡Hola! Bienvenido a Pronto. Soy tu asistente virtual. ¿En qué puedo ayudarte hoy?",
                Mensaje.Sender.BOT
            )
        )
    }

    fun initService(context: Context) {
        if (!::groqApiService.isInitialized) {
            groqApiService = GroqApiService(context)
        }
    }

    // Obtiene toda la información relevante del usuario para inyectar en el prompt
    private suspend fun getUserContext(): String {
        val user = FirebaseAuth.getInstance().currentUser ?: return ""
        val uid = user.uid

        // 1. Carrito
        val carritoItems = carritoDao.getCarrito()
        val carritoTexto = if (carritoItems.isNotEmpty()) {
            val productosCarrito = mutableListOf<String>()
            for ((id, cantidad) in carritoItems) {
                val prod = productosDao.getProducto(id)
                prod?.let {
                    productosCarrito.add("${it.nombre} (${cantidad} ${it.unidad ?: "ud"})")
                }
            }
            "📦 Carrito: ${productosCarrito.joinToString(", ")}"
        } else {
            "🛒 Carrito vacío"
        }

        // 2. Lista de la compra
        val listaItems = listasDao.getLista()
        val listaTexto = if (listaItems.isNotEmpty()) {
            val productosLista = mutableListOf<String>()
            for (item in listaItems) {
                val prod = productosDao.getProducto(item.id)
                prod?.let {
                    productosLista.add("${it.nombre} (${item.cantidad} ud)")
                }
            }
            "📝 Lista de la compra: ${productosLista.joinToString(", ")}"
        } else {
            "📝 Lista de la compra vacía"
        }

        // 3. Cupones activos del usuario
        val cuponesUsuario = usuariosDao.getCupones()
        val cuponesTexto = if (cuponesUsuario.isNotEmpty()) {
            "🎟️ Tus cupones activos: ${cuponesUsuario.joinToString(", ")}"
        } else {
            "🎟️ No tienes cupones activos"
        }

        // 4. Ofertas generales disponibles
        val ofertas = descuentosDao.getOfertas()
        val ofertasTexto = if (ofertas.isNotEmpty()) {
            val descOfertas = ofertas.mapNotNull { it.nombre ?: it.codigo }
            "🔥 Ofertas actuales: ${descOfertas.joinToString(", ")}"
        } else {
            "🔥 No hay ofertas especiales ahora"
        }

        return buildString {
            appendLine("Contexto del usuario $uid:")
            appendLine(carritoTexto)
            appendLine(listaTexto)
            appendLine(cuponesTexto)
            appendLine(ofertasTexto)
        }
    }

    fun sendMessage(text: String, context: Context) {
        val userMessage = Mensaje(text, Mensaje.Sender.USER)
        messages.value = messages.value + userMessage
        isLoading.value = true

        viewModelScope.launch {
            try {
                initService(context)
                val userContext = getUserContext()
                val reply = withContext(Dispatchers.IO) {
                    groqApiService.sendMessage(messages.value, userContext)
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