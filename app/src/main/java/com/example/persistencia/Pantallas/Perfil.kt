package com.example.persistencia.Pantallas

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

//--------------------------------------------------------------------------
// PANTALLA DE PERFIL
//--------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Perfil(navController: NavController) {

    val context = LocalContext.current
    val usuario = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    // Fondo degradado original
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFD13CF2), Color(0xFF6C3AEC))
    )

    // Estados principales
    var modoEdicion by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    // Datos del usuario
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var nuevaPassword by remember { mutableStateOf("") }
    var fotoFirestore by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var showConfirmDialog by remember { mutableStateOf(false) }

    // Cargar datos del usuario desde Firestore
    LaunchedEffect(usuario?.uid) {
        usuario?.uid?.let { uid ->
            firestore.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    nombre = doc.getString("nombre") ?: ""
                    apellidos = doc.getString("apellidos") ?: ""
                    telefono = doc.getString("telefono") ?: ""
                    fotoFirestore = doc.getString("foto")
                }
        }
    }

    // Selector de imágenes
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),

                // Botones editar / guardar / cancelar
                actions = {
                    if (modoEdicion) {
                        IconButton(onClick = {
                            modoEdicion = false
                            nuevaPassword = ""
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cancelar",
                                tint = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (modoEdicion) showConfirmDialog = true
                            else modoEdicion = true
                        }
                    ) {
                        Icon(
                            imageVector = if (modoEdicion) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(24.dp))

                // Foto y nombre del usuario
                val imagenMostrar = when {
                    imageUri != null -> imageUri
                    !fotoFirestore.isNullOrBlank() -> fotoFirestore
                    usuario?.photoUrl != null -> usuario.photoUrl
                    else -> "https://www.shutterstock.com/image-vector/default-avatar-profile-icon-social-600nw-1906669723.jpg"
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = imagenMostrar,
                            contentDescription = "Foto perfil",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "$nombre $apellidos",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    // Botón para cambiar la foto
                    if (modoEdicion) {
                        TextButton(onClick = { launcher.launch("image/*") }) {
                            Text("Cambiar foto", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ------------------------------------------------------------------
                // TABS SUPERIORES
                // ------------------------------------------------------------------

                val tabs = listOf("Datos", "Compras", "Ajustes")

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, color = Color.White) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ------------------------------------------------------------------
                // CONTENIDO SEGÚN TAB
                // ------------------------------------------------------------------

                when (selectedTab) {

                    // TAB DATOS
                    0 -> {
                        if (!modoEdicion) {
                            PerfilView(
                                nombre = nombre,
                                apellidos = apellidos,
                                telefono = telefono,
                                email = usuario?.email
                            )
                        } else {
                            PerfilEdit(
                                nombre = nombre,
                                apellidos = apellidos,
                                telefono = telefono,
                                nuevaPassword = nuevaPassword,
                                onNombre = { nombre = it },
                                onApellidos = { apellidos = it },
                                onTelefono = { telefono = it },
                                onPassword = { nuevaPassword = it }
                            )
                        }
                    }

                    // TAB COMPRAS
                    1 -> TarjetaPlaceholder("Datos de compras.")

                    // TAB AJUSTES
                    2 -> TarjetaPlaceholder("Opciones de ajuste próximamente.")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón para cerrar sesión
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") { popUpTo(0) }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF6C3AEC)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar sesión")
                }
            }

            // Diálogo de confirmación
            if (showConfirmDialog) {
                ConfirmacionDialog(
                    nuevaPassword = nuevaPassword,
                    onDismiss = { showConfirmDialog = false },
                    onConfirm = { passActual, repetirNueva ->

                        val credential = EmailAuthProvider
                            .getCredential(usuario?.email!!, passActual)

                        // Reautentica y cambia los datos
                        usuario.reauthenticate(credential)
                            .addOnSuccessListener {

                                val datos = hashMapOf(
                                    "nombre" to nombre,
                                    "apellidos" to apellidos,
                                    "telefono" to telefono
                                )

                                firestore.collection("usuarios")
                                    .document(usuario.uid)
                                    .update(datos as Map<String, Any>)

                                if (nuevaPassword.isNotEmpty()) {
                                    usuario.updatePassword(nuevaPassword)
                                }

                                nuevaPassword = ""
                                modoEdicion = false
                                showConfirmDialog = false

                                Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Contraseña incorrecta", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                )
            }
        }
    }
}

// --------------------------------------------------------------------------
// VISTA DE PERFIL
// --------------------------------------------------------------------------
@Composable
fun PerfilView(nombre: String, apellidos: String, telefono: String, email: String?) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {

        // Datos del usuario
        Text("Nombre:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(nombre, fontSize = 18.sp, color = Color.White.copy(alpha = 0.9f))

        Spacer(modifier = Modifier.height(12.dp))

        Text("Apellidos:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(apellidos, fontSize = 18.sp, color = Color.White.copy(alpha = 0.9f))

        Spacer(modifier = Modifier.height(12.dp))

        Text("Teléfono:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(
            telefono.ifBlank { "No especificado" }, // Si no hay teléfono, pone "No especificado"
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.9f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Correo electrónico:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(email ?: "", fontSize = 18.sp, color = Color.White.copy(alpha = 0.9f))
    }
}

// --------------------------------------------------------------------------
// VISTA DE PERFIL (modo edición)
// --------------------------------------------------------------------------
@Composable
fun PerfilEdit(
    nombre: String,
    apellidos: String,
    telefono: String,
    nuevaPassword: String,
    onNombre: (String) -> Unit,
    onApellidos: (String) -> Unit,
    onTelefono: (String) -> Unit,
    onPassword: (String) -> Unit
) {
    // Estado de scroll para el contenido interno
    val scrollState = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        // La tarjeta es scrolleable
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = nombre,
                onValueChange = onNombre,
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = apellidos,
                onValueChange = onApellidos,
                label = { Text("Apellidos") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = onTelefono,
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = nuevaPassword,
                onValueChange = onPassword,
                label = { Text("Nueva contraseña (opcional)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}


/* --------------------------------------------------------------------------
   TARJETA PLACEHOLDER PARA TABS VACÍOS
   -------------------------------------------------------------------------- */

@Composable
fun TarjetaPlaceholder(texto: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Text(
            texto,
            modifier = Modifier.padding(24.dp),
            fontSize = 16.sp,
            color = Color.DarkGray
        )
    }
}

/* --------------------------------------------------------------------------
   DIÁLOGO DE CONFIRMACIÓN
   -------------------------------------------------------------------------- */

@Composable
fun ConfirmacionDialog(
    nuevaPassword: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var passActual by remember { mutableStateOf("") }
    var repetirNueva by remember { mutableStateOf("") }

    val errorPassActual = passActual.isBlank()
    val errorRepetir = nuevaPassword.isNotEmpty() &&
            repetirNueva.isNotEmpty() &&
            repetirNueva != nuevaPassword

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Confirmar cambios",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                OutlinedTextField(
                    value = passActual,
                    onValueChange = { passActual = it },
                    label = { Text("Contraseña actual") },
                    isError = errorPassActual,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (nuevaPassword.isNotEmpty()) {

                    OutlinedTextField(
                        value = repetirNueva,
                        onValueChange = { repetirNueva = it },
                        label = { Text("Repetir nueva contraseña") },
                        isError = errorRepetir,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorRepetir) {
                        Text(
                            text = "Las contraseñas no coinciden",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (errorPassActual) return@Button
                    if (errorRepetir) return@Button
                    onConfirm(passActual, repetirNueva)
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
