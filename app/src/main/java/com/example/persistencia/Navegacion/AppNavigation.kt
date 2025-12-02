package com.example.persistencia.Navegacion

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.persistencia.Formulario
import com.example.persistencia.Inicio
import com.example.persistencia.Resultados
import com.example.persistencia.localdb.AppDB
import com.example.persistencia.localdb.Estructura

@Composable
fun AppNavigation(){
    val context = LocalContext.current
    val db = Room.databaseBuilder(context, AppDB::class.java, Estructura.DB.NAME).allowMainThreadQueries().build()
    var estadoSesion = db.sesionDao().getEstadoSesion()
    val navController = rememberNavController()
    //NavHost(navController = navController, startDestination = AppScreens.Formulario.route) {
    NavHost(navController = navController, startDestination = if(estadoSesion == null)AppScreens.Inicio.route else
        AppScreens.Formulario.route){
        composable (route = AppScreens.Inicio.route){
            Inicio(navController)
        }
        composable (route = AppScreens.Formulario.route){
            BackHandler(true) {
                Toast.makeText(context, "Presionaste atrás, pero está restringido volver atrás", Toast.LENGTH_SHORT).show()
            } // Sirve para interceptar y manejar el evento del botón físico Atrás del dispositivo. El parámetro true indica que el sistema no ejecutará su comportamiento predeterminado (cerrar la app o regresar a la pantalla anterior).
            Formulario(navController)
        }
        composable (route = AppScreens.Resultados.route){
            BackHandler(true) {
                Toast.makeText(context, "Presionaste atrás, pero está restringido volver atrás", Toast.LENGTH_SHORT).show()
            }
            Resultados(navController)
        }
    }
}

