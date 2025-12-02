package com.example.persistencia

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.room.Room
import com.example.persistencia.Navegacion.AppScreens
import com.example.persistencia.localdb.AppDB
import com.example.persistencia.localdb.Estructura
import com.example.persistencia.localdb.UsuarioDao
import com.example.persistencia.localdb.UsuarioData
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Formulario(navController: NavController) {
    // Variables de los datos recogidos en los formularios
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var incorporacion by remember { mutableStateOf("") }
    var contrasena = rememberTextFieldState("")
    var passVisible by remember { mutableStateOf(false) }
    val emailPattern = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}") // Patrón a seguir en el email
    // Se obtiene el contexto actual, necesario para construir la base de datos Room y mostrar mensajes Toasts.
    val context = LocalContext.current
    // Se crea una instancia de la base de datos local, indicando el contexto, la clase base de datos y el nombre del archivo, permitiendo consultas en el hilo principal.
    val db = Room.databaseBuilder(context, AppDB::class.java, Estructura.DB.NAME)
        .allowMainThreadQueries().build()
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
                modifier = Modifier.height(60.dp),
                title = {
                    Text(text = "Registrar un usuario", fontSize = 15.sp)
                },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pedimos el nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    if(it.length < 40) {
                        nombre = it
                    }},
                label = { Text("Nombre del usuario") },
                modifier = Modifier.width(300.dp)
            )

            //Pedimos los apellidos
            OutlinedTextField(
                value = apellidos,
                onValueChange = {
                    if(it.length < 50) {
                        apellidos = it
                    }},
                label = { Text("Apellidos del usuario") },
                modifier = Modifier.width(300.dp)
            )
            // Pedimos el email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    if(it.length < 40) {
                        email = it
                    }},
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
                    IconButton(onClick = { passVisible = !passVisible }) {
                        Icon(
                            imageVector = if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                textObfuscationMode = if (passVisible) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped
            )

            val calendar = Calendar.getInstance()
            //Pedimos la fecha de incorporación
            var incorporacion by remember { mutableStateOf("") }

            var showDatePicker by remember { mutableStateOf(false) }
            val datePickerState = rememberDatePickerState()
            val selectedDate = datePickerState.selectedDateMillis?.let {
                convertMillisToDate(it)
            } ?: ""

            Box(
                modifier = Modifier.width(300.dp)
            ) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { },
                    label = { Text("Incorporación") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = !showDatePicker }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select date"
                            )
                        }
                    },
                    modifier = Modifier.width(300.dp)
                )

                if (showDatePicker) {
                    Popup(
                        onDismissRequest = { showDatePicker = false },
                        alignment = Alignment.TopStart
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = 64.dp)
                                .shadow(elevation = 4.dp)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp)
                        ) {
                            DatePicker(
                                state = datePickerState,
                                showModeToggle = false
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.size(20.dp))

            Button(
                onClick = {
                    usuarioem = db.usuarioDao().getUnUser(email) // Obtiene el usuario completo (si lo hay) en función del email a registrar.

                    // Validaciones básicas de campos
                    when {
                        nombre.isBlank() -> {
                            Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                        }
                        apellidos.isBlank() -> {
                            Toast.makeText(context, "Los apellidos no pueden estar vacíos", Toast.LENGTH_SHORT).show()
                        }
                        email.isBlank() -> {
                            Toast.makeText(context, "El email no puede estar vacío", Toast.LENGTH_SHORT).show()
                        }
                        !email.matches(emailPattern) -> {
                            Toast.makeText(context, "El email no tiene un formato válido", Toast.LENGTH_SHORT).show()
                        }
                        contrasena.text.length < 8 -> {
                            Toast.makeText(context, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                        }
                        selectedDate.isBlank() -> {
                            Toast.makeText(context, "La fecha de incorporación no puede estar vacía", Toast.LENGTH_SHORT).show()
                        }
                        usuarioem != null -> {
                            Toast.makeText(context, "Ya existe alguien registrado con ese email", Toast.LENGTH_SHORT).show()
                        }
                        else -> {

                            // Almacena los datos en la base de datos Room
                            val usr = UsuarioData(
                                nombreUsuario = nombre,
                                apellidosUsuario = apellidos,
                                incorporacionUsuario = selectedDate,
                                email = email
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
                                "Incorporacion" to selectedDate
                            )

                            // Se accede a la colección "usuarios" de Firestore utilizando el email como ID del documento.
                            // Si no existe, Firestore lo crea automáticamente; si existe, lo sobrescribe.
                            dbfire.collection("usuarios").document(email)
                                .set(data) // Se guarda el mapa de datos en Firestore.
                                .addOnSuccessListener {
                                    // Se ejecuta cuando los datos se han guardado correctamente en Firestore.
                                    Toast.makeText(context, "Usuario guardado en Firestore", Toast.LENGTH_SHORT).show()
                                    navController.navigate(route = AppScreens.Resultados.route)
                                }
                                .addOnFailureListener { e ->
                                    // Se ejecuta cuando ocurre un error al intentar guardar en Firestore.
                                    Toast.makeText(context, "Error al guardar en Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                                }

                        }
                    }
                }
            ) {
                Text(text = "Agregar usuario")
            }

            Button(
                onClick = {
                    Toast.makeText(context, "Ir a resultados", Toast.LENGTH_SHORT).show()
                    navController.navigate(route = AppScreens.Resultados.route)
                }) {
                Text(text = "Ir a resultados")
            }

            Button(
                onClick = {
                    Toast.makeText(context, "Cerrar sesión", Toast.LENGTH_SHORT).show()
                    navController.navigate(route = AppScreens.Inicio.route)
                }) {
                Text(text = "Cerrar sesión")
            }
        }
    }
}

@Composable
fun DatePickerFieldToModal(modifier: Modifier = Modifier) {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showModal by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate?.let { convertMillisToDate(it) } ?: "",
        onValueChange = { },
        label = { Text("Incorporación") },
        placeholder = { Text("DD/MM/YYYY") },
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Selecciona una fecha")
        },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(selectedDate) {
                awaitEachGesture {
                    // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                    // in the Initial pass to observe events before the text field consumes them
                    // in the Main pass.
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        showModal = true
                    }
                }
            }
    )

    if (showModal) {
        DatePickerModal(
            onDateSelected = {
                val it = null
                selectedDate = it
            },
            onDismiss = { showModal = false }
        )
    }
}

@Composable
fun DatePickerModal(onDateSelected: () -> Unit, onDismiss: () -> Unit) {
    TODO("Not yet implemented")
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}


