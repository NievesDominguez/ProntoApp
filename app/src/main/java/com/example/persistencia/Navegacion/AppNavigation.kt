package com.example.persistencia.Navegacion

import android.app.Activity
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.persistencia.Pantallas.BarcodeScannerScreen
import com.example.persistencia.Pantallas.Carrito
import com.example.persistencia.Pantallas.Catalogo
import com.example.persistencia.Pantallas.Chatbot
import com.example.persistencia.Pantallas.Cupones
import com.example.persistencia.Pantallas.Inicio
import com.example.persistencia.Pantallas.InvitacionesScreen
import com.example.persistencia.Pantallas.ListaCompra
import com.example.persistencia.Pantallas.PantallaPrincipal
import com.example.persistencia.Pantallas.PantallaProducto
import com.example.persistencia.Pantallas.Perfil
import com.example.persistencia.Pantallas.Registro
import com.example.persistencia.Pantallas.TicketDetalleScreen
import com.stripe.android.paymentsheet.PaymentSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.example.persistencia.Herramientas.FlyToTargetState
import com.example.persistencia.Herramientas.LocalFlyToTargetState
import com.example.persistencia.Herramientas.FlyToTargetOverlay


/**  
 * Composable reutilizable: al pulsar atrás una vez muestra un Toast,  
 * al pulsar atrás otra vez en menos de 2 segundos cierra la app.
 */
@Composable
fun DoubleBackToExit() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var backPressedOnce by remember { mutableStateOf(false) }

    BackHandler {
        if (backPressedOnce) {
            // Cierra la app
            (context as? Activity)?.finish()
        } else {
            backPressedOnce = true
            Toast.makeText(context, "Pulsa atrás de nuevo para salir", Toast.LENGTH_SHORT).show()
            scope.launch {
                delay(2000)
                backPressedOnce = false
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation(
    destino: String?,
    paymentSheet: PaymentSheet,
    onNavControllerReady: (NavController) -> Unit = {}
) {
    val startDestination =
        when (destino) {
            "Inicio" -> AppScreens.Inicio.route
            "Perfil" -> AppScreens.Perfil.route
            "Carrito" -> AppScreens.Carrito.route
            "Catalogo" -> AppScreens.Catalogo.route
            "PantallaPrincipal" -> AppScreens.PantallaPrincipal.route
            "ListaCompra" -> AppScreens.ListaCompra.route
            else -> AppScreens.PantallaPrincipal.route
        }

    val user = Firebase.auth.currentUser

    val destinoFinal = when {
        user == null -> AppScreens.Inicio.route
        destino != null -> startDestination
        else -> AppScreens.PantallaPrincipal.route
    }

    val navController = rememberNavController()

    LaunchedEffect(navController) {
        onNavControllerReady(navController)
    }

    val bottomBarScreens = listOf(
        AppScreens.PantallaPrincipal.route,
        AppScreens.Perfil.route,
        AppScreens.Catalogo.route,
        AppScreens.Carrito.route,
        AppScreens.ListaCompra.route
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val flyState = remember { FlyToTargetState() }

    CompositionLocalProvider(LocalFlyToTargetState provides flyState) {
        Box(Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    if (currentRoute in bottomBarScreens) {
                        BottomBar(navController)
                    }
                }
            ) { innerPadding ->

                NavHost(
                    navController = navController,
                    startDestination = destinoFinal,
                ) {
                    // Pantallas raíz: doble click atrás para salir

                    composable(route = AppScreens.Inicio.route) {
                        DoubleBackToExit()
                        Inicio(navController)
                    }

                    composable(route = AppScreens.PantallaPrincipal.route) {
                        DoubleBackToExit()
                        PantallaPrincipal(navController)
                    }

                    // Pantallas con back normal

                    composable(route = AppScreens.Registro.route) {
                        Registro(navController)
                    }

                    composable(route = AppScreens.Perfil.route) {
                        Perfil(navController)
                    }

                    composable(route = AppScreens.Catalogo.route) {
                        Catalogo(navController)
                    }

                    composable(route = AppScreens.Carrito.route) {
                        Carrito(navController, paymentSheet)
                    }

                    composable(route = AppScreens.ListaCompra.route) {
                        ListaCompra(navController)
                    }

                    // Pantallas secundarias: back = popBackStack

                    composable(route = AppScreens.Escaner.route) {
                        BackHandler {
                            navController.popBackStack()
                        }
                        BarcodeScannerScreen(navController)
                    }

                    composable(
                        route = AppScreens.PantallaProducto.route + "/{idProducto}",
                        arguments = listOf(
                            navArgument("idProducto") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        BackHandler {
                            navController.popBackStack()
                        }
                        val id =
                            backStackEntry.arguments?.getString("idProducto") ?: return@composable
                        PantallaProducto(idProducto = id, navController)
                    }

                    composable(route = AppScreens.Cupones.route) {
                        BackHandler {
                            navController.popBackStack()
                        }
                        Cupones(navController)
                    }

                    composable(route = AppScreens.Chatbot.route) {
                        BackHandler {
                            navController.popBackStack()
                        }
                        Chatbot(onBack = { navController.popBackStack() })
                    }

                    composable(
                        route = AppScreens.TicketDetalle.route + "/{ticketId}",
                        arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val ticketId =
                            backStackEntry.arguments?.getString("ticketId") ?: return@composable
                        TicketDetalleScreen(ticketId = ticketId, navController = navController)
                    }

                    composable(route = AppScreens.Invitaciones.route) {
                        BackHandler { navController.popBackStack() }
                        InvitacionesScreen(navController)
                    }
                }
            }

            // Overlay de animación por encima del Scaffold
            FlyToTargetOverlay(flyState)
        }
    }
}