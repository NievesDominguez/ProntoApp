package com.example.persistencia.Navegacion

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBar(navController: NavController) {

    // Lista de pantallas en las que aparece la barra inferior
    val items = listOf(
        BottomItem.Principal,
        BottomItem.Catalogo,
        BottomItem.Carrito,
        BottomItem.Perfil
    )

    // Barra de navegación, aquí se determinan sus características
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState() // Indica la pantalla actual
        val currentRoute = navBackStackEntry?.destination?.route // Indica la ruta a la pantalla actual

        // Añade cada pantalla de la lista a la barra inferior
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route, // La ruta al item
                onClick = {
                    navController.navigate(item.route) { // Va a la ruta seleccionada
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
