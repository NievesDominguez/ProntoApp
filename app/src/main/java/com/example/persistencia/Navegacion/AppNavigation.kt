package com.example.persistencia.Navegacion

import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
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
import androidx.room.Room
import com.example.persistencia.Escaner.BarcodeScannerScreen
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Pantallas.Amigos
import com.example.persistencia.Pantallas.Carrito
import com.example.persistencia.Pantallas.Catalogo
import com.example.persistencia.Pantallas.Formulario
import com.example.persistencia.Pantallas.Inicio
import com.example.persistencia.Pantallas.InmueblesTodos
import com.example.persistencia.Pantallas.MisInmuebles
import com.example.persistencia.Pantallas.PantallaPrincipal
import com.example.persistencia.Pantallas.PantallaProducto
import com.example.persistencia.Pantallas.Perfil
import com.example.persistencia.Pantallas.Registro
import com.example.persistencia.Pantallas.Resultados
import com.example.persistencia.localdb.AppDB
import com.example.persistencia.localdb.Estructura
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


@Composable
fun AppNavigation(destino: String?) { // Recibe la información del destino
    val startDestination =
        when (destino) { // Verifica con un when la información leída para determinar la ventana que se abrirá
            "Inicio" -> AppScreens.Inicio.route
            "Perfil" -> AppScreens.Perfil.route
            "Carrito" -> AppScreens.Carrito.route
            "Catalogo" -> AppScreens.Catalogo.route
            "PantallaPrincipal" -> AppScreens.PantallaPrincipal.route
            else -> AppScreens.PantallaPrincipal.route
        }

    val context = LocalContext.current
    val db = Room.databaseBuilder(context, AppDB::class.java, Estructura.DB.NAME)
        .allowMainThreadQueries().build()
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
        AppScreens.Carrito.route
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
            composable(route = AppScreens.Formulario.route) {
                BackHandler(true) {
                    Toast.makeText(
                        context,
                        "Presionaste atrás, pero está restringido volver atrás",
                        Toast.LENGTH_SHORT
                    ).show()
                } // Sirve para interceptar y manejar el evento del botón físico Atrás del dispositivo. El parámetro true indica que el sistema no ejecutará su comportamiento predeterminado (cerrar la app o regresar a la pantalla anterior).
                Formulario(navController)
            }
            composable(route = AppScreens.Resultados.route) {
                BackHandler(true) {
                    Toast.makeText(
                        context,
                        "Presionaste atrás, pero está restringido volver atrás",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Resultados(navController)
            }
            composable(route = AppScreens.Amigos.route) {
                BackHandler(true) {
                    Toast.makeText(
                        context,
                        "Presionaste atrás, pero está restringido volver atrás",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Amigos(navController)
            }

            composable(route = AppScreens.MisInmuebles.route) {
                BackHandler(true) {
                    Toast.makeText(
                        context,
                        "Presionaste atrás, pero está restringido volver atrás",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                MisInmuebles(navController)
            }

            composable(route = AppScreens.InmueblesTodos.route) {
                BackHandler(true) {
                    Toast.makeText(
                        context,
                        "Presionaste atrás, pero está restringido volver atrás",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                InmueblesTodos(navController)
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
                Carrito(navController)
            }

            composable(route = AppScreens.Escaner.route) {
//                BackHandler(true) {
//                    AppScreens.PantallaPrincipal.route
//                }
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
                route = AppScreens.PantallaProducto.route + "/{idProducto}",
                arguments = listOf(
                    navArgument("idProducto") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                BackHandler(true) {} // El usuario puede volver atras
                val id = backStackEntry.arguments?.getInt("idProducto") ?: return@composable

                PantallaProducto(idProducto = id.toString())
            }

        }
    }
}
