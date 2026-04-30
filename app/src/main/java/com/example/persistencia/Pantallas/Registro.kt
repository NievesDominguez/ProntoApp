package com.example.persistencia.Pantallas

import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Registro(navController: NavController) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contrasena = rememberTextFieldState("")
    var passVisible by remember { mutableStateOf(false) }
    var telefono by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .drawBehind {
                val edgeWidth = with(density) { 25.dp.toPx() }
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp) // Reducido el padding vertical
                .imePadding(), // Evita que el teclado tape los campos inferiores
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Logo eliminado para ganar espacio vertical

//            // Lema (opcional, se mantiene)
//            Text(
//                text = "Escanea, paga y listo",
//                fontSize = 16.sp,
//                color = colors.onBackground.copy(alpha = 0.7f)
//            )

            Spacer(modifier = Modifier.height(30.dp))

            // Tarjeta de registro (sin borde, igual que Inicio)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        "Crear cuenta",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
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
                        onValueChange = { apellidos = it },
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
                        value = email,
                        onValueChange = { email = it },
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

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = colors.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
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

                            if (nombre.isBlank()) {
                                Toast.makeText(context, "Introduce tu nombre", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (correo.isEmpty()) {
                                Toast.makeText(context, "Introduce un correo electrónico", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                                Toast.makeText(context, "Introduce un correo electrónico válido", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (pass.isEmpty()) {
                                Toast.makeText(context, "Introduce una contraseña", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (pass.length < 8) {
                                Toast.makeText(context, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!pass.any { it.isUpperCase() } || !pass.any { it.isDigit() }) {
                                Toast.makeText(context, "La contraseña debe contener al menos una mayúscula y un número", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (telefono.isNotEmpty() && (telefono.length < 9 || !telefono.all { it.isDigit() })) {
                                Toast.makeText(context, "Introduce un teléfono válido", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            auth.createUserWithEmailAndPassword(correo, pass as String)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val uid = auth.currentUser?.uid
                                        FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                                        if (uid != null) {
                                            val datosUsuario = mapOf(
                                                "nombre" to nombre,
                                                "apellidos" to apellidos,
                                                "email" to correo,
                                                "telefono" to telefono
                                            )
                                            firestore.collection("usuarios")
                                                .document(uid)
                                                .set(datosUsuario)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Usuario registrado correctamente. Comprueba tu correo electrónico.", Toast.LENGTH_SHORT).show()
                                                    Firebase.auth.signOut()
                                                    navController.navigate("Inicio")
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Error al guardar datos", Toast.LENGTH_SHORT).show()
                                                }
                                        } else {
                                            Toast.makeText(context, "Error al obtener usuario", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        val mensaje = task.exception?.localizedMessage ?: "Error al registrar"
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
                        Text("Registrarse", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    TextButton(
                        onClick = { navController.navigate(AppScreens.Inicio.route) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("¿Ya tienes cuenta? Inicia sesión", color = colors.secondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}