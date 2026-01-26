package com.example.persistencia.Pantallas

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.example.persistencia.Navegacion.AppScreens

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MainScreen() {

    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),

    ) { innerPadding ->

        val graph =
            navController.createGraph(startDestination = AppScreens.PantallaPrincipal.route) {
                composable(route = AppScreens.PantallaPrincipal.route) {
                    PantallaPrincipal(
                        navController = navController
                    )
                }
                composable(route = AppScreens.Carrito.route) {
                    Carrito(
                        navController = navController
                    )
                }
                composable(route = AppScreens.Catalogo.route) {
                    Catalogo(
                        navController = navController
                    )
                }
                composable(route = AppScreens.Perfil.route) {
                    Perfil(
                        navController = navController
                    )
                }
            }
        NavHost(
            navController = navController,
            graph = graph,
            modifier = Modifier.padding(innerPadding)
        )

    }
}