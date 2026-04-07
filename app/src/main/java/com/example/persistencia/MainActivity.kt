package com.example.persistencia

import android.os.Build
import android.os.Bundle
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
import com.redsys.tpvvinapplibrary.ErrorResponse
import com.redsys.tpvvinapplibrary.ResultResponse
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

class MainActivity : ComponentActivity() {

    // Stripe PaymentSheet debe declararse aquí, NO dentro de onCreate
    lateinit var paymentSheet: PaymentSheet

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializamos Stripe PaymentSheet ANTES de que la Activity esté STARTED
        paymentSheet = PaymentSheet(this) { result ->
            PaymentSheetResultHandler.onPaymentResult(result)
        }

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
                    val destino = intent?.getStringExtra("destino")

                    // Pasamos paymentSheet a la navegación
                    AppNavigation(destino, paymentSheet)
                }
            }
        }
    }
}

// Manejo del resultado de Stripe
object PaymentSheetResultHandler {
    var onResult: ((PaymentSheetResult) -> Unit)? = null

    fun onPaymentResult(result: PaymentSheetResult) {
        onResult?.invoke(result)
    }
}

object RedsysPaymentResultHandler {
    var onSuccess: ((ResultResponse) -> Unit)? = null
    var onError: ((ErrorResponse) -> Unit)? = null
}
