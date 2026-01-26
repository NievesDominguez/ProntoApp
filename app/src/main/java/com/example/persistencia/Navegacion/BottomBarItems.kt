package com.example.persistencia.Navegacion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomItem(val route: String, val icon: ImageVector, val label: String) {
    object Principal : BottomItem(AppScreens.PantallaPrincipal.route, Icons.Default.Home, "Inicio")
    object Catalogo : BottomItem(AppScreens.Catalogo.route, Icons.Default.List, "Catálogo")
    object Carrito : BottomItem(AppScreens.Carrito.route, Icons.Default.ShoppingCart, "Carrito")
    object Perfil : BottomItem(AppScreens.Perfil.route, Icons.Default.Person, "Perfil")
}
