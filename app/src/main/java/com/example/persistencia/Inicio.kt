package com.example.persistencia

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import com.example.persistencia.Navegacion.AppScreens
import com.example.persistencia.localdb.AppDB
import com.example.persistencia.localdb.Estructura
import com.example.persistencia.localdb.SesionData
import com.example.persistencia.localdb.UsuarioDao
import com.example.persistencia.localdb.UsuarioData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inicio(navController: NavController) {
    // BBDD Firebase
    val dbFirebase = Firebase.firestore // Se crea una instancia de la base de datos en la nube para consultar posteriormente los usuarios registrados en la nube y poder acceder a la colección creada.
    var email by remember { mutableStateOf("") }
    var contrasena = rememberTextFieldState("")
    var passVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Se crea una instancia de la base de datos, indicando el contexto, la clase base de datos y el nombre del archivo, permitiendo consultas en el hilo principal.
    val dbl = Room.databaseBuilder(context, AppDB::class.java, Estructura.DB.NAME)
        .allowMainThreadQueries().build()
    // La aplicación está corriendo en un hilo, pero cuando nosotros estamos almacenando los datos, estos datos corren en un hilo distinto al que está corriendo la aplicación y eso no es la mejor solución para producción.
    // Se soluciona temporalmente con .allowMainThreadQueries() y si no, es necesario realizar corrutinas o hilos en background.

    // Obtención del DAO, es decir, la interfaz para realizar operaciones CRUD sobre la tabla de la base de datos local.
    var usuarioid: UsuarioData?


    // BARRA SUPERIOR
    Scaffold(
        topBar = {
            TopAppBar(modifier = Modifier.height(60.dp),
                title = {
                    Text(text = "Iniciar sesión", fontSize = 15.sp)
                },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White)
            )
        }){ innerPadding->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(text = "Bienvenid@",
                fontSize = 30.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(30.dp))
            //Pedimos el email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    if(it.length < 30) {
                        email = it
                    }},
                label = { Text("Email") },
                modifier = Modifier.width(300.dp)
            )
            Spacer(modifier = Modifier.height(15.dp))

            //Pedimos la contraseña
            OutlinedSecureTextField(
                state = contrasena,
                label = { Text("Contraseña") },
                modifier = Modifier
                    .width(300.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passVisible = !passVisible }) {
                        Icon(
                            imageVector = if (passVisible) Icons.Filled.Visibility else
                                Icons.Filled.VisibilityOff,
                            contentDescription = if (passVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                textObfuscationMode = if (passVisible) TextObfuscationMode.Visible else
                    TextObfuscationMode.RevealLastTyped
            )
            Spacer(Modifier.size(15.dp))

            Button(onClick = {
                if(contrasena.text.isBlank() || email.isBlank()) { // Comprueba si hay campos con algún campo en blanco (vacío) y si lo hay mostrará un mensaje Toast.
                    Toast.makeText(context,"No puede haber campos en blanco",Toast.LENGTH_SHORT).show()
                }
                // Si los campos están completos, iniciamos sesión
                else {
                    dbFirebase.collection("usuarios").document(email).get().addOnSuccessListener { usuario -> // Se realiza una consulta en la base de datos en la nube (Firestore) para poder recuperar el documento cuyo email coincide con el tecleado.
                        val usr = usuario.data // Se crea un mapa mutable con los datos del usuario si el documento existe o nulo si no lo recupera.
                        if (usr != null) { // Si existe un usuario en la bd de la nube, extraemos el email y la contraseña del usuario extraído a través de la nube.
                            val emailUsr = usr["Email"] as? String
                            val pwd = usr["Password"] as? String
                            // Validamos que hemos recuperado un usuario con datos no nulos
                            if (emailUsr != null && pwd != null) {
                                // Obtener usuarioid sólo si hay un email registrado y se recuperan los datos previamente.
                                usuarioid = dbl.usuarioDao().getUnUser(email) // Recuperamos la información del usuario en la base de datos local a través del email.
                                Log.d("LOGIN", "pwd: ${contrasena.text}, usuarioid: ${usuarioid}")
                                if (pwd == contrasena.text && usuarioid != null) { // Si además coincide la contraseña almacenada con la introducida, generamos la estructura.
                                    val formatFecha = SimpleDateFormat(
                                        "dd-MM-yyyy",
                                        Locale.getDefault()
                                    ) // Creamos el formato de la fecha, utilizando patrones.
                                    val fecha = formatFecha.format(Date())
                                    val sesionData =
                                        SesionData( // Creamos el objeto de sesión para insertar en la bd.
                                            idUsuario = usuarioid!!.idUsuario,
                                            fechaInicio = fecha
                                        )
                                    dbl.sesionDao().nuevaSesion(sesionData) // Insertamos la sesión utilizando el procedimineto DAO nuevaSesion().
                                    navController.navigate(route = AppScreens.Formulario.route) // Se redirige y navega a la ventana principal del formulario.
                                    Toast.makeText(context,"Usuario inició sesión",Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context,"Credenciales inválidas",Toast.LENGTH_SHORT).show()
                                    Log.d("LOGIN", "Usuario Firestore: ${usuario.data}")
                                }
                            } else {
                                Toast.makeText(context,"Datos del usuario incompletos",Toast.LENGTH_SHORT).show()
                                Log.d("LOGIN", "Usuario Firestore: ${usuario.data}")
                            }
                        } else {
                            Toast.makeText(context, "Usuario no existe", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(context, "Error en la consulta", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text(text = "Iniciar sesion")
            }

            Button(
                onClick = {
                    Toast.makeText(context, "Ir a formulario", Toast.LENGTH_SHORT).show()
                    navController.navigate(route = AppScreens.Formulario.route)
                }) {
                Text(text = "Ir a formulario")
            }

            Button(
                onClick = {
                    Toast.makeText(context, "Ir a resultados", Toast.LENGTH_SHORT).show()
                    navController.navigate(route = AppScreens.Resultados.route)
                }) {
                Text(text = "Ir a resultados")
            }
        }
    }
}






/*
            Column(
            modifier = Modifier
                .fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = {
                    Toast.makeText(context, "Ir a formulario", Toast.LENGTH_SHORT).show()
                    navController.navigate(route = AppScreens.Formulario.route)
                }) {
                Text(text = "Ir a formulario")
            }

            Button(
                onClick = {
                    Toast.makeText(context, "Ir a resultados", Toast.LENGTH_SHORT).show()
                    navController.navigate(route = AppScreens.Resultados.route)
                }) {
                Text(text = "Ir a resultados")
            }
        }
    }
}*/