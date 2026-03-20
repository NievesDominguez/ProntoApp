package com.example.persistencia.Pantallas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.persistencia.Firestore.CarritoDao
import com.example.persistencia.Firestore.DescuentosDao
import com.example.persistencia.Modelos.Descuento
import com.example.persistencia.Navegacion.AppScreens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cupones(
    navController: NavController
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF6C3AEC), Color(0xFF3A1E6E))
    )

    val db = FirebaseFirestore.getInstance()
    val usuario = FirebaseAuth.getInstance().currentUser
    val uid: String = usuario!!.uid

    val daoOfertas = DescuentosDao()
    val daoCarrito = CarritoDao()

    val scope = rememberCoroutineScope()

    var cuponesUsuario by remember { mutableStateOf<List<String>>(emptyList()) }
    var cupones by remember { mutableStateOf<List<Descuento>>(emptyList()) }
    var cuponesCarrito by remember { mutableStateOf<List<String>>(emptyList()) }

    // Cargar cupones del usuario y cupones disponibles
    LaunchedEffect(uid) {

        // Cargar cupones del usuario
        val doc = db.collection("usuarios").document(uid).get().await()
        cuponesUsuario = doc.get("cupones") as? List<String> ?: emptyList()
        Log.d("CuponesUsuario", cuponesUsuario.toString())

        // Cargar cupones de la base de datos
        cupones = daoOfertas.getCupones()
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cupones disponibles",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            contentDescription = "Volver",
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

            when {
                // Si el usuario no tiene ningún cupón
                cuponesUsuario.isEmpty() && cupones.isNotEmpty()-> {
                    Text(
                        text = "No tienes cupones disponibles",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Se muestra mientras se cargan los cupones
                cupones.isEmpty() -> {
                    Text(
                        text = "Cargando cupones...",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Se muestran todos los cupones disponibles
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        items(cupones) { desc ->

                            if (desc.codigo in cuponesUsuario) {

                                // Estado del switch
                                var cuponActivo by remember { mutableStateOf(false) }

                                // Cargar estado real desde Firestore
                                LaunchedEffect(desc.codigo) {
                                    cuponActivo = daoCarrito.comprobarCupon(desc.codigo)
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color.White.copy(alpha = 0.15f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(16.dp),
                                ) {

                                    Text(
                                        text = desc.nombre ?: "Cupón",
                                        color = Color.White,
                                        fontSize = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Text(
                                            text = desc.descripcion ?: "",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 15.sp,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // SWITCH
                                        Switch(
                                            checked = cuponActivo,
                                            onCheckedChange = { isChecked ->
                                                cuponActivo = isChecked

                                                scope.launch {
                                                    if (isChecked) {
                                                        daoCarrito.activarCupon(desc.codigo!!)
                                                    } else {
                                                        daoCarrito.desactivarCupon(desc.codigo!!)
                                                    }
                                                }
                                            },
                                            thumbContent = if (cuponActivo) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = null,
                                                        tint = Color.White
                                                    )
                                                }
                                            } else null,
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                                uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                                                uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
