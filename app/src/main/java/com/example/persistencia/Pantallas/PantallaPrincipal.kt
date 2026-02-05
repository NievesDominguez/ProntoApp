package com.example.persistencia.Pantallas

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.composables.icons.lucide.CircleUserRound
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.example.persistencia.Navegacion.AppScreens
import com.example.persistencia.Notificacion.NotificationHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.TIRAMISU) // Sólo Android 13 o superior (API 33)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(navController: NavController) {

    val scope = rememberCoroutineScope() // Para ejecutar corrutinas
    val context = LocalContext.current // Para acceder al sistema
    val postNotificationPermission =
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS) // Control de permisos
    val notificationHandler = NotificationHandler(context) // La clase de notificaciones
    LaunchedEffect(key1 = true) { // Al cargar la ventana pide permiso POST_NOTIFICATIONS si no se pidió. Sólo la primera vez en la primera recomposición. Pide el permiso automáticamente.
        if (!postNotificationPermission.status.isGranted) {
            postNotificationPermission.launchPermissionRequest() // Popup de permiso si no está concedido
        }
    }


    // --------------------------------------------------------------------------
    // BARRA SUPERIOR
    // --------------------------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(90.dp),
                title = {
                    // Centra verticalmente el texto dentro del espacio disponible
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pantalla principal",
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                // Aquí estará el icono para volver a la pantalla principal
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(route = AppScreens.PantallaPrincipal.route)
                        Toast.makeText(context, "Pantalla principal", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Lucide.House,
                            contentDescription = "Inicio",
                            tint = Color.White
                        )
                    }
                },

                // Icono alineado a la derecha
                actions = {
                    // Icono de perfil
                    IconButton(
                        onClick = {
                            navController.navigate(route = AppScreens.Resultados.route)
                            Toast.makeText(context, "Volver al perfil", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.CircleUserRound,
                            contentDescription = "Usuario",
                            tint = Color.White
                        )
                    }

                    // Icono de log out
                    IconButton(
                        onClick = {
                            navController.navigate(route = AppScreens.Inicio.route)
                            Toast.makeText(context, "Volver atrás", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.LogOut,
                            contentDescription = "backIcon",
                            tint = Color.White
                        )
                    }

                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hola",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )

            Column {
                Button(onClick = {
                    notificationHandler.showSimpleNotification(
                        titulo = "Esta es una prueba simple",
                        cuerpo = "Prueba de una notificación simple",
                        destino = ""
                    ) // Luego notifica el mensaje creado
                }) { Text(text = "Clic para una notificación simple") }

                Button(onClick = {
                    scope.launch { // Ejecuta la corrutina
                        delay(10000) // Espera de 10 segundos. 1 segundo = 1000 milisegundos
                        Toast.makeText(
                            context,
                            "Presionaste ir a la ventana de inicio",
                            Toast.LENGTH_SHORT
                        ).show()
                        notificationHandler.showSimpleNotification(
                            "¡Hola!",
                            "Notificación que irá a la ventana de inicio",
                            "Inicio"
                        ) // Luego notifica el mensaje creado
                    }
                }) { Text(text = "Clic para una notificación avanzada en la ventana de inicio") }
                Button(
                    onClick = {
                        scope.launch { // Ejecuta la corrutina
                            delay(10000) // Espera de 10 segundos. 1 segundo = 1000 milisegundos
                            Toast.makeText(
                                context,
                                "Presionaste ir al perfil",
                                Toast.LENGTH_SHORT
                            ).show()
                            notificationHandler.showSimpleNotification(
                                "¡Hola!",
                                "Notificación que irá a la ventana perfil",
                                "Perfil"
                            ) // Luego notifica el mensaje creado
                        }
                    }
                ) {
                    Text(text = "Clic para una notificación avanzada en la ventana perfil")
                }

                Button(
                    onClick = {
                        navController.navigate(route = AppScreens.Escaner.route)
                        Toast.makeText(context, "Escanear producto", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(text = "Escanear un producto")
                }


            }

        }


    }

}
