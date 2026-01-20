package com.example.persistencia.Notificacion

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.persistencia.R
import kotlin.random.Random


class NotificationHandler(private val context: Context) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val channelId = "notification_channel_id" // ID único (como dirección)
    fun showSimpleNotification() {
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Compra realizada") // Título
            .setContentText("Se ha realizado una compra. Haz click aquí para ver la cuenta.") // Texto/cuerpo de la notificación
            .setSmallIcon(R.drawable.pronto_blanco) // Icono del sistema
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Tipo de prioridad
            .setAutoCancel(true) // Que la notificación se descarte al tocarla
            .build() // Crea objeto final
        notificationManager.notify(Random.nextInt(), notification) // Envía la notificación
    }
}