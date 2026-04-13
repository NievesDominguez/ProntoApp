package com.example.persistencia

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import com.example.persistencia.Herramientas.LocalThemeManager
import com.example.persistencia.Herramientas.ThemeManager
import com.example.persistencia.Herramientas.ThemePreference
import com.example.persistencia.ui.theme.PersistenciaTheme
import com.example.persistencia.Navegacion.AppNavigation
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa Stripe con la clave pública de prueba
        PaymentConfiguration.init(
            applicationContext,BuildConfig.STRIPE_PUBLISHABLE_KEY
        )

        // Crea la instancia de PaymentSheet que maneja la interfaz de pago
        lateinit var paymentSheet: PaymentSheet
        paymentSheet = PaymentSheet(this) { result ->
            when (result) {
                is PaymentSheetResult.Completed -> {
                    Toast.makeText(this, "Pago completado", Toast.LENGTH_SHORT).show()
                }
                is PaymentSheetResult.Canceled -> {
                    Toast.makeText(this, "Pago cancelado", Toast.LENGTH_SHORT).show()
                }
                is PaymentSheetResult.Failed -> {
                    Toast.makeText(this, "Error: ${result.error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Permite que el contenido se dibuje detrás de las barras del sistema
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            val themeManager = remember { ThemeManager() }

            CompositionLocalProvider(LocalThemeManager provides themeManager) {
                PersistenciaTheme(
                    darkTheme = when (themeManager.themePreference) {
                        ThemePreference.Light -> false
                        ThemePreference.Dark -> true
                        ThemePreference.System -> isSystemInDarkTheme()
                    },
                    dynamicColor = false
                ) {
                    // Obtiene el destino para la navegación
                    val destino = intent?.getStringExtra("destino")

                    // Inicia la navegación
                    AppNavigation(destino, paymentSheet)
                }
            }
        }
    }
}
