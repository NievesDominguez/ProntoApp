package com.example.persistencia.Navegacion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBar(navController: NavController) {
    val colors = MaterialTheme.colorScheme

    // Lista de pantallas en las que aparece la barra inferior
    val items = listOf(
        BottomItem.Principal,
        BottomItem.Catalogo,
        BottomItem.Carrito,
        BottomItem.Perfil
    )

    // Barra de navegación con colores del tema
    NavigationBar(
        containerColor = colors.surface,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    unselectedIconColor = colors.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = colors.onSurface.copy(alpha = 0.6f),
                    indicatorColor = colors.primaryContainer
                )
            )
        }
    }
}

// Pantallas de la barra inferior (sin cambios)
sealed class BottomItem(val route: String, val icon: ImageVector, val label: String) {
    object Principal : BottomItem(AppScreens.PantallaPrincipal.route, Icons.Default.Home, "Inicio")
    object Catalogo : BottomItem(AppScreens.Catalogo.route, Icons.Default.List, "Catálogo")
    object Carrito : BottomItem(AppScreens.Carrito.route, Icons.Default.ShoppingCart, "Carrito")
    object Perfil : BottomItem(AppScreens.Perfil.route, Icons.Default.Person, "Perfil")
}