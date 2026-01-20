package com.example.persistencia

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.KebabDining
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.room.Room
import com.example.persistencia.Navegacion.AppScreens
import com.example.persistencia.localdb.AppDB
import com.example.persistencia.localdb.Estructura
import com.example.persistencia.localdb.SesionData
import com.example.persistencia.localdb.UsuarioDao
import com.example.persistencia.localdb.UsuarioData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inicio(navController: NavController) {
    val dbFirebase = Firebase.firestore
    var email by remember { mutableStateOf("") }
    var contrasena = rememberTextFieldState("")
    var passVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val dbl = Room.databaseBuilder(context, AppDB::class.java, Estructura.DB.NAME)
        .allowMainThreadQueries().build()

    var usuarioid: UsuarioData?

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFD13CF2), Color(0xFF6C3AEC)) // naranja → rosa
    )

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

            Image(
                painter = painterResource(R.drawable.pronto_blanco),
                contentDescription = "Logo supermercado",
                modifier = Modifier.size(200.dp)
            )

            Text(
                text = "Escanea, paga y listo",
                fontSize = 16.sp,
                color = Color.White
            )

            Spacer(Modifier.height(10.dp))

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

                    OutlinedTextField(
                        value = email,
                        onValueChange = { if (it.length < 30) email = it },
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
                            IconButton(onClick = { passVisible = !passVisible }) {
                                Icon(
                                    imageVector = if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        textObfuscationMode = if (passVisible) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped
                    )

                    Button(
                        onClick = {
                            if (email.isBlank() || contrasena.text.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "No puede haber campos en blanco",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                dbFirebase.collection("usuarios").document(email).get()
                                    .addOnSuccessListener { usuario ->
                                        val usr = usuario.data
                                        val emailUsr = usr?.get("Email") as? String
                                        val pwd = usr?.get("Password") as? String
                                        if (emailUsr != null && pwd != null) {
                                            usuarioid = dbl.usuarioDao().getUnUser(email)
                                            if (pwd == contrasena.text && usuarioid != null) {
                                                val fecha = SimpleDateFormat(
                                                    "dd-MM-yyyy",
                                                    Locale.getDefault()
                                                ).format(Date())
                                                val sesionData = SesionData(
                                                    idUsuario = usuarioid!!.idUsuario,
                                                    fechaInicio = fecha
                                                )

                                                dbl.sesionDao().nuevaSesion(sesionData)
                                                navController.navigate(AppScreens.Resultados.route)
                                                Toast.makeText(
                                                    context,
                                                    "Usuario inició sesión",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Credenciales inválidas",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Datos del usuario incompletos",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Error en la consulta",
                                            Toast.LENGTH_SHORT
                                        ).show()
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

                    TextButton(onClick = {
                        navController.navigate(AppScreens.Formulario.route)
                    }) {
                        Text("¿No tienes cuenta? Regístrate >", color = Color(0xFF973BEB))
                    }
                }
            }
        }
    }
}
