package com.example.persistencia

import android.app.Application
import com.redsys.tpvvinapplibrary.TPVVConfiguration
import com.redsys.tpvvinapplibrary.TPVVConstants

class Aplicacion : Application() {
    override fun onCreate() {
        super.onCreate()
        // Entorno de pruebas
        TPVVConfiguration.setEnvironment(TPVVConstants.ENVIRONMENT_INTEGRATION)
        // Datos de prueba
        TPVVConfiguration.setFuc("263100000")   // Código de comercio (FUC) de prueba
        TPVVConfiguration.setTerminal("1")    // Número de terminal de prueba
        TPVVConfiguration.setCurrency("978")    // Código de moneda (EUR)
        // Parámetros adicionales que suelen ser necesarios
        TPVVConfiguration.setMerchantName("Pronto")
        //TPVVConfiguration.setMerchantUrl("https://www.pronto.com")
        TPVVConfiguration.setLanguage("1")      // 1 = Español
        // Licencia (para pruebas, puede ir vacío o se puede omitir)
        //TPVVConfiguration.setLicense("sq7HjrUOBfKmC576ILgskD5srU870gJ7")
        TPVVConfiguration.setTitular("Titular de la prueba")
    }
}

