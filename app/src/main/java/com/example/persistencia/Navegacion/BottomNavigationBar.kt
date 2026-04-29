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
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShoppingBag

@Composable
fun BottomBar(navController: NavController) {
    val colors = MaterialTheme.colorScheme

    // Items que aparecen en el bottombar
    val items = listOf(
        BottomItem.Principal,
        BottomItem.Catalogo,
        BottomItem.Carrito,
        BottomItem.ListaCompra,
        BottomItem.Perfil
    )

    // Barra de navegación inferior
    NavigationBar(
        containerColor = colors.surface,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                // Al hacer click lleva a la pantalla correspondiente
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
                    selectedIconColor = Color.White,
                    selectedTextColor = colors.onSurface.copy(alpha = 0.6f),
                    unselectedIconColor = colors.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = colors.onSurface.copy(alpha = 0.6f),
                    indicatorColor = colors.primary // Fondo sólido del elemento seleccionado
                )
            )
        }
    }
}

// Cada item del bottombar con su icono y ruta a la que lleva
sealed class BottomItem(val route: String, val icon: ImageVector, val label: String) {
    object Principal : BottomItem(AppScreens.PantallaPrincipal.route, Icons.Default.Home, "Inicio")
    object Catalogo : BottomItem(AppScreens.Catalogo.route, Lucide.ShoppingBag, "Catálogo")
    object Carrito : BottomItem(AppScreens.Carrito.route, Icons.Default.ShoppingCart, "Carrito")
    object Perfil : BottomItem(AppScreens.Perfil.route, Icons.Default.Person, "Perfil")
    object ListaCompra : BottomItem(AppScreens.ListaCompra.route, Icons.Default.List, "Lista")
}