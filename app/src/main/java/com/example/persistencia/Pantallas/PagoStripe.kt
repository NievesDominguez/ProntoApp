package com.example.persistencia.Pantallas

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.stripe.android.paymentsheet.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun PagoStripeScreen(navController: NavController, total: Double) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val paymentSheet = rememberPaymentSheet { result ->
        isLoading = false
        if (result is PaymentSheetResult.Completed) {
            navController.navigate("exito_pedido")
        }
    }

    // FUNCIÓN PRO: Llama a tu Python para obtener el Secret real
    fun iniciarPagoReal() {
        isLoading = true
        scope.launch(Dispatchers.IO) {
            try {
                // REEMPLAZA CON LA IP DE TU PC (ej: 192.168.1.50)
                val url = URL("http://192.168.74.52:4242/payment-sheet")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val amountInCents = (total * 100).toInt()
                val jsonInput = JSONObject().put("amount", amountInCents).toString()

                conn.outputStream.use { it.write(jsonInput.toByteArray()) }

                val response = conn.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)
                val clientSecret = jsonResponse.getString("paymentIntent")

                withContext(Dispatchers.Main) {
                    paymentSheet.presentWithPaymentIntent(
                        clientSecret,
                        PaymentSheet.Configuration("Pronto Supermercados", googlePay = null)
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Toast.makeText(context, "Error: No se pudo conectar al servidor", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Total a pagar: ${"%.2f".format(total)} €", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(30.dp))

        Button(
            onClick = { iniciarPagoReal() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(Modifier.size(24.dp))
            else Text("Pagar Ahora (Modo Pro)")
        }
    }
}