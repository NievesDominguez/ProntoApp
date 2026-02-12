package com.example.persistencia.Pantallas

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import androidx.room.util.TableInfo
import com.example.persistencia.Navegacion.AppScreens
import com.example.persistencia.R
import com.example.persistencia.localdb.AppDB
import com.example.persistencia.localdb.Estructura
import com.example.persistencia.localdb.SesionData
import com.example.persistencia.localdb.UsuarioData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inicio(navController: NavController) {
    val dbFirebase = Firebase.firestore

    // Variables para los datos de inicio de sesión
    var email by remember { mutableStateOf("") }
    var contrasena = rememberTextFieldState("")
    var passVisible by remember { mutableStateOf(false) } // Para mostrar u ocultar la contraseña

    val context = LocalContext.current // Contexto de la aplicación

    // Instancia de la base de datos Room (no está en uso ahora)
    val dbl = Room.databaseBuilder(context, AppDB::class.java, Estructura.DB.NAME)
        .allowMainThreadQueries().build()

    var usuarioid: UsuarioData?

    // Degradado magenta a morado
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFD13CF2), Color(0xFF6C3AEC))
    )

    // Para obtener el token de Google
    val token = stringResource(R.string.default_web_client_id)
    val googleSignInClient = remember {
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(token)
                .requestEmail()
                .build()
        GoogleSignIn.getClient(context, gso)
    }

    // Launcher que recibe el resultado de la actividad de Google Sign-In
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            // Google devuelve un Intent con la información del login
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Obtiene la cuenta de Google seleccionada por el usuario
                val account = task.getResult(ApiException::class.java)
                // Crea las credenciales para Firebase usando el ID Token de Google
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                // Inicia sesión en Firebase con las credenciales de Google
                Firebase.auth.signInWithCredential(credential)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user // Obtiene el usuario autenticado
                        // Si no es nulo, guarda los datos del usuario en Firestore
                        if (user != null) {
                            val db = FirebaseFirestore.getInstance()
                            val datosUsuario = mapOf(
                                "uid" to user.uid,
                                "nombre" to user.displayName,
                                "email" to user.email,
                                "foto" to user.photoUrl?.toString(),
                                "fechaRegistro" to FieldValue.serverTimestamp()
                            )
                            db.collection("usuarios")
                                .document(user.uid)
                                .set(datosUsuario, SetOptions.merge())
                        }

                        // Lleva a la pantalla principal tras iniciar sesión
                        navController.navigate(AppScreens.PantallaPrincipal.route)

                        // Errores
                    }.addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(context, "Inicio cancelado", Toast.LENGTH_SHORT).show()
            }
        }


    // Da el color de fondo y permite que los elementos de dentro tengan un margen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(Modifier.height(40.dp))

            // Logo del supermercado
            Image(
                painter = painterResource(R.drawable.pronto_blanco),
                contentDescription = "Logo supermercado",
                modifier = Modifier.size(200.dp)
            )

            // Lema
            Text(
                text = "Escanea, paga y listo",
                fontSize = 16.sp,
                color = Color.White
            )

            //Spacer(Modifier.height(10.dp))

            // INICIO DE SESIÓN
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Bienvenid@", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { if (it.length < 30) email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Outlined.Email, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Contraseña
                    OutlinedSecureTextField(
                        state = contrasena,
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { passVisible = !passVisible }) {
                                Icon(
                                    imageVector = if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        textObfuscationMode = if (passVisible) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped
                    )

                    // Botón de inicio de sesión
                    Button(
                        onClick = {

                            // Se obtiene el email sin espacios
                            val correo = email.trim()
                            val pass = contrasena.text

                            // Validación del email
                            if (correo.isEmpty()) {
                                Toast.makeText(context, "Introduce un email", Toast.LENGTH_SHORT)
                                    .show()
                                return@Button
                            }

                            // Validación de formato de email
                            val emailValido =
                                android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()
                            if (!emailValido) {
                                Toast.makeText(
                                    context,
                                    "Introduce un email válido",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            // Validación de contraseña
                            if (pass.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Introduce una contraseña",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }


                            // Inicio de sesión en Firebase Authentication
                            Firebase.auth.signInWithEmailAndPassword(correo, pass as String)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        val user = FirebaseAuth.getInstance().currentUser
                                        // Solo puede iniciar sesión si el correo está verificado
                                        if (user == null || !user.isEmailVerified) {
                                            Toast.makeText(
                                                context,
                                                "Valida tu correo electrónico",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            // Cerrar sesión de Firebase Authentication
                                            Firebase.auth.signOut()
                                        } else {
                                            // Inicio de sesión correcto
                                            Toast.makeText(
                                                context,
                                                "Inicio de sesión correcto",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Navegación a la pantalla principal
                                            navController.navigate(AppScreens.PantallaPrincipal.route) {
                                                popUpTo(AppScreens.Inicio.route) {
                                                    inclusive = true
                                                }
                                            }

                                        }

                                    } else {
                                        // Error al iniciar sesión
                                        val mensaje = task.exception?.localizedMessage
                                            ?: "Error al iniciar sesión"
                                        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                                    }
                                }

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF973BEB))
                    ) {
                        Text("Iniciar sesión", fontSize = 16.sp)
                    }

                    // Lleva a la pantalla de registro para crear una cuenta
                    TextButton(onClick = {
                        navController.navigate(AppScreens.Registro.route)
                    }) {
                        Text("¿No tienes cuenta? Regístrate >", color = Color(0xFFDCD9E0))
                    }


                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Botón para iniciar sesión con Google
                Button(
                    onClick = { launcher.launch(googleSignInClient.signInIntent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.google_logo),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Iniciar sesión con Google",
                        color = Color.Black,
                        fontSize = 15.sp
                    )

                }
            }
        }
    }
}
