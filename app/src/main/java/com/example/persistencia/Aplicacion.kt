package com.example.persistencia

import android.app.Application
import com.stripe.android.PaymentConfiguration


class Aplicacion : Application() {
    override fun onCreate() {
        super.onCreate()
        // Clave pública de Stripe
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51TE5qDCNZxzYE0FEay0v0s8J5F1BhPmXzIogyoTUQAwYmLNMmFBgBUCna9eYdMZahW7RvQWEbewlg4k1Ts2qSg2R007qkaME2A"
        )
    }
}

