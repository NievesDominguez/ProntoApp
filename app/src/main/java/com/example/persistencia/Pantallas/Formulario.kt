package com.example.persistencia.Pantallas

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
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
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Formulario(navController: NavController) {
    // Variables de los datos recogidos en los formularios
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contrasena = rememberTextFieldState("")
    var passVisible by remember { mutableStateOf(false) }
    val emailPattern =
        Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}") // Patrón a seguir en el email
    // Se obtiene el contexto actual, necesario para construir la base de datos Room y mostrar mensajes Toasts.
    val context = LocalContext.current
    // Se crea una instancia de la base de datos local, indicando el contexto, la clase base de datos y el nombre del archivo, permitiendo consultas en el hilo principal.
    val db = Room.databaseBuilder(context, AppDB::class.java, Estructura.DB.NAME)
        .allowMainThreadQueries().build()
    // Instancia de la base de datos de Firebase
    val dbfire =
        FirebaseFirestore.getInstance()

    // La aplicación está corriendo en un hilo, pero cuando nosotros estamos almacenando los datos, estos datos corren en un hilo distinto al que está corriendo la aplicación y eso no es la mejor solución para producción.
    // Se soluciona temporalmente con .allowMainThreadQueries() y si no, es necesario realizar corrutinas o hilos en background.

    // Obtención del DAO, es decir, la interfaz para realizar operaciones CRUD sobre la tabla. Y la estructura.
    val usuarioD: UsuarioDao = db.usuarioDao()
    var usuarioem: UsuarioData?

    // BARRA SUPERIOR
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(90.dp),
                title = {
                    Text(text = "Registrar un usuario", fontSize = 15.sp)
                },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                        Toast.makeText(context, "Volver atrás", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )

        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Regístrate",
                fontSize = 30.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(30.dp))

            // Pedimos el nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    if (it.length < 40) {
                        nombre = it
                    }
                },
                label = { Text("Nombre del usuario") },
                modifier = Modifier.width(300.dp)
            )

            //Pedimos los apellidos
            OutlinedTextField(
                value = apellidos,
                onValueChange = {
                    if (it.length < 50) {
                        apellidos = it
                    }
                },
                label = { Text("Apellidos del usuario") },
                modifier = Modifier.width(300.dp)
            )


            // Pedimos el email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    if (it.length < 40) {
                        email = it
                    }
                },
                label = { Text("Email") },
                modifier = Modifier.width(300.dp)
            )
            //Pedimos la contraseña
            OutlinedSecureTextField(
                state = contrasena,
                label = { Text("Contraseña") },
                modifier = Modifier
                    .width(300.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    // Al hacer click se cambia su estado de visibilidad
                    IconButton(onClick = { passVisible = !passVisible }) {
                        // Icono que cambia si la contraseña está visible o no
                        Icon(
                            imageVector = if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                // Oculta o muestra la contraseña según si passVisible es true o false
                textObfuscationMode = if (passVisible) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped
            )

            //Pedimos la fecha de incorporación
            var showDatePicker by remember { mutableStateOf(false) }
            val datePickerState = rememberDatePickerState()
            val selectedDate = datePickerState.selectedDateMillis?.let {
                convertMillisToDate(it)
            } ?: ""

            Box(
                modifier = Modifier.width(300.dp)
            ) {
                // Field donde aparece la fecha
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { },
                    label = { Text("Incorporación") },
                    readOnly = true, // No se puede escribir directamente en él
                    trailingIcon = {
                        // Al hacer click en el icono muestra el selector de fecha
                        IconButton(onClick = { showDatePicker = !showDatePicker }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Selecciona una fecha"
                            )
                        }
                    },
                    modifier = Modifier.width(300.dp)
                )

                // Si el selector de fecha está visible
                if (showDatePicker) {
                    // Muestra un popup con el selector
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            // Botón dentro del propio selector
                            TextButton(
                                onClick = {
                                    showDatePicker = false
                                }
                            ) {
                                Text("Hecho")
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerState,
                            showModeToggle = false
                        )
                    }
                }
            }

            Spacer(Modifier.size(20.dp))

            // Selector para el sexo
            val radioOptions = listOf("Hombre", "Mujer", "Otro") // Lista de opciones
            val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) } // Opción seleccionada

            // Contenedor en fila para mostrar los radio buttons uno al lado del otro
            Row(
                modifier = Modifier
                    .selectableGroup() // Indica que este grupo contiene elementos seleccionables relacionados
                    .fillMaxWidth(),   // Ocupa todo el ancho disponible
                verticalAlignment = Alignment.CenterVertically, // Centra verticalmente los elementos
                horizontalArrangement = Arrangement.SpaceEvenly // Distribuye las opciones de forma uniforme
            ) {
                // Recorremos cada opción de la lista
                radioOptions.forEach { text ->
                    // Contenedor de cada opción (radio + texto)
                    Row(
                        Modifier
                            .selectable(
                                selected = (text == selectedOption), // Marca si esta opción está seleccionada
                                onClick = { onOptionSelected(text) }, // Actualiza la opción seleccionada
                                role = Role.RadioButton // Indica que este elemento actúa como radio button
                            )
                            .padding(horizontal = 8.dp), // Espaciado lateral
                        verticalAlignment = Alignment.CenterVertically // Alinea radio y texto en el centro
                    ) {
                        RadioButton(
                            selected = (text == selectedOption), // Estado visual del radio button
                            onClick = null // null recomendado para accesibilidad (el click lo gestiona el Row)
                        )
                        Text(
                            text = text, // Texto de la opción
                            style = MaterialTheme.typography.bodyLarge, // Estilo del texto
                            modifier = Modifier.padding(start = 8.dp) // Separación entre radio y texto
                        )
                    }
                }
            }



            Spacer(Modifier.size(20.dp))

            // Botón para mandar el formulario
            Button(
                onClick = {
                    usuarioem = db.usuarioDao()
                        .getUnUser(email) // Obtiene el usuario completo (si lo hay) en función del email a registrar.

                    // Validaciones básicas de campos
                    when {
                        nombre.isBlank() -> {
                            Toast.makeText(
                                context,
                                "El nombre no puede estar vacío",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        apellidos.isBlank() -> {
                            Toast.makeText(
                                context,
                                "Los apellidos no pueden estar vacíos",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        email.isBlank() -> {
                            Toast.makeText(
                                context,
                                "El email no puede estar vacío",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        !email.matches(emailPattern) -> {
                            Toast.makeText(
                                context,
                                "El email no tiene un formato válido",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        contrasena.text.length < 8 -> {
                            Toast.makeText(
                                context,
                                "La contraseña debe tener al menos 8 caracteres",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        selectedDate.isBlank() -> {
                            Toast.makeText(
                                context,
                                "La fecha de incorporación no puede estar vacía",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        usuarioem != null -> {
                            Toast.makeText(
                                context,
                                "Ya existe alguien registrado con ese email",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {

                            // Almacena los datos en la base de datos Room
                            val usr = UsuarioData(
                                nombreUsuario = nombre,
                                apellidosUsuario = apellidos,
                                incorporacionUsuario = selectedDate,
                                email = email,
                                sexo = selectedOption
                            )
                            usuarioD.nuevoUsuario(usr)
                            //db.usuarioDao().nuevoUsuario(usr) // Alternativa.


                            // GUARDADO EN FIRESTORE

                            // Se crea un mapa de Strings con los pares clave-valor para Firestore.
                            // Cada clave será un campo y su valor la información del usuario.
                            val data = hashMapOf(
                                "Nombre" to nombre,
                                "Apellidos" to apellidos,
                                "Email" to email,
                                "Password" to contrasena.text,
                                "Incorporacion" to selectedDate,
                                "Sexo" to selectedOption
                            )

                            // Se accede a la colección "usuarios" de Firestore utilizando el email como ID del documento.
                            // Si no existe, Firestore lo crea automáticamente; si existe, lo sobrescribe.
                            dbfire.collection("usuarios").document(email)
                                .set(data) // Se guarda el mapa de datos en Firestore.
                                .addOnSuccessListener {
                                    // Se ejecuta cuando los datos se han guardado correctamente en Firestore.
                                    Toast.makeText(
                                        context,
                                        "Usuario guardado en Firestore",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Se crea una instancia de la base de datos, indicando el contexto, la clase base de datos y el nombre del archivo, permitiendo consultas en el hilo principal.
                                    val dbl = Room.databaseBuilder(
                                        context,
                                        AppDB::class.java,
                                        Estructura.DB.NAME
                                    )
                                        .allowMainThreadQueries().build()

                                    var usuarioid =
                                        dbl.usuarioDao()
                                            .getUnUser(email) // Recuperamos la información del usuario en la base de datos local a través del email.

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
                                    dbl.sesionDao()
                                        .nuevaSesion(sesionData) // Insertamos la sesión utilizando el procedimiento DAO nuevaSesion().
                                    navController.navigate(route = AppScreens.Resultados.route) // Se redirige y navega a la ventana de resultados
                                    Toast.makeText(
                                        context,
                                        "Usuario inició sesión",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    // Se ejecuta cuando ocurre un error al intentar guardar en Firestore.
                                    Toast.makeText(
                                        context,
                                        "Error al guardar en Firestore: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                        }
                    }
                }
            ) {
                Text(text = "Agregar usuario")
            }

            // Botón para ir a los resultados
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

// Convierte milisegundos a fecha para el calendario
fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}


