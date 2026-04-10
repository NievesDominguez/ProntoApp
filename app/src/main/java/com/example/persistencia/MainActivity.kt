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
import com.redsys.tpvvinapplibrary.ErrorResponse
import com.redsys.tpvvinapplibrary.ResultResponse
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

class MainActivity : ComponentActivity() {

    // Stripe PaymentSheet debe declararse aquí, NO dentro de onCreate
    //lateinit var paymentSheet: PaymentSheet

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        // Inicializamos Stripe PaymentSheet ANTES de que la Activity esté STARTED
//        paymentSheet = PaymentSheet(this) { result ->
//            PaymentSheetResultHandler.onPaymentResult(result)
//        }
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51TE5qDCNZxzYE0FEay0v0s8J5F1BhPmXzIogyoTUQAwYmLNMmFBgBUCna9eYdMZahW7RvQWEbewlg4k1Ts2qSg2R007qkaME2A"
        )
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
                    //AppNavigation(destino, paymentSheet)
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
