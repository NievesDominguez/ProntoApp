package com.example.persistencia

import android.app.Application
import com.stripe.android.PaymentConfiguration


class Aplicacion : Application() {
    override fun onCreate() {
        super.onCreate()
        // Clave pública de Stripe
        PaymentConfiguration.init(
            applicationContext,
            BuildConfig.STRIPE_PUBLISHABLE_KEY
        )
    }
}

