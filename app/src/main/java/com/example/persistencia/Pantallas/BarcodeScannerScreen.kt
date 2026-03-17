package com.example.persistencia.Pantallas

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.persistencia.Herramientas.CameraScannerView
import kotlinx.coroutines.launch

@Composable
fun BarcodeScannerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val activity = context as Activity

    // Pedimos permiso de cámara al entrar en la pantalla
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        }
    }

    // Estado del Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Scope para lanzar coroutinas (necesario para mostrar Snackbars)
    val scope = rememberCoroutineScope()

    // Estado donde guardaremos el código detectado
    var codigoDetectado by remember { mutableStateOf<String?>("prueba") }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Vista de la cámara + análisis de códigos
            CameraScannerView(
                modifier = Modifier.weight(1f)
            ) { codigo ->

                // Solo se ejecuta si el código detectado no es igual al que ya hay en el snackbar
                if (!codigo.equals(snackbarHostState.currentSnackbarData?.visuals?.message)) {
                    snackbarHostState.currentSnackbarData?.dismiss()

                    // Mostramos el Snackbar
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = codigo,
                            actionLabel = "Añadir al carrito", // Botón del snackbar
                            duration = SnackbarDuration.Indefinite, // El snackbar dura hasta que se presiona la X o se escanea un nuevo código
                            withDismissAction = true
                        )
                    }
                }
            }

//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(120.dp)
//                    .background(Color.White),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = codigoDetectado ?: "Escanea un código...",
//                    style = MaterialTheme.typography.headlineSmall
//                )
//            }
        }
    }
}


