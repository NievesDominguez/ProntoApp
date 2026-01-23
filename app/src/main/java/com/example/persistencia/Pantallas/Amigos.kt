package com.example.persistencia.Pantallas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import com.example.persistencia.localdb.AmistadData
import com.example.persistencia.localdb.AppDB
import com.example.persistencia.localdb.Estructura


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Amigos(navController: NavController) {

    val context = LocalContext.current
    val dbl = Room.databaseBuilder(context, AppDB::class.java, Estructura.DB.NAME)
        .allowMainThreadQueries().build()

    // Id del usuario de la sesión actual.
    var idSesionActual by remember {
        mutableIntStateOf(dbl.sesionDao().getEstadoSesion()?.idUsuario ?: 0)
    }

    // Listado de usuarios menos el de la sesión actual.
    var listaUsuarios = dbl.usuarioDao().getListaUsuariosMY(idSesionActual).toMutableList()

    // Para almacenar los favoritos del usuario.
    var favoritos by remember { mutableStateOf(mutableMapOf<Int, Boolean>()) }
    LaunchedEffect(Unit) {
        val listaAmistades = dbl.amistadDao().getAmistadUsuario(idSesionActual)
        favoritos = listaAmistades.associate { it.idUsuario2 to true }.toMutableMap()
    }

    //Barra superior.
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(90.dp),
                title = {
                    Text(
                        text = "Mis amigos",
                        fontSize = 15.sp
                    )
                },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                        Toast.makeText(context, "Volver atrás", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->

        // Acceso a los usuarios para mostrar la información, presentandolo con distintos elementos componibles y formatos.
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            itemsIndexed(listaUsuarios) { index, user ->
                val isFavorito = favoritos[user.idUsuario] ?: false

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iniciales = buildString {
                        append(user.nombreUsuario.first().uppercase())
                        if (user.apellidosUsuario.isNotBlank()) {
                            append(user.apellidosUsuario.first().uppercase())
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = iniciales,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = user.nombreUsuario,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = user.apellidosUsuario,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Icono favorito.
                    IconButton(
                        onClick = {
                            val nuevoEstado = !isFavorito
                            favoritos = favoritos.toMutableMap().also { it[user.idUsuario] = nuevoEstado }
                            if (nuevoEstado) {
                                dbl.amistadDao().nuevaAmistad(
                                    AmistadData(
                                        idUsuario1 = idSesionActual,
                                        idUsuario2 = user.idUsuario
                                    )
                                )
                            } else {
                                dbl.amistadDao().eliminarAmistad(idUsuario1 = idSesionActual, idUsuario2 = user.idUsuario)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorito usuario",
                            tint = if (isFavorito) {
                                Color.Red
                            } else {
                                Color.Black
                            }
                        )
                    }
                }

                // Barra horizontal.
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            }
        }
    }
}