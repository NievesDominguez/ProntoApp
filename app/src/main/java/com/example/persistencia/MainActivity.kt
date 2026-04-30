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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.persistencia.Herramientas.CheckoutHelper
import com.example.persistencia.Herramientas.ListasRepository
import com.example.persistencia.Herramientas.LocalThemeManager
import com.example.persistencia.Herramientas.ThemeManager
import com.example.persistencia.Herramientas.ThemePreference
import com.example.persistencia.ui.theme.PersistenciaTheme
import com.example.persistencia.Navegacion.AppNavigation
import com.example.persistencia.Navegacion.AppScreens
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ListasRepository.init(applicationContext)

        var navController: NavController? = null

        // Inicializa Stripe con la clave pública de prueba
        PaymentConfiguration.init(
            applicationContext, BuildConfig.STRIPE_PUBLISHABLE_KEY
        )

        // Crea la instancia de PaymentSheet que maneja la interfaz de pago
        lateinit var paymentSheet: PaymentSheet
        paymentSheet = PaymentSheet(this) { result ->
            when (result) {
                // Pago completado
                is PaymentSheetResult.Completed -> {
                    Toast.makeText(this, "Pago completado", Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch {
                        val ticketId = CheckoutHelper.finalizarCompra(limpiarCarrito = true)
                        if (ticketId != null) {
                            Toast.makeText(
                                this@MainActivity,
                                "Compra registrada",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Error al guardar la compra",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    navController?.navigate(AppScreens.PantallaPrincipal.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                // Pago cancelado
                is PaymentSheetResult.Canceled -> {
                    Toast.makeText(this, "Pago cancelado", Toast.LENGTH_SHORT).show()
                }
                // Error en el pago
                is PaymentSheetResult.Failed -> {
                    Toast.makeText(this, "Error: ${result.error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Permite que el contenido se dibuje detrás de las barras del sistema
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            val themeManager = remember { ThemeManager(applicationContext) }
            CompositionLocalProvider(LocalThemeManager provides themeManager) {
                PersistenciaTheme(
                    darkTheme = when (themeManager.themePreference.value) { // <-- .value
                        ThemePreference.Light -> false
                        ThemePreference.Dark -> true
                        ThemePreference.System -> isSystemInDarkTheme()
                    },
                    dynamicColor = false
                ) {
                    // Obtiene el destino para la navegación
                    val destino = intent?.getStringExtra("destino")

                    // Inicia la navegación
                    AppNavigation(destino, paymentSheet) { navController = it }
                }
            }
        }
    }
}
