package com.example.persistencia.Navegacion

import android.os.Build
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
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
import com.example.persistencia.Pantallas.ListaCompra
import com.example.persistencia.Pantallas.PagoStripe
import com.example.persistencia.Pantallas.PantallaPagoRedsys
import com.example.persistencia.Pantallas.PantallaPrincipal
import com.example.persistencia.Pantallas.PantallaProducto
import com.example.persistencia.Pantallas.Perfil
import com.example.persistencia.Pantallas.Registro
import com.stripe.android.paymentsheet.PaymentSheet


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation(destino: String?, paymentSheet: PaymentSheet) { // Recibe la información del destino
    val startDestination =
        when (destino) { // Verifica con un when la información leída para determinar la ventana que se abrirá
            "Inicio" -> AppScreens.Inicio.route
            "Perfil" -> AppScreens.Perfil.route
            "Carrito" -> AppScreens.Carrito.route
            "Catalogo" -> AppScreens.Catalogo.route
            "PantallaPrincipal" -> AppScreens.PantallaPrincipal.route
            "ListaCompra" -> AppScreens.ListaCompra.route
            else -> AppScreens.PantallaPrincipal.route
        }

    val context = LocalContext.current
    val user = Firebase.auth.currentUser

    val destinoFinal = when {
        user == null -> AppScreens.Inicio.route
        destino != null -> startDestination
        else -> AppScreens.PantallaPrincipal.route
    }

    val navController = rememberNavController()

    // Pantallas en las que aparece la barra inferior
    val bottomBarScreens = listOf(
        AppScreens.PantallaPrincipal.route,
        AppScreens.Perfil.route,
        AppScreens.Catalogo.route,
        AppScreens.Carrito.route,
        AppScreens.ListaCompra.route
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState() // Indica la pantalla actual
    val currentRoute = navBackStackEntry?.destination?.route // Indica la ruta a la pantalla actual

    Scaffold(
        // Muestra la barra inferior solo en las pantallas indicadas
        bottomBar = {
            if (currentRoute in bottomBarScreens) {
                BottomBar(navController)
            }
        }
    ) { innerPadding ->

        //NavHost(navController = navController, startDestination = AppScreens.Formulario.route) {
        NavHost(
            navController = navController,
            startDestination = destinoFinal,
            //modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = AppScreens.Inicio.route) {
                Inicio(navController)
            }

            composable(route = AppScreens.PantallaPrincipal.route) {
                BackHandler(true) {
                    Toast.makeText(
                        context,
                        "Presionaste atrás, pero está restringido volver atrás",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                PantallaPrincipal(navController)
            }

            composable(route = AppScreens.Registro.route) {
                BackHandler(true) {
                    Toast.makeText(
                        context,
                        "Presionaste atrás, pero está restringido volver atrás",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Registro(navController)
            }

            composable(route = AppScreens.Perfil.route) {
                BackHandler(true) {
                    Toast.makeText(
                        context,
                        "Presionaste atrás, pero está restringido volver atrás",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Perfil(navController)
            }

            composable(route = AppScreens.Catalogo.route) {
                BackHandler(true) {
                    Toast.makeText(
                        context,
                        "Presionaste atrás, pero está restringido volver atrás",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Catalogo(navController)
            }

            composable(route = AppScreens.Carrito.route) {
                BackHandler(true) {
                    Toast.makeText(
                        context,
                        "Presionaste atrás, pero está restringido volver atrás",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Carrito(navController, paymentSheet)
            }

            composable(route = AppScreens.Escaner.route) {
                // Se encarga de controlar lo que ocurre al presionar el botón para volver atrás
                val callback: OnBackPressedCallback =
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            AppScreens.PantallaPrincipal.route
                        }
                    }
                BarcodeScannerScreen(navController)
            }

            composable(
                // Ruta con argumento para llevar a la pantalla del producto con su id
                route = AppScreens.PantallaProducto.route + "/{idProducto}",
                arguments = listOf(
                    navArgument("idProducto") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                // Permite volver a la pantalla anterior
                BackHandler {
                    navController.popBackStack()
                }
                val id = backStackEntry.arguments?.getString("idProducto") ?: return@composable
                PantallaProducto(idProducto = id, navController)

            }

            composable(route = AppScreens.Cupones.route) { backStackEntry ->
                // Permite volver a la pantalla anterior
                BackHandler {
                    navController.popBackStack()
                }
                Cupones(navController)
            }

            composable(route = AppScreens.Chatbot.route) {
                // Se encarga de controlar lo que ocurre al presionar el botón para volver atrás
                val callback: OnBackPressedCallback =
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            AppScreens.PantallaPrincipal.route
                        }
                    }
                Chatbot(onBack = { navController.popBackStack() })
            }

            composable("pago_stripe/{total}") { backStackEntry ->
                val total = backStackEntry.arguments?.getString("total")?.toFloatOrNull() ?: 0f
                PagoStripe(navController, total, paymentSheet)
            }

            composable(route = AppScreens.ListaCompra.route) { backStackEntry ->
                // Permite volver a la pantalla anterior
                BackHandler {
                    navController.popBackStack()
                }
                ListaCompra(navController)
            }

            composable(
                route = AppScreens.PagoRedsys.route,
                arguments = listOf(navArgument("total") { type = NavType.FloatType })
            ) { backStackEntry ->
                val total = backStackEntry.arguments?.getFloat("total") ?: 0f
                PantallaPagoRedsys(navController, total)
            }


        }
    }
}
