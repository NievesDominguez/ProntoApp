package com.example.persistencia.Herramientas

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi

class NotificationApplication : Application() {
    companion object {
        lateinit var instance: NotificationApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "notification_channel_id", // ID único (como dirección)
            "Notificaciones", // Nombre que ve el usuario
            NotificationManager.IMPORTANCE_HIGH // (sonido + LED + pantalla bloqueada + prioridad alta en la lista)
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}