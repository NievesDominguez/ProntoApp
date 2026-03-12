package com.example.persistencia.Herramientas

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.example.persistencia.Modelos.Mensaje
import com.example.persistencia.BuildConfig

class GroqApiService(private val context: Context) {

    private val apiKey = BuildConfig.GROQ_API_KEY

    private val url = "https://api.groq.com/openai/v1/chat/completions"
    private val systemPrompt = "Eres un asistente virtual del supermercado Pronto. Ayudas a los clientes con sus compras, recomendaciones, precios, etc. Responde de forma amigable y concisa. Siempre en español."

    suspend fun sendMessage(messages: List<Mensaje>): String? = suspendCancellableCoroutine { continuation ->
        val queue = Volley.newRequestQueue(context)

        // Construir el array de mensajes para la API
        val jsonMessages = JSONArray()

        // Añadir el mensaje de sistema (solo una vez, al principio)
        jsonMessages.put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
        })

        // Añadir el historial de mensajes (omitimos el primero si ya es el system? pero no está en la lista)
        messages.forEach { msg ->
            jsonMessages.put(JSONObject().apply {
                put("role", if (msg.sender == Mensaje.Sender.USER) "user" else "assistant")
                put("content", msg.content)
            })
        }

        val jsonBody = JSONObject().apply {
            put("model", "llama-3.1-8b-instant") // Modelo de IA a usar
            put("messages", jsonMessages)
        }

        val successListener = Response.Listener<JSONObject> { response ->
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

        val errorListener = Response.ErrorListener { error ->
            val errorMsg = if (error.networkResponse != null && error.networkResponse.data != null) {
                String(error.networkResponse.data)
            } else {
                error.message ?: "Error desconocido"
            }
            continuation.resumeWithException(Exception("Error ${error.networkResponse?.statusCode}: $errorMsg"))
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