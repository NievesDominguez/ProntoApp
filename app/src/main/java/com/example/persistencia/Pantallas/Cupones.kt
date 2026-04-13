package com.example.persistencia.Pantallas

import android.util.Log
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
import com.example.persistencia.Modelos.Descuento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cupones(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current

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
                    colors = listOf(primaryColor, Color.Transparent),
                    startY = 0f, endY = edgeWidth
                ),
                topLeft = Offset(0f, 0f), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, primaryColor),
                    startY = height - edgeWidth, endY = height
                ),
                topLeft = Offset(0f, height - edgeWidth), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(secondaryColor, Color.Transparent),
                    startX = 0f, endX = edgeWidth
                ),
                topLeft = Offset(0f, 0f), size = Size(edgeWidth, height)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, secondaryColor),
                    startX = width - edgeWidth, endX = width
                ),
                topLeft = Offset(width - edgeWidth, 0f), size = Size(edgeWidth, height)
            )
        }

    val db = FirebaseFirestore.getInstance()
    val usuario = FirebaseAuth.getInstance().currentUser
    val uid: String = usuario?.uid ?: return

    val daoOfertas = DescuentosDao()
    val daoCarrito = CarritoDao()

    val scope = rememberCoroutineScope()

    var cuponesUsuario by remember { mutableStateOf<List<String>>(emptyList()) }
    var cupones by remember { mutableStateOf<List<Descuento>>(emptyList()) }

    LaunchedEffect(uid) {
        val doc = db.collection("usuarios").document(uid).get().await()
        cuponesUsuario = doc.get("cupones") as? List<String> ?: emptyList()
        Log.d("CuponesUsuario", cuponesUsuario.toString())

        cupones = daoOfertas.getCupones()
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
            when {
                cuponesUsuario.isEmpty() && cupones.isNotEmpty() -> {
                    Text(
                        text = "No tienes cupones disponibles",
                        color = colors.onBackground,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                cupones.isEmpty() -> {
                    Text(
                        text = "Cargando cupones...",
                        color = colors.onBackground,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(cupones) { desc ->
                            if (desc.codigo in cuponesUsuario) {
                                var cuponActivo by remember { mutableStateOf(false) }

                                LaunchedEffect(desc.codigo) {
                                    cuponActivo = daoCarrito.comprobarCupon(desc.codigo)
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