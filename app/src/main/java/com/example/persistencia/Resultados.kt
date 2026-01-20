package com.example.persistencia

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.example.persistencia.Navegacion.AppScreens
import com.example.persistencia.localdb.AppDB
import com.example.persistencia.localdb.Estructura
import com.example.persistencia.localdb.UsuarioDao
import com.example.persistencia.localdb.UsuarioData
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Resultados(navController: NavController) {

    // Se obtiene el contexto actual, necesario para construir la base de datos Room y mostrar Toasts.
    val context = LocalContext.current

    // --------------------------------------------------------------------------
    // PARTE DE ROOM
    // --------------------------------------------------------------------------

    val db = Room.databaseBuilder(context, AppDB::class.java, Estructura.DB.NAME)
        .allowMainThreadQueries().build()

    // Ejecuta la consulta definida en el DAO que recupera todos los usuarios de la tabla.
    var listaUsuarios = db.usuarioDao().getListaUsuarios().toMutableList()
    // Consulta el id de la última sesión iniciada y a través de él consigue los datos del usuario
    var usuarioSesion = db.sesionDao().getDatosSesion()
    var usuarioActivo = db.usuarioDao().getUsuario(usuarioSesion)

    // Variables de los datos recogidos en los formularios
    var nombre by remember { mutableStateOf(usuarioActivo?.nombreUsuario ?: "Nombre") }
    var apellidos by remember { mutableStateOf(usuarioActivo?.apellidosUsuario ?: "Apellidos") }
    var email by remember { mutableStateOf(usuarioActivo?.email ?: "Email") }
    var contrasena = rememberTextFieldState("")
    var passVisible by remember { mutableStateOf(false) }
    val emailPattern =
        Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}") // Patrón a seguir en el email
    var sexoUsuario by remember { mutableStateOf(usuarioActivo?.sexo ?: "") }

    var incorporacionUsuario by remember {
        mutableStateOf(usuarioActivo?.incorporacionUsuario ?: "")
    }


    // Instancia de la base de datos de Firebase
    val dbfire =
        FirebaseFirestore.getInstance()

    // La aplicación está corriendo en un hilo, pero cuando nosotros estamos almacenando los datos, estos datos corren en un hilo distinto al que está corriendo la aplicación y eso no es la mejor solución para producción.
    // Se soluciona temporalmente con .allowMainThreadQueries() y si no, es necesario realizar corrutinas o hilos en background.

    // Obtención del DAO, es decir, la interfaz para realizar operaciones CRUD sobre la tabla. Y la estructura.
    val usuarioD: UsuarioDao = db.usuarioDao()
    var usuarioem: UsuarioData?


    /*
    // Lista para guardar los datos de firebase
    val listaUsuarios = remember { mutableStateListOf<UsuarioData>() }

    // Se obtienen los datos de firestore*/
    // Con LaunchedEffect se ejecuta el código solo una vez para que no haya duplicados
    LaunchedEffect(Unit) {
        // Se obtiene una instancia de acceso a Firestore.
        val dbfire = FirebaseFirestore.getInstance()

        // Acceso a la colección "usuarios" almacenada en Firestore.
        dbfire.collection("usuarios")
            .get()
            .addOnSuccessListener { result ->
                listaUsuarios.clear()

                // Recorre todos los documentos encontrados en la colección.
                for (doc in result) {

                    // Convierte los datos del documento en un objeto UsuarioData.
                    val usr = UsuarioData(
                        idUsuario = result.indexOf(doc) + 1, // El id de usuario será el número del elemento
                        nombreUsuario = doc.getString("Nombre") ?: "",
                        apellidosUsuario = doc.getString("Apellidos") ?: "",
                        email = doc.getString("Email") ?: "",
                        incorporacionUsuario = doc.getString("Incorporacion") ?: "",
                        sexo = doc.getString("Sexo") ?: "",
                    )

                    listaUsuarios.add(usr)
                }

                Toast.makeText(context, "Datos cargados", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error leyendo Firestore: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
    }

    // --------------------------------------------------------------------------
    // BARRA SUPERIOR
    // --------------------------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(90.dp),
                title = {
                    // Centra verticalmente el texto dentro del espacio disponible
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Mi perfil",
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),

                // Aquí estará el icono para volver a la pantalla principal
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(route = AppScreens.PantallaPrincipal.route)
                        Toast.makeText(context, "Pantalla principal", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Lucide.House,
                            contentDescription = "Inicio",
                            tint = Color.White
                        )
                    }
                },

                // Icono alineado a la derecha
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(route = AppScreens.Inicio.route)
                            Toast.makeText(context, "Volver atrás", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.LogOut,
                            contentDescription = "backIcon",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        var editar by rememberSaveable { mutableStateOf(false) } // Variable para mostrar una imagen o no
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
            ) {

                val iniciales = buildString {
                    append(usuarioActivo?.nombreUsuario?.first()?.uppercase())
                }

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            color = Color.Blue,
                            shape = CircleShape
                        ),

                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iniciales,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(15.dp, 5.dp)
                        .width(280.dp)
                ) {
                    // Título del item (su nombre)
                    Text(
                        modifier = Modifier,
                        text = (usuarioActivo?.nombreUsuario
                            ?: "Nombre") + " " + (usuarioActivo?.apellidosUsuario ?: "Apellido"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    // Cuerpo del item
                    Text(
                        modifier = Modifier,
                        text = usuarioActivo?.email ?: "Email",
                        fontSize = 20.sp
                    )
                }
                IconButton(onClick = {
                    editar =
                        !editar // Al hacer click alterna entre mostrar las opciones de edición o no

                    Toast.makeText(context, "Editar usuario", Toast.LENGTH_SHORT).show()
                }
                ) {
                    Icon(Icons.Filled.Edit, "edit")
                }
            }
            Row() {
                FilledTonalButton(
                    onClick = {
                        navController.navigate(route = AppScreens.Amigos.route)
                    },
                ) {
                    Text(
                        text = "Ver amistades",
                        color = Color.Black
                    )
                }
                FilledTonalButton(
                    onClick = {
                        navController.navigate(route = AppScreens.MisInmuebles.route)
                    },
                ) {
                    Text(
                        text = "Ver inmuebles",
                        color = Color.Black
                    )
                }
            }

        }

        // Se muestra solo al hacer click en editar
        if (editar) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .background(Color(0xE6F2F0F0))
                        .width(350.dp)
                        .padding(20.dp),
                ) {

                    Column(
                        modifier = Modifier.fillMaxWidth(), // Necesario para alinear la X a la derecha
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // Fila para alinear la X a la derecha
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = {
                                    editar = !editar // Alterna entre mostrar/ocultar edición
                                }
                            ) {
                                Icon(Lucide.X, "edit")
                            }
                        }

                        Text(
                            text = "Hola, $nombre",
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp
                        )


                        //Spacer(modifier = Modifier.size(30.dp))

                        // Pedimos el nombre
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = {
                                if (it.length < 40) {
                                    nombre = it
                                }
                            },
                            label = { Text("Nombre del usuario") },
                            modifier = Modifier.width(300.dp),
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

                        // Estado que controla si se muestra el selector
                        var showDatePicker by remember { mutableStateOf(false) }

                        // Convertimos la fecha del usuario (String) a millis
                        val initialDateMillis = remember(incorporacionUsuario) {
                            convertDateStringToMillis(incorporacionUsuario) // tu función de conversión
                        }

                        // Creamos el estado del DatePicker con la fecha inicial
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = initialDateMillis
                        )

                        // Convertimos la fecha seleccionada a texto para mostrarla en el campo
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
                        val (selectedOption, onOptionSelected) = remember { mutableStateOf(if (sexoUsuario in radioOptions) sexoUsuario else radioOptions[0]) } // Opción seleccionada

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


                                    else -> {

                                        // Almacena los datos en la base de datos Room
                                        val usr = UsuarioData(
                                            nombreUsuario = nombre,
                                            apellidosUsuario = apellidos,
                                            incorporacionUsuario = selectedDate,
                                            email = email,
                                            sexo = selectedOption
                                        )
                                        usuarioD.editar(
                                            nombre,
                                            apellidos,
                                            email,
                                            selectedDate,
                                            selectedOption,
                                            usuarioActivo?.email
                                        )
                                        //usuarioD.update(usr)
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
                                                    "Usuario guardado",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.navigate(route = AppScreens.Resultados.route) // Lleva a la pantalla de resultados
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
                            Text(text = "Editar datos")
                        }

                    }
                }
            }
        }


    }
}

fun convertDateStringToMillis(dateString: String): Long? {
    return try {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formatter.parse(dateString)?.time
    } catch (e: Exception) {
        null
    }
}




