package com.example.persistencia.Pantallas

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.example.persistencia.Modelos.Producto
import com.example.persistencia.Navegacion.AppScreens
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Perfil(navController: NavController) {
    val context = LocalContext.current

    // Degradado magenta a morado
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFD13CF2), Color(0xFF6C3AEC))
    )

    val authUser = Firebase.auth.currentUser// Usuario autenticado
    val firestore =
        FirebaseFirestore.getInstance() // Estado donde guardaremos los datos del usuario desde Firestore

    var datosUsuario by remember {
        mutableStateOf<Map<String, Any>?>(null)
    }
    // Cargar datos del usuario desde Firestore al cargar la pantalla
    LaunchedEffect(authUser?.uid) {
        val uid = authUser?.uid // ID del usuario
        // Si el ID no es nulo, se cargan los datos del usuario
        if (uid != null) {
            firestore.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        datosUsuario = doc.data
                    }
                }.addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Error al cargar datos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
    var showDialog by remember { mutableStateOf(false) } // Variable que determina si se muestra el dialogo para cerrar sesión o no

    var mostrarDialogo by remember { mutableStateOf(false) } // Variable que determina si se muestra el dialogo para editar perfil o no
    val scope = rememberCoroutineScope() // Para ejecutar corrutinas

    // TOPBAR
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                colors = topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                ),

                actions = {
                    // Botón de editar
                    IconButton(onClick = { mostrarDialogo = true }) {
                        Icon(
                            imageVector = Lucide.Pencil,
                            contentDescription = "Editar producto",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->

        // Da el color de fondo y permite que los elementos de dentro tengan un margen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues), // Esto evita que el contenido tape la TopBar
        ) {

            // Permite editar el perfil
            if (mostrarDialogo) {
                EditarPerfil(
                    onDismiss = { mostrarDialogo = false },
                    onSave = {

                    }
                )

            }

            Column() {

                // Card donde va el perfil del usuario
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        // Imagen del usuario
                        AsyncImage(
                            modifier = Modifier
                                .size(85.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            // Si no hubiera imagen, se usa una por defecto
                            model = authUser?.photoUrl
                                ?: datosUsuario?.get("foto")
                                ?: "https://www.shutterstock.com/image-vector/default-avatar-profile-icon-social-600nw-1906669723.jpg",

                            contentScale = ContentScale.Crop,
                            contentDescription = "Imagen del usuario"
                        )

                        // Nombre del usuario
                        val nombre = datosUsuario?.get("nombre") as? String
                        val apellidos =
                            datosUsuario?.get("apellidos") as? String
                        if (!nombre.isNullOrBlank()) {
                            Text(
                                text = if (!apellidos.isNullOrBlank()) "$nombre $apellidos" else nombre,
                                fontSize = 25.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }


                    }

                }

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
            }

        }
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


// Diálogo de edición de perfil
@Composable
fun EditarPerfil(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val usuario = Firebase.auth.currentUser!!
    val context = LocalContext.current

    // Datos del usuario
    var email by remember { mutableStateOf(usuario.email ?: "") }
    var contrasenaNueva by remember { mutableStateOf("") }
    var contrasenaActual by remember { mutableStateOf("") }

    // Abre el diálogo para editar el perfil
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Email (no editable)
                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    label = { Text("Email") },
                    enabled = false
                )

                // Nueva contraseña
                OutlinedTextField(
                    value = contrasenaNueva,
                    onValueChange = { contrasenaNueva = it },
                    label = { Text("Nueva contraseña") },
                    visualTransformation = PasswordVisualTransformation()
                )

                // Contraseña actual (para reautenticar)
                OutlinedTextField(
                    value = contrasenaActual,
                    onValueChange = { contrasenaActual = it },
                    label = { Text("Contraseña actual") },
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },

        confirmButton = {
            TextButton(onClick = {

                // Validaciones
                if (contrasenaNueva.isNotEmpty() && contrasenaNueva.length < 6) {
                    Toast.makeText(
                        context,
                        "La contraseña debe tener al menos 6 caracteres",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@TextButton
                }

                if (contrasenaNueva.isNotEmpty() && contrasenaActual.isEmpty()) {
                    Toast.makeText(context, "Introduce tu contraseña actual", Toast.LENGTH_SHORT)
                        .show()
                    return@TextButton
                }

                // Actualizar contraseña
                if (contrasenaNueva.isNotEmpty()) {

                    // Reautenticación para comprobar si la contraseña actual coincide
                    val credential = EmailAuthProvider.getCredential(email, contrasenaActual)

                    usuario.reauthenticate(credential)
                        .addOnSuccessListener {
                            usuario.updatePassword(contrasenaNueva) // Cambiar contraseña
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Perfil actualizado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onSave()
                                    onDismiss()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Error al cambiar contraseña: ${it.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Contraseña actual incorrecta",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                }
            }) {
                Text("Guardar")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
