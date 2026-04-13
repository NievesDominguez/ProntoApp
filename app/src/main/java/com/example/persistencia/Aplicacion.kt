package com.example.persistencia

import android.app.Application
import com.stripe.android.PaymentConfiguration


class Aplicacion : Application() {
    override fun onCreate() {
        super.onCreate()
        // Clave pública de Stripe desde BuildConfig
        val stripeKey = BuildConfig.STRIPE_PUBLISHABLE_KEY
        if (stripeKey.isNotBlank() && stripeKey.startsWith("pk_")) {
            PaymentConfiguration.init(
                applicationContext,
                stripeKey
            )
        }
    }
}

