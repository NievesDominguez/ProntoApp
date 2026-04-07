package com.example.persistencia.Pantallas

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.persistencia.Herramientas.RedsysPaymentHelper
import com.example.persistencia.Navegacion.AppScreens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPagoRedsys(
    navController: NavController,
    total: Float
) {
    val context = LocalContext.current
    var pagoEnProceso by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pago con Redsys") },
                navigationIcon = {
                    if (!pagoEnProceso) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total a pagar",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "%.2f €".format(total),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Método de pago: Tarjeta de crédito/débito",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (!pagoEnProceso) {
                        pagoEnProceso = true
                        RedsysPaymentHelper.startDirectPayment(
                            context = context,
                            amount = (total * 100).toInt().toDouble(),
                            description = "Compra en Mi Tienda",
                            onSuccess = {
                                pagoEnProceso = false
                                Toast.makeText(context, "Pago realizado correctamente", Toast.LENGTH_LONG).show()
                                navController.navigate(AppScreens.PantallaPrincipal.route) {
                                    popUpTo(AppScreens.PagoRedsys.route) { inclusive = true }
                                }
                            },
                            onError = { error ->
                                pagoEnProceso = false
                                Toast.makeText(context, "Error en el pago: ${error.desc}", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                enabled = !pagoEnProceso,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (pagoEnProceso) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Procesando pago...")
                } else {
                    Text("Pagar con Redsys")
                }
            }
        }
    }
}