package com.example.persistencia.Pantallas

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.persistencia.PaymentSheetResultHandler
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagoStripe(
    navController: NavController,
    total: Float,
    paymentSheet: PaymentSheet
) {
    val scope = rememberCoroutineScope()

    var clientSecret by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var paymentCompleted by remember { mutableStateOf(false) }

    // Registrar callback del resultado
    LaunchedEffect(Unit) {
        PaymentSheetResultHandler.onResult = { result ->
            when (result) {
                is PaymentSheetResult.Completed -> paymentCompleted = true
                is PaymentSheetResult.Failed -> errorMessage = result.error.localizedMessage
                is PaymentSheetResult.Canceled -> {}
            }
        }
    }

    fun crearPaymentIntent() {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null

                val amountInCents = (total * 100).toInt()

                val url = URL("https://stripe-backend.prontoapp.workers.dev")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val body = JSONObject()
                body.put("amount", amountInCents)

                connection.outputStream.use { os ->
                    os.write(body.toString().toByteArray())
                }

                val response = connection.inputStream.bufferedReader().readText()
                Log.d("Stripe", "Respuesta del servidor: $response")
                Log.d("Stripe", "ClientSecret recibido: $clientSecret")
                Log.d("Stripe", "Estado: clientSecret=$clientSecret isLoading=$isLoading paymentCompleted=$paymentCompleted")


                val json = JSONObject(response)

                clientSecret = json.getString("clientSecret")

            } catch (e: Exception) {
                errorMessage = e.toString()
                Log.e("Stripe", "Error creando PaymentIntent", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun lanzarPaymentSheet() {
        val secret = clientSecret ?: return

        val config = PaymentSheet.Configuration(
            merchantDisplayName = "Pronto"
        )

        paymentSheet.presentWithPaymentIntent(secret, config)
    }

    LaunchedEffect(Unit) {
        crearPaymentIntent()
    }

    // UI igual que antes…
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Pago con tarjeta", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Total: %.2f €".format(total), fontSize = 22.sp, fontWeight = FontWeight.Bold)

                if (isLoading) CircularProgressIndicator()

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { crearPaymentIntent() }) { Text("Reintentar") }
                }

                if (clientSecret != null && !isLoading && !paymentCompleted) {
                    Button(onClick = { lanzarPaymentSheet() }) { Text("Pagar ahora") }
                }

                if (paymentCompleted) {
                    Text("Pago completado correctamente", color = MaterialTheme.colorScheme.primary)
                    Button(onClick = { navController.popBackStack() }) { Text("Volver") }
                }
            }
        }
    }
}
