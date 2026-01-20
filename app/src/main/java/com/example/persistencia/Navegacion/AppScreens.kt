package com.example.persistencia.Navegacion

sealed class AppScreens (val route: String) {
    object Inicio: AppScreens("Inicio")
    object Formulario: AppScreens("Formulario")
    object Resultados: AppScreens("Resultados")
    object Amigos: AppScreens("Amigos")
    object MisInmuebles: AppScreens("MisInmuebles")
    object InmueblesTodos: AppScreens("InmueblesTodos")
    object PantallaPrincipal: AppScreens("PantallaPrincipal")

    companion object {
    }
}

