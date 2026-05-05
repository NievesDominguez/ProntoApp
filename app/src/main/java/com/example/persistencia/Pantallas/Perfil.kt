package com.example.persistencia.Pantallas

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.persistencia.Navegacion.AppScreens
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.example.persistencia.BuildConfig
import com.example.persistencia.Herramientas.CarritoRepository
import com.example.persistencia.Herramientas.DescuentosRepository
import com.example.persistencia.Herramientas.ListasRepository
import com.example.persistencia.Herramientas.ProductosRepository
import com.example.persistencia.Herramientas.ThemeManager
import com.example.persistencia.Herramientas.TicketsRepository
import com.example.persistencia.Herramientas.UsuariosRepository
import com.example.persistencia.Herramientas.fondoDegradado
import com.example.persistencia.Herramientas.toFormattedString
import com.example.persistencia.Modelos.Ticket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Perfil(navController: NavController) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val usuario = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    val themeManager = LocalThemeManager.current
    val scope = rememberCoroutineScope()

    // Estados
    var modoEdicion by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    val nuevaPasswordState = rememberTextFieldState()
    var fotoFirestore by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    // Cargar datos
    LaunchedEffect(usuario?.uid) {
        cargando = true
        usuario?.uid?.let { uid ->
            firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener { doc ->
                    nombre = doc.getString("nombre") ?: ""
                    apellidos = doc.getString("apellidos") ?: ""
                    telefono = doc.getString("telefono") ?: ""
                    fotoFirestore = doc.getString("foto")
                }
        }
        delay(300L)
        cargando = false
    }

    // Selector de imagenes
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            scope.launch {
                try {
                    val url = subirImagen(it, context)
                    withContext(Dispatchers.Main) {
                        if (url != null) {
                            fotoFirestore = url
                            usuario?.uid?.let { uid ->
                                firestore.collection("usuarios").document(uid)
                                    .update("foto", url)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Error al guardar foto en Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        } else {
                            Toast.makeText(context, "Error: No se pudo subir la imagen a Cloudinary", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Perfil", "Excepcion al subir imagen", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } finally {
                    imageUri = null
                }
            }
        }
    }

    // Guardar cambios de texto (sin contrasena)
    fun guardarCambiosTexto() {
        val datos = mapOf(
            "nombre" to nombre,
            "apellidos" to apellidos,
            "telefono" to telefono
        )
        usuario?.uid?.let { uid ->
            firestore.collection("usuarios").document(uid)
                .update(datos)
                .addOnSuccessListener {
                    Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show()
                    modoEdicion = false
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
        }
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
                            nuevaPasswordState.edit { replace(0, length, "") }
                        }) {
                            Icon(Icons.Default.Close, "Cancelar", tint = colors.onBackground)
                        }
                    }
                    if (selectedTab == 0 && !cargando) {
                        IconButton(
                            onClick = {
                                if (modoEdicion) {
                                    if (nuevaPasswordState.text.isBlank()) guardarCambiosTexto()
                                    else showConfirmDialog = true
                                } else {
                                    modoEdicion = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (modoEdicion) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = colors.onBackground
                            )
                        }
                    }
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = colors.onBackground
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier
            .fondoDegradado()
            .padding(padding)) {
            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Foto con boton de camara flotante
                    Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = when {
                                imageUri != null -> imageUri
                                !fotoFirestore.isNullOrBlank() -> fotoFirestore
                                usuario?.photoUrl != null -> usuario.photoUrl
                                else -> "https://www.shutterstock.com/image-vector/default-avatar-profile-icon-social-600nw-1906669723.jpg"
                            },
                            contentDescription = "Foto perfil",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .background(colors.primary, CircleShape)
                                .clip(CircleShape),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = colors.primary)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$nombre $apellidos",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onBackground
                    )
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
                                text = { Text(title, color = colors.onBackground) })
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    when (selectedTab) {
                        0 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                if (!modoEdicion) {
                                    PerfilView(nombre, apellidos, telefono, usuario?.email)
                                } else {
                                    PerfilEdit(
                                        nombre = nombre,
                                        apellidos = apellidos,
                                        telefono = telefono,
                                        nuevaPasswordState = nuevaPasswordState,
                                        onNombre = { nombre = it },
                                        onApellidos = { apellidos = it },
                                        onTelefono = { telefono = it }
                                    )
                                }
                            }
                        }

                        1 -> {
                            var tickets by remember { mutableStateOf<List<Ticket>?>(null) }
                            var errorCarga by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                try {
                                    tickets = TicketsRepository.getTickets()
                                } catch (e: Exception) {
                                    errorCarga = true
                                    tickets = emptyList()
                                }
                            }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                when {
                                    tickets == null -> Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        Alignment.Center
                                    ) { CircularProgressIndicator(color = colors.primary) }

                                    errorCarga -> Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = colors.errorContainer)
                                    ) {
                                        Column(
                                            Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.Error,
                                                null,
                                                tint = colors.error,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                "Error al cargar las compras",
                                                color = colors.error,
                                                textAlign = TextAlign.Center
                                            )
                                            TextButton(onClick = {
                                                errorCarga = false; tickets = null
                                            }) { Text("Reintentar") }
                                        }
                                    }

                                    tickets!!.isEmpty() -> Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Box(Modifier.padding(32.dp), Alignment.Center) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    Icons.Default.ShoppingCart,
                                                    null,
                                                    modifier = Modifier.size(48.dp),
                                                    tint = colors.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                                Spacer(Modifier.height(16.dp))
                                                Text(
                                                    "Aun no has realizado ninguna compra",
                                                    fontSize = 16.sp,
                                                    color = colors.onSurfaceVariant,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }

                                    else -> LazyColumn(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(tickets!!, key = { it.id }) { ticket ->
                                            TicketResumenCard(
                                                ticket = ticket,
                                                onClick = { navController.navigate("${AppScreens.TicketDetalle.route}/${ticket.id}") })
                                        }
                                        item { Spacer(Modifier.height(16.dp)) }
                                    }
                                }
                            }
                        }

                        2 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                AjustesScreenCompact(themeManager, colors)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
//
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text("Cerrar sesion") },
                            text = { Text("Estas seguro de que quieres cerrar sesion?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDialog = false; signOut(context, navController)
                                }) { Text("Cerrar sesion") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showDialog = false
                                }) { Text("Cancelar") }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }

            // Dialogo de confirmacion para cambio de contrasena
            if (showConfirmDialog) {
                ConfirmacionDialog(
                    nuevaPassword = nuevaPasswordState.text.toString(),
                    onDismiss = {
                        showConfirmDialog = false
                        nuevaPasswordState.edit { replace(0, length, "") }
                    },
                    onConfirm = { passActual, _ ->
                        val credential =
                            EmailAuthProvider.getCredential(usuario?.email!!, passActual)
                        usuario.reauthenticate(credential).addOnSuccessListener {
                            val passText = nuevaPasswordState.text.toString()
                            if (passText.isNotEmpty()) usuario.updatePassword(passText)
                            val datos = mapOf(
                                "nombre" to nombre,
                                "apellidos" to apellidos,
                                "telefono" to telefono
                            )
                            usuario.uid?.let { uid ->
                                firestore.collection("usuarios").document(uid).update(datos)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Datos y contraseña actualizados",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Error al actualizar datos",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            nuevaPasswordState.edit { replace(0, length, "") }
                            modoEdicion = false
                            showConfirmDialog = false
                        }.addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Contraseña actual incorrecta",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
    }
}


// VISTA DE PERFIL (MODO VISUALIZACION)
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
            "Telefono:",
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
            "Correo electronico:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground.copy(alpha = 0.7f)
        )
        Text(email ?: "", fontSize = 18.sp, color = colors.onBackground)
    }
}


// Funcion para cerrar sesion
// Funcion para cerrar sesion
fun signOut(context: Context, navController: NavController) {
    // Sesion de google
    val googleSignInClient = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
    )
    // Cerrar sesion de Google
    googleSignInClient.signOut()

    // Cerrar sesion de Firebase
    Firebase.auth.signOut()

    // Limpiar todos los caches en memoria
    CoroutineScope(Dispatchers.Main).launch {
        CarritoRepository.invalidar()
        ListasRepository.invalidar()
        TicketsRepository.invalidar()
        UsuariosRepository.invalidar()
        ProductosRepository.invalidar()
        DescuentosRepository.invalidar()
    }

    // Navegar a Inicio limpiando la pila
    navController.navigate(AppScreens.Inicio.route) {
        popUpTo(0) { inclusive = true }
    }
}


// Modo edicion con campos estilizados
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilEdit(
    nombre: String,
    apellidos: String,
    telefono: String,
    nuevaPasswordState: TextFieldState,
    onNombre: (String) -> Unit,
    onApellidos: (String) -> Unit,
    onTelefono: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var passVisible by remember { mutableStateOf(false) }

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
                leadingIcon = { Icon(Icons.Outlined.Person, null, tint = colors.primary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline
                ),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = apellidos,
                onValueChange = onApellidos,
                label = { Text("Apellidos") },
                leadingIcon = { Icon(Icons.Outlined.Badge, null, tint = colors.primary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline
                ),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = onTelefono,
                label = { Text("Telefono") },
                leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = colors.primary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline
                ),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedSecureTextField(
                state = nuevaPasswordState,
                label = { Text("Nueva contraseña") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = colors.primary) },
                trailingIcon = {
                    IconButton(onClick = { passVisible = !passVisible }) {
                        Icon(
                            imageVector = if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = colors.primary
                        )
                    }
                },
                textObfuscationMode = if (passVisible) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}


// Ajustes
@Composable
fun AjustesScreenCompact(
    themeManager: ThemeManager,
    colors: ColorScheme
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
                "Tema",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themeOptions.forEach { (pref, label) ->
                    val isSelected = themeManager.themePreference.value == pref
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = if (isSelected) colors.primary else colors.surface,
                        onClick = { themeManager.setThemePreference(pref) }
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


// DIALOGO DE CONFIRMACION
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmacionDialog(
    nuevaPassword: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    val passActualState = rememberTextFieldState()
    val repetirNuevaState = rememberTextFieldState()

    var passActualVisible by remember { mutableStateOf(false) }
    var repetirVisible by remember { mutableStateOf(false) }

    val errorPassActual = passActualState.text.isBlank()
    val errorRepetir = nuevaPassword.isNotEmpty() &&
            repetirNuevaState.text.isNotEmpty() &&
            repetirNuevaState.text.toString() != nuevaPassword

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
                OutlinedSecureTextField(
                    state = passActualState,
                    label = { Text("Contraseña actual") },
                    isError = errorPassActual,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = colors.primary) },
                    trailingIcon = {
                        IconButton(onClick = { passActualVisible = !passActualVisible }) {
                            Icon(
                                imageVector = if (passActualVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = colors.primary
                            )
                        }
                    },
                    textObfuscationMode = if (passActualVisible) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                if (nuevaPassword.isNotEmpty()) {
                    OutlinedSecureTextField(
                        state = repetirNuevaState,
                        label = { Text("Repetir nueva contraseña") },
                        isError = errorRepetir,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = colors.primary) },
                        trailingIcon = {
                            IconButton(onClick = { repetirVisible = !repetirVisible }) {
                                Icon(
                                    imageVector = if (repetirVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = colors.primary
                                )
                            }
                        },
                        textObfuscationMode = if (repetirVisible) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline
                        ),
                        shape = RoundedCornerShape(16.dp)
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
                    onConfirm(
                        passActualState.text.toString(),
                        repetirNuevaState.text.toString()
                    )
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
    return withContext(Dispatchers.IO) {
        try {
            Log.d("SubirImagen", "Iniciando subida. URI: $uri")

            val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
            val uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET
            if (cloudName.isBlank() || uploadPreset.isBlank()) {
                Log.e("SubirImagen", "Cloudinary config vacia: cloudName='$cloudName', uploadPreset='$uploadPreset'")
                return@withContext null
            }

            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("SubirImagen", "No se pudo abrir InputStream para URI: $uri")
                return@withContext null
            }
            val bytes = inputStream.readBytes()
            inputStream.close()
            Log.d("SubirImagen", "Imagen leida, tamano: ${bytes.size} bytes")

            val requestBody = okhttp3.MultipartBody.Builder()
                .setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "perfil.jpg",
                    okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), bytes)
                )
                .addFormDataPart("upload_preset", uploadPreset)
                .build()

            val request = okhttp3.Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                .post(requestBody)
                .build()

            Log.d("SubirImagen", "Enviando peticion a Cloudinary...")
            val client = okhttp3.OkHttpClient()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            Log.d("SubirImagen", "Respuesta codigo: ${response.code}, body: $responseBody")

            if (!response.isSuccessful) {
                Log.e("SubirImagen", "Error en Cloudinary: ${response.code} - $responseBody")
                return@withContext null
            }

            val json = org.json.JSONObject(responseBody ?: return@withContext null)
            val url = json.getString("secure_url")
            Log.d("SubirImagen", "Imagen subida correctamente: $url")
            url
        } catch (e: Exception) {
            Log.e("SubirImagen", "Excepcion en subida", e)
            null
        }
    }
}


@Composable
fun TicketResumenCard(
    ticket: Ticket,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = ticket.fecha.toFormattedString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurfaceVariant
            )

            Text(
                text = "%.2f €".format(ticket.total),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
        }
    }
}