package com.example.persistencia.Pantallas

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.persistencia.Firestore.CarritoDao
import com.example.persistencia.Firestore.DescuentosDao
import com.example.persistencia.Herramientas.CarritoRepository
import com.example.persistencia.Herramientas.DescuentosRepository
import com.example.persistencia.Herramientas.fondoDegradado
import com.example.persistencia.Modelos.Descuento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cupones(navController: NavController) {
    val colors = MaterialTheme.colorScheme

    // Modificador con fondo degradado
    val backgroundModifier = Modifier.fondoDegradado()

    // Instancias de Firebase
    val db = FirebaseFirestore.getInstance()
    val usuario = FirebaseAuth.getInstance().currentUser
    val uid: String = usuario?.uid ?: return  // Si no hay usuario, sale de la función

    val scope = rememberCoroutineScope()

    // Estados para almacenar los cupones del usuario y todos los cupones disponibles
    var cuponesUsuario by remember { mutableStateOf<List<String>>(emptyList()) }
    var cupones by remember { mutableStateOf<List<Descuento>>(emptyList()) }

    // Carga los cupones disponibles y los del usuario desde Firestore
    LaunchedEffect(uid) {
        val doc = db.collection("usuarios").document(uid).get().await()
        cuponesUsuario = doc.get("cupones") as? List<String> ?: emptyList()
        Log.d("CuponesUsuario", cuponesUsuario.toString())

        cupones = DescuentosRepository.getCupones()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cupones disponibles",
                        color = colors.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colors.onBackground
                ),
                navigationIcon = {
                    // Botón para volver
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            contentDescription = "Volver",
                            tint = colors.onBackground
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = backgroundModifier
                .padding(padding)
        ) {
            // Tres estados posibles: sin cupones, cargando, o lista de cupones
            when {
                // Usuario no tiene cupones asignados
                cuponesUsuario.isEmpty() && cupones.isNotEmpty() -> {
                    Text(
                        text = "No tienes cupones disponibles",
                        color = colors.onBackground,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Aún cargando los cupones
                cupones.isEmpty() -> {
                    Text(
                        text = "Cargando cupones...",
                        color = colors.onBackground,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Muestra la lista de cupones
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(cupones) { desc ->
                            // Solo muestra cupones que el usuario tiene asignados
                            if (desc.codigo in cuponesUsuario) {
                                var cuponActivo by remember { mutableStateOf(false) }

                                // Comprueba si el cupón está activo en el carrito
                                LaunchedEffect(desc.codigo) {
                                    cuponActivo = CarritoRepository.comprobarCupon(desc.codigo)
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            colors.surfaceVariant.copy(alpha = 0.8f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(16.dp),
                                ) {
                                    Text(
                                        text = desc.nombre ?: "Cupón",
                                        color = colors.onBackground,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = desc.descripcion ?: "",
                                            color = colors.onBackground.copy(alpha = 0.8f),
                                            fontSize = 15.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        // Switch para activar/desactivar el cupón
                                        Switch(
                                            checked = cuponActivo,
                                            onCheckedChange = { isChecked ->
                                                cuponActivo = isChecked
                                                scope.launch {
                                                    if (isChecked) {
                                                        CarritoRepository.activarCupon(desc.codigo!!)
                                                    } else {
                                                        CarritoRepository.desactivarCupon(desc.codigo!!)
                                                    }
                                                }
                                            },
                                            // Muestra un icono de check cuando está activo
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
                                                checkedThumbColor = colors.primary,
                                                checkedTrackColor = colors.primaryContainer,
                                                uncheckedThumbColor = colors.secondary,
                                                uncheckedTrackColor = colors.secondaryContainer,
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