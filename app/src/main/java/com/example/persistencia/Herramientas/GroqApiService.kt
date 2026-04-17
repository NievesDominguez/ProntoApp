package com.example.persistencia.Herramientas

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.persistencia.BuildConfig
import com.example.persistencia.Modelos.Mensaje
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Servicio para comunicarse con la API de Groq. Se encarga de construir la petición, enviarla y procesar la respuesta
class GroqApiService(private val context: Context) {

    private val apiKey = BuildConfig.GROQ_API_KEY // Clave API almacenada en BuildConfig
    private val url = "https://api.groq.com/openai/v1/chat/completions" // Endpoint de la API de Groq para completar chats

    // Prompt del sistema que define el comportamiento del asistente
    private val systemPrompt = """
        Eres un asistente virtual del supermercado Pronto. Ayudas a los clientes con sus compras, recomendaciones, precios, etc. Responde de forma amigable y concisa. Siempre en español.
        
        NORMAS IMPORTANTES:
        - Cuando recibas información del usuario (carrito, lista de compra, cupones, ofertas), DEBES usarla exactamente como viene. No inventes productos ni cantidades.
        - Para mostrar productos, SIEMPRE usa el formato que aparece en la información que recibes. Si ves "1 unidad" o "2 unidades", usa eso. Si ves "0.5 kg", úsalo.
        - Nunca muestres cantidades como "1.0" o "2.0". Si es entero, pon "1" o "2".
        - Para productos al peso (con kg), respeta el formato que ya viene dado.
        
        EJEMPLOS DE CÓMO RESPONDER:
        Usuario: "¿Qué tengo en el carrito?"
        Tú: "Tienes en tu carrito:
        - Leche entera (x2)
        - Fresas (0.5 kg)
        - Pan de molde"
        
        Usuario: "Muéstrame mi lista de la compra"
        Tú: "Tu lista de la compra tiene:
        - Huevos (12 unidades)
        - Manzanas (1 kg)
        - Yogures (x6)"
        
        REGLAS ADICIONALES:
        - Avisa si algún producto de la lista no tiene stock (usa la información que se te da).
        - Si preguntan por recomendaciones, usa los datos del catálogo (se te pasarán en el contexto si es necesario).
        - No ofrezcas añadir nada al carrito/lista, solo informa. Si alguien pregunta por añadir algo al carrito explica que se hace desde el catálogo o escaneando el código de barras del producto
        - Para cambiar tema (tema oscuro, tema claro), indica que se hace desde perfil, en la pestaña ajustes.
    """.trimIndent()

    /**
     * Envía el historial de la conversación a Groq junto con el contexto adicional del usuario.
     * Es una función suspend que se ejecuta en una corrutina y se puede cancelar.
     *
     * @param messages Lista de mensajes de la conversación
     * @param userContext Información adicional del usuario
     * @return La respuesta del asistente como String, o null si hubo un error
     */
    suspend fun sendMessage(messages: List<Mensaje>, userContext: String = ""): String? =
        suspendCancellableCoroutine { continuation ->
            val queue = Volley.newRequestQueue(context) // Cola de peticiones de Volley
            val jsonMessages = JSONArray() // Array JSON que contiene todos los mensajes

            // Construir el prompt final del sistema añadiendo el contexto si existe
            val finalSystemPrompt = if (userContext.isNotBlank()) {
                "$systemPrompt\n\nInformación actual del usuario:\n$userContext"
            } else {
                systemPrompt
            }

            // Añadir el mensaje de sistema con el prompt final
            jsonMessages.put(JSONObject().apply {
                put("role", "system")
                put("content", finalSystemPrompt)
            })

            // Añadir cada mensaje del historial
            messages.forEach { msg ->
                jsonMessages.put(JSONObject().apply {
                    put("role", if (msg.sender == Mensaje.Sender.USER) "user" else "assistant")
                    put("content", msg.content)
                })
            }

            // Cuerpo de la petición JSON
            val jsonBody = JSONObject().apply {
                put("model", "llama-3.1-8b-instant") // Modelo a utilizar
                put("messages", jsonMessages) // Lista de mensajes
            }

            // Listener para respuesta exitosa
            val successListener = { response: JSONObject ->
                try {
                    // Extraer el contenido del mensaje de la respuesta
                    val botReply = response.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    // Reanudar la corrutina con el resultado
                    continuation.resume(botReply)
                } catch (e: Exception) {
                    // Si falla, reanudar con excepción
                    continuation.resumeWithException(e)
                }
            }

            // Listener para error de red o de API
            val errorListener = { error: com.android.volley.VolleyError ->
                val errorMsg = error.message ?: "Error desconocido"
                continuation.resumeWithException(Exception(errorMsg))
            }

            // Crear la petición POST con los datos JSON
            val request = object : JsonObjectRequest(
                Method.POST, url, jsonBody, successListener, errorListener
            ) {
                // Añadir cabeceras necesarias: autenticación y tipo de contenido
                override fun getHeaders(): MutableMap<String, String> = hashMapOf(
                    "Authorization" to "Bearer $apiKey",
                    "Content-Type" to "application/json"
                )
            }

            queue.add(request) // Añadir la petición a la cola

            // Si la corrutina se cancela, cancelar todas las peticiones de Volley
            continuation.invokeOnCancellation { queue.cancelAll { true } }
        }
}