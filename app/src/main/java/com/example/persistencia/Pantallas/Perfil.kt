package com.example.persistencia.Pantallas

import android.content.Context
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.persistencia.Herramientas.LocalThemeManager
import com.example.persistencia.Herramientas.ThemePreference
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import com.example.persistencia.Navegacion.AppScreens
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.example.persistencia.BuildConfig

//--------------------------------------------------------------------------
// PANTALLA DE PERFIL
//--------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun Perfil(navController: NavController) {

    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val usuario = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    val themeManager = LocalThemeManager.current

    // Estados principales
    var modoEdicion by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }

    // Datos del usuario
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var nuevaPassword by remember { mutableStateOf("") }
    var fotoFirestore by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var showConfirmDialog by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) } // Variable que determina si se muestra el dialogo para cerrar sesión o no

    // Cargar datos del usuario desde Firestore
    LaunchedEffect(usuario?.uid) {
        cargando = true
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
        delay(300L) // Retardo para la carga
        cargando = false
    }

    // Selector de imágenes
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Modificador para el degradado de los bordes
    val backgroundModifier = Modifier
        .fillMaxSize()
        .background(colors.background)
        .drawBehind {
            val edgeWidth = with(density) { 25.dp.toPx() }
            val primaryColor = colors.primary.copy(alpha = 0.1f)
            val secondaryColor = colors.secondary.copy(alpha = 0.05f)
            val width = size.width
            val height = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    listOf(primaryColor, Color.Transparent),
                    0f,
                    edgeWidth
                ),
                topLeft = Offset(0f, 0f), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, primaryColor),
                    height - edgeWidth,
                    height
                ),
                topLeft = Offset(0f, height - edgeWidth), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(secondaryColor, Color.Transparent),
                    0f,
                    edgeWidth
                ),
                topLeft = Offset(0f, 0f), size = Size(edgeWidth, height)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, secondaryColor),
                    width - edgeWidth,
                    width
                ),
                topLeft = Offset(width - edgeWidth, 0f), size = Size(edgeWidth, height)
            )
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil",
                        color = colors.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    if (modoEdicion) {
                        IconButton(onClick = {
                            modoEdicion = false
                            nuevaPassword = ""
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cancelar",
                                tint = colors.onBackground
                            )
                        }
                    }
                    if (selectedTab == 0 && !cargando) {
                        IconButton(
                            onClick = {
                                if (modoEdicion) showConfirmDialog = true
                                else modoEdicion = true
                            }
                        ) {
                            Icon(
                                imageVector = if (modoEdicion) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = colors.onBackground
                            )
                        }
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->

        Box(modifier = backgroundModifier.padding(padding)) {

            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(24.dp))

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
                                .background(colors.surfaceVariant.copy(alpha = 0.5f)),
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
                            color = colors.onBackground,
                            textAlign = TextAlign.Center
                        )

                        if (modoEdicion) {
                            TextButton(onClick = { launcher.launch("image/*") }) {
                                Text("Cambiar foto", color = colors.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val tabs = listOf("Datos", "Compras", "Ajustes")

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = colors.primary
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title, color = colors.onBackground) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    when (selectedTab) {
                        0 -> {
                            if (!modoEdicion) {
                                PerfilView(nombre, apellidos, telefono, usuario?.email)
                            } else {
                                PerfilEdit(
                                    nombre,
                                    apellidos,
                                    telefono,
                                    nuevaPassword,
                                    { nombre = it },
                                    { apellidos = it },
                                    { telefono = it },
                                    { nuevaPassword = it })
                            }
                        }

                        1 -> TarjetaPlaceholder("Datos de compras.", colors)
                        2 -> AjustesScreenCompact(themeManager, colors)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón para cerrar sesión
                    Button(
                        onClick = {
                            showDialog = true

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 15.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "Cerrar sesión",
                            fontSize = 18.sp,
                            color = Color(0xFF6C3AEC)
                        )
                    }

                    // Diálogo para cerrar sesión
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                // Se ejecuta cuando el usuario toca fuera del diálogo o pulsa atrás
                                showDialog = false
                            },
                            title = {
                                Text(text = "Cerrar sesión")
                            },
                            text = {
                                Text(text = "¿Estás seguro de que quieres cerrar sesión?")
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDialog = false
                                    signOut(context, navController)
                                }) {
                                    Text("Cerrar sesión")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDialog = false }) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }

            if (showConfirmDialog) {
                ConfirmacionDialog(
                    nuevaPassword = nuevaPassword,
                    onDismiss = { showConfirmDialog = false },
                    onConfirm = { passActual, _ ->
                        val credential =
                            EmailAuthProvider.getCredential(usuario?.email!!, passActual)
                        usuario.reauthenticate(credential).addOnSuccessListener {
                            val datos = mutableMapOf<String, Any>(
                                "nombre" to nombre,
                                "apellidos" to apellidos,
                                "telefono" to telefono
                            )

                            val uid = usuario.uid

                            // Si el usuario ha seleccionado una nueva imagen
                            imageUri?.let { uri ->
                                // Subir a Cloudinary
                                kotlinx.coroutines.GlobalScope.launch {
                                    val url = subirImagen(uri, context)
                                    if (url != null) {
                                        datos["foto"] = url
                                        fotoFirestore = url
                                    }

                                    firestore.collection("usuarios")
                                        .document(uid)
                                        .update(datos)
                                }
                            }

                            firestore.collection("usuarios").document(usuario.uid)
                                .update(datos as Map<String, Any>)
                            if (nuevaPassword.isNotEmpty()) usuario.updatePassword(nuevaPassword)
                            nuevaPassword = ""
                            modoEdicion = false
                            showConfirmDialog = false
                            Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Contraseña incorrecta", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                )
            }
        }
    }
}


// VISTA DE PERFIL (MODO VISUALIZACIÓN)
@Composable
fun PerfilView(nombre: String, apellidos: String, telefono: String, email: String?) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {

        Text(
            "Nombre:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground.copy(alpha = 0.7f)
        )
        Text(nombre, fontSize = 18.sp, color = colors.onBackground)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Apellidos:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground.copy(alpha = 0.7f)
        )
        Text(apellidos, fontSize = 18.sp, color = colors.onBackground)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Teléfono:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground.copy(alpha = 0.7f)
        )
        Text(
            telefono.ifBlank { "No especificado" },
            fontSize = 18.sp,
            color = colors.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Correo electrónico:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground.copy(alpha = 0.7f)
        )
        Text(email ?: "", fontSize = 18.sp, color = colors.onBackground)
    }
}


// Función para cerrar sesión
fun signOut(context: Context, navController: NavController) {
    // Sesión de google
    val googleSignInClient = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
    )
    // Cerrar sesión de Google
    googleSignInClient.signOut()

    // Cerrar sesión de Firebase
    Firebase.auth.signOut()

    // Navegar a Inicio limpiando la pila
    navController.navigate(AppScreens.Inicio.route) {
        popUpTo(0) { inclusive = true }
    }
}


// VISTA DE PERFIL (MODO EDICIÓN)
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
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp),
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


// TARJETA PLACEHOLDER PARA TAB "COMPRAS"
@Composable
fun TarjetaPlaceholder(texto: String, colors: androidx.compose.material3.ColorScheme) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Text(
            texto,
            modifier = Modifier.padding(24.dp),
            fontSize = 16.sp,
            color = colors.onSurfaceVariant
        )
    }
}


// SELECTOR DE TEMA
@Composable
fun AjustesScreenCompact(
    themeManager: com.example.persistencia.Herramientas.ThemeManager,
    colors: androidx.compose.material3.ColorScheme
) {
    val themeOptions = listOf(
        ThemePreference.System to "Sistema",
        ThemePreference.Light to "Claro",
        ThemePreference.Dark to "Oscuro"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Tema",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themeOptions.forEach { (pref, label) ->
                    val isSelected = themeManager.themePreference == pref
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = if (isSelected) colors.primary else colors.surface,
                        onClick = { themeManager.themePreference = pref }
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = if (isSelected) Color.White else colors.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}


// DIÁLOGO DE CONFIRMACIÓN
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

suspend fun subirImagen(uri: Uri, context: Context): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bytes = inputStream.readBytes()
        inputStream.close()

        val requestBody = okhttp3.MultipartBody.Builder()
            .setType(okhttp3.MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "perfil.jpg",
                okhttp3.RequestBody.create(
                    "image/*".toMediaTypeOrNull(),
                    bytes
                )
            )
            .addFormDataPart("upload_preset", BuildConfig.CLOUDINARY_UPLOAD_PRESET)
            .build()

        val request = okhttp3.Request.Builder()
            .url("https://api.cloudinary.com/v1_1/${BuildConfig.CLOUDINARY_CLOUD_NAME}/image/upload")
            .post(requestBody)
            .build()

        val client = okhttp3.OkHttpClient()
        val response = client.newCall(request).execute()

        val json = org.json.JSONObject(response.body?.string() ?: return null)
        json.getString("secure_url")

    } catch (e: Exception) {
        null
    }
}
