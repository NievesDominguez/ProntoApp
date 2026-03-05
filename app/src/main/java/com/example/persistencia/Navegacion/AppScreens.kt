package com.example.persistencia.Navegacion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ReceiptEuro
import com.composables.icons.lucide.TicketPercent

sealed class AppScreens (val route: String, val icon: ImageVector) {
    object Inicio: AppScreens("Inicio",Icons.Default.LibraryBooks)
    object Formulario: AppScreens("Formulario",Icons.Default.LibraryBooks)
    object Resultados: AppScreens("Resultados",Icons.Default.LibraryBooks)
    object Amigos: AppScreens("Amigos",Icons.Default.LibraryBooks)
    object MisInmuebles: AppScreens("MisInmuebles",Icons.Default.LibraryBooks)
    object InmueblesTodos: AppScreens("InmueblesTodos",Icons.Default.LibraryBooks)
    object PantallaPrincipal: AppScreens("PantallaPrincipal",Icons.Default.Home)
    object Registro: AppScreens("Registro",Icons.Default.LibraryBooks)
    object Perfil: AppScreens("Perfil",Icons.Default.Person)
    object Carrito: AppScreens("Carrito",Icons.Default.ShoppingCart)
    object Catalogo: AppScreens("Catalogo",Icons.Default.LibraryBooks)
    object BottomNavigationBar: AppScreens("BottomNavigationBar",Icons.Default.LibraryBooks)
    object Escaner: AppScreens("BarcodeScannerScreen",Icons.Default.Camera)
    object PantallaProducto: AppScreens("PantallaProducto",Icons.Default.ShoppingBasket)
    object Cupones: AppScreens("Cupones",Lucide.TicketPercent)

    companion object {
    }
}

