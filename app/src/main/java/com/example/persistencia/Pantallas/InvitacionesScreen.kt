package com.example.persistencia.Pantallas

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.persistencia.Herramientas.ListasRepository
import com.example.persistencia.Herramientas.fondoDegradado
import com.example.persistencia.Modelos.ListaCompartida
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitacionesScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var invitaciones by remember { mutableStateOf<List<ListaCompartida>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    suspend fun recargarInvitaciones() {
        Log.d("INVITACIONES", "UID actual: $userId")
        cargando = true
        invitaciones = ListasRepository.getInvitacionesPendientes(userId)
        cargando = false
    }


    LaunchedEffect(userId) {
        recargarInvitaciones()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Invitaciones pendientes",
                        fontWeight = FontWeight.Bold,
                        color = colors.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .fondoDegradado()
                .padding(padding)
        ) {

            when {
                cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }

                invitaciones.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes invitaciones pendientes",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onBackground,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(invitaciones) { lista ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colors.onPrimary.copy(alpha = 0.85f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = lista.nombre,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.onBackground
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Lista compartida",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.onBackground.copy(alpha = 0.7f)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {

                                        TextButton(
                                            onClick = {
                                                scope.launch {
                                                    ListasRepository.rechazarInvitacion(lista.id, userId)
                                                    recargarInvitaciones()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colors.secondary,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Text("Rechazar")
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    ListasRepository.aceptarInvitacion(lista.id, userId)
                                                    recargarInvitaciones()

                                                    navController.navigate("ListaCompra") {
                                                        popUpTo(0) {
                                                            inclusive = true
                                                        }
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colors.primary,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Text("Aceptar")
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
}