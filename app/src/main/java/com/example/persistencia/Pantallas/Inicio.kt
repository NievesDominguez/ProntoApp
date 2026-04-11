package com.example.persistencia.Pantallas

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.persistencia.BuildConfig
import com.example.persistencia.Herramientas.LocalThemeManager
import com.example.persistencia.Herramientas.ThemePreference
import com.example.persistencia.Navegacion.AppScreens
import com.example.persistencia.R
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inicio(navController: NavController) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current

    var email by remember { mutableStateOf("") }
    var contrasena = rememberTextFieldState("")
    var passVisible by remember { mutableStateOf(false) }

    // Google Sign-In
    val token = stringResource(R.string.default_web_client_id)
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(token)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            Firebase.auth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        val db = FirebaseFirestore.getInstance()
                        val docRef = db.collection("usuarios").document(user.uid)

                        docRef.get().addOnSuccessListener { snapshot ->
                            if (!snapshot.exists()) {
                                // Usuario nuevo: guardamos los datos básicos de Google
                                val datosUsuario = hashMapOf<String, Any>(
                                    "uid" to user.uid,
                                    "nombre" to (user.displayName ?: ""),
                                    "email" to (user.email ?: ""),
                                    "foto" to (user.photoUrl?.toString() ?: ""),
                                    "fechaRegistro" to FieldValue.serverTimestamp()
                                )
                                docRef.set(datosUsuario)
                                    .addOnSuccessListener {
                                        navController.navigate(AppScreens.PantallaPrincipal.route)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Error al guardar datos: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            } else {
                                // Usuario ya existente: NO hacemos nada, solo navegamos
                                navController.navigate(AppScreens.PantallaPrincipal.route)
                            }
                        }.addOnFailureListener { e ->
                            Toast.makeText(context, "Error al verificar usuario: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Error: usuario nulo", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            Toast.makeText(context, "Inicio cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .drawBehind {
                // Ancho del degradado más pequeño y sutil
                val edgeWidth = with(density) { 25.dp.toPx() }
                // Opacidad más baja para mayor sutileza
                val primaryColor = colors.primary.copy(alpha = 0.1f)
                val secondaryColor = colors.secondary.copy(alpha = 0.05f)
                val width = size.width
                val height = size.height

                // Borde superior
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor, Color.Transparent),
                        startY = 0f,
                        endY = edgeWidth
                    ),
                    topLeft = Offset(0f, 0f),
                    size = Size(width, edgeWidth)
                )
                // Borde inferior
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, primaryColor),
                        startY = height - edgeWidth,
                        endY = height
                    ),
                    topLeft = Offset(0f, height - edgeWidth),
                    size = Size(width, edgeWidth)
                )
                // Borde izquierdo
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(secondaryColor, Color.Transparent),
                        startX = 0f,
                        endX = edgeWidth
                    ),
                    topLeft = Offset(0f, 0f),
                    size = Size(edgeWidth, height)
                )
                // Borde derecho
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, secondaryColor),
                        startX = width - edgeWidth,
                        endX = width
                    ),
                    topLeft = Offset(width - edgeWidth, 0f),
                    size = Size(edgeWidth, height)
                )
            }
    ) {
        val themeManager = LocalThemeManager.current

        // Botón debug (solo en debug)
        if (BuildConfig.DEBUG) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp),
                shape = CircleShape,
                color = colors.surface.copy(alpha = 0.8f),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = {
                        val newPreference = when (themeManager.themePreference) {
                            ThemePreference.System -> ThemePreference.Light
                            ThemePreference.Light -> ThemePreference.Dark
                            ThemePreference.Dark -> ThemePreference.System
                            else -> ThemePreference.System
                        }
                        themeManager.themePreference = newPreference
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Brightness4,
                        contentDescription = "Cambiar tema",
                        tint = colors.onBackground
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Logo
            Image(
                painter = painterResource(R.drawable.pronto_logo),
                contentDescription = "Logo supermercado",
                modifier = Modifier.size(160.dp),
                contentScale = ContentScale.Fit
            )

            // Lema
            Text(
                text = "Escanea, paga y listo",
                fontSize = 16.sp,
                color = colors.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tarjeta de inicio de sesión
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        "Bienvenid@",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { if (it.length < 30) email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Outlined.Email, null, tint = colors.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedSecureTextField(
                        state = contrasena,
                        label = { Text("Contraseña") },
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

                    Button(
                        onClick = {
                            val correo = email.trim()
                            val pass = contrasena.text

                            if (correo.isEmpty()) {
                                Toast.makeText(context, "Introduce un email", Toast.LENGTH_SHORT)
                                    .show()
                                return@Button
                            }
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                                Toast.makeText(
                                    context,
                                    "Introduce un email válido",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            if (pass.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Introduce una contraseña",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            Firebase.auth.signInWithEmailAndPassword(correo, pass as String)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = FirebaseAuth.getInstance().currentUser
                                        if (user == null || !user.isEmailVerified) {
                                            Toast.makeText(
                                                context,
                                                "Valida tu correo electrónico",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Firebase.auth.signOut()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Inicio de sesión correcto",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.navigate(AppScreens.PantallaPrincipal.route) {
                                                popUpTo(AppScreens.Inicio.route) {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                    } else {
                                        val mensaje = task.exception?.localizedMessage
                                            ?: "Error al iniciar sesión"
                                        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Iniciar sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    TextButton(
                        onClick = { navController.navigate(AppScreens.Registro.route) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("¿No tienes cuenta? Regístrate", color = colors.secondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Google
            OutlinedButton(
                onClick = { launcher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.onBackground
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(colors.primary, colors.secondary)),
                    width = 1.5.dp
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.google_logo),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Continuar con Google", fontSize = 14.sp)
            }
        }
    }
}