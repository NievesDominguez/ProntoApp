package com.example.persistencia.Pantallas

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.persistencia.Navegacion.AppScreens
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Registro(navController: NavController) {

    val context = LocalContext.current // Contexto de la aplicación

    // Variables para los datos a rellenar
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contrasena = rememberTextFieldState("")
    var passVisible by remember { mutableStateOf(false) }
    var telefono by remember { mutableStateOf("") }

    // Degradado magenta a morado
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFD13CF2), Color(0xFF6C3AEC))
    )

    // Inicializa las variables para la autenticación
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

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
                painter = painterResource(com.example.persistencia.R.drawable.pronto_blanco),
                contentDescription = "Logo supermercado",
                modifier = Modifier.size(200.dp)
            )

            // Lema
            Text(
                text = "Escanea, paga y listo",
                fontSize = 16.sp,
                color = Color.White
            )

            // FORMULARIO DE REGISTRO
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 500.dp), // Límite visual
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()), // Para poder hacer scroll
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text("Crear cuenta", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Outlined.Person, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = apellidos,
                        onValueChange = { apellidos = it },
                        label = { Text("Apellidos") },
                        leadingIcon = { Icon(Icons.Outlined.Badge, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Outlined.Email, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedSecureTextField(
                        state = contrasena,
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                        trailingIcon = {
                            // Botón para mostrar u ocultar la contraseña
                            IconButton(onClick = { passVisible = !passVisible }) {
                                Icon(
                                    imageVector = if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        // Muestra u oculta la contraseña
                        textObfuscationMode = if (passVisible) TextObfuscationMode.Visible else
                            TextObfuscationMode.RevealLastTyped
                    )

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        leadingIcon = { Icon(Icons.Outlined.Phone, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), // Permite solo números
                        modifier = Modifier.fillMaxWidth()
                    )

                    // BOTÓN DE REGISTRO
                    Button(
                        onClick = {
                            // Se obtiene el email sin espacios
                            val correo = email.trim()
                            val pass = contrasena.text

                            // Validación del nombre
                            if (nombre.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Introduce tu nombre",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button // Vuelve arriba
                            }

                            // Validación del correo
                            if (correo.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Introduce un correo electrónico",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button // Vuelve arriba
                            }

                            // Validación del formato del correo
                            val emailValido = android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()
                            if (!emailValido) {
                                Toast.makeText(
                                    context,
                                    "Introduce un correo electrónico válido",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button // Vuelve arriba
                            }

                            // Validación de contraseña
                            if (pass.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Introduce una contraseña",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button // Vuelve arriba
                            }

                            // Validación del formato de la contraseña
//                            if (pass.length<6) {
//                                Toast.makeText(
//                                    context,
//                                    "La contraseña debe tener al menos 6 caracteres",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                                return@Button // Vuelve arriba
//                            }

                            // Validación del teléfono solo si el usuario ha escrito algo
                            if (telefono.isNotEmpty()) {
                                val telefonoValido = telefono.length >= 9 && telefono.all {
                                    it.isDigit()
                                }
                                if (!telefonoValido) {
                                    Toast.makeText(
                                        context,
                                        "Introduce un teléfono válido",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                            }

                            // Se crea el usuario en Firebase Authentication con email y contraseña
                            auth.createUserWithEmailAndPassword(correo, pass as String)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        // Si el registro es correcto, se obtiene el UID del usuario autenticado
                                        val uid = auth.currentUser?.uid

                                        // Se envía un correo de verificación
                                        FirebaseAuth.getInstance().currentUser?.sendEmailVerification()

                                        // Si el registro es correcto, se guardan los datos del usuario en firestore
                                        if (uid != null) {
                                            val datosUsuario = mapOf(
                                                "nombre" to nombre,
                                                "apellidos" to apellidos,
                                                "email" to correo,
                                                "telefono" to telefono
                                            )

                                            // Se crea el documento del usuario en la colección "usuarios"
                                            firestore.collection("usuarios")
                                                .document(uid)
                                                .set(datosUsuario)
                                                .addOnSuccessListener {
                                                    // Se confirma que se ha registrado el usuario correctamente con un toast
                                                    Toast.makeText(
                                                        context,
                                                        "Usuario registrado correctamente. Comprueba tu correo electrónico.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                    // Cerrar sesión de Firebase Authentication
                                                    Firebase.auth.signOut()

                                                    // Navegación a la pantalla de inicio de sesión
                                                    navController.navigate("Inicio")
                                                }
                                                .addOnFailureListener {
                                                    // Error al guardar los datos en Firestore
                                                    Toast.makeText(
                                                        context,
                                                        "Error al guardar datos",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                        } else {
                                            // Error inesperado al obtener el UID del usuario
                                            Toast.makeText(
                                                context,
                                                "Error al obtener usuario",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    } else {
                                        // Error al registrar en Firebase Authentication
                                        val mensaje = task.exception?.localizedMessage
                                            ?: "Error al registrar"
                                        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C3AEC))
                    ) {
                        Text("Registrarse", fontSize = 16.sp)
                    }


                    // Botón para volver a la pantalla de inicio de sesión
                    TextButton(onClick = {
                        navController.navigate(AppScreens.Inicio.route)
                    }) {
                        Text("¿Ya tienes cuenta? Inicia sesión", color = Color(0xFF6C3AEC))
                    }
                }
            }
        }
    }
}
