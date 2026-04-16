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

class GroqApiService(private val context: Context) {

    private val apiKey = BuildConfig.GROQ_API_KEY
    private val url = "https://api.groq.com/openai/v1/chat/completions"
    private val systemPrompt = """
        Eres un asistente virtual del supermercado Pronto. Ayudas a los clientes con sus compras, recomendaciones, precios, etc. Responde de forma amigable y concisa. Siempre en español.
        Cuando recibas información del usuario (carrito, lista de compra, cupones, ofertas), debes usarla para responder. No te inventes productos ni cantidades. Si la información está vacía, indícalo.
        La lista de la compra y el carrito muéstralos en el siguiente formato:
        - Fresas (0.5 kg)
        - Camiseta negra con dibujo
        - Botella de agua mineral
        - Lata de fanta de naranja (x2)
        - Aceitunas (x3)
        """.trimIndent()

    suspend fun sendMessage(messages: List<Mensaje>, userContext: String = ""): String? = suspendCancellableCoroutine { continuation ->
        val queue = Volley.newRequestQueue(context)

        val jsonMessages = JSONArray()

        val finalSystemPrompt = if (userContext.isNotBlank()) {
            "$systemPrompt\n\nInformación actual del usuario:\n$userContext"
        } else {
            systemPrompt
        }

        jsonMessages.put(JSONObject().apply {
            put("role", "system")
            put("content", finalSystemPrompt)
        })

        messages.forEach { msg ->
            jsonMessages.put(JSONObject().apply {
                put("role", if (msg.sender == Mensaje.Sender.USER) "user" else "assistant")
                put("content", msg.content)
            })
        }

        val jsonBody = JSONObject().apply {
            put("model", "llama-3.1-8b-instant")
            put("messages", jsonMessages)
        }

        val successListener = { response: JSONObject ->
            try {
                val botReply = response.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                continuation.resume(botReply)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

        val errorListener = { error: com.android.volley.VolleyError ->
            val errorMsg = error.message ?: "Error desconocido"
            continuation.resumeWithException(Exception(errorMsg))
        }

        val request = object : JsonObjectRequest(
            Method.POST, url, jsonBody, successListener, errorListener
        ) {
            override fun getHeaders(): MutableMap<String, String> = hashMapOf(
                "Authorization" to "Bearer $apiKey",
                "Content-Type" to "application/json"
            )
        }

        queue.add(request)
        continuation.invokeOnCancellation { queue.cancelAll { true } }
    }
}