package com.example.persistencia.Pantallas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import coil.compose.AsyncImage
import com.composables.icons.lucide.CircleUserRound
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.example.persistencia.Navegacion.AppScreens
import com.example.persistencia.localdb.AppDB
import com.example.persistencia.localdb.Estructura
import com.example.persistencia.localdb.InmuebleDao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InmueblesTodos(navController: NavController) {

    val context = LocalContext.current
    val dbl = Room.databaseBuilder(context, AppDB::class.java, Estructura.DB.NAME)
        .allowMainThreadQueries().build()

    // Id del usuario de la sesión actual.
    var idSesionActual by remember {
        mutableIntStateOf(dbl.sesionDao().getEstadoSesion()?.idUsuario ?: 0)
    }

    // Listado de inmuebles
    var misInmuebles = dbl.inmuebleDao().getListaInmuebles().toMutableList()

    // Variables de los datos recogidos en los formularios
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var imagen by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var contrato by remember { mutableStateOf("Alquiler") }


    val inmuebleD: InmuebleDao = dbl.inmuebleDao()


    // --------------------------------------------------------------------------
    // BARRA SUPERIOR
    // --------------------------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(90.dp),
                title = {
                    // Centra verticalmente el texto dentro del espacio disponible
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Lista de inmuebles",
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
                },

                // Icono alineado a la derecha
                actions = {
                    // Icono de perfil
                    IconButton(
                        onClick = {
                            navController.navigate(route = AppScreens.Resultados.route)
                            Toast.makeText(context, "Volver al perfil", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.CircleUserRound,
                            contentDescription = "Usuario",
                        )
                    }

                    // Icono de inmuebles
                    IconButton(
                        onClick = {
                            navController.navigate(route = AppScreens.MisInmuebles.route)
                            Toast.makeText(context, "Mis inmuebles", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.House,
                            contentDescription = "Inmuebles",
                        )
                    }

                    // Icono de log out
                    IconButton(
                        onClick = {
                            navController.navigate(route = AppScreens.Inicio.route)
                            Toast.makeText(context, "Volver atrás", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.LogOut,
                            contentDescription = "backIcon",
                        )
                    }

                }
            )
        }
    ) { innerPadding ->
        // Acceso a los inmuebles para mostrar la información, presentandolo con distintos elementos componibles y formatos.
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            itemsIndexed(misInmuebles) { index, propiedad ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .padding(8.dp, 10.dp)
                            .size(85.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        model = propiedad.imagenInmueble,
                        contentScale = ContentScale.Crop,
                        contentDescription = "Imagen del inmueble"
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = propiedad.tituloInmueble,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = propiedad.descripcionInmueble,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row() {
                            Text(
                                text = propiedad.precioInmueble + " € ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = propiedad.contratoInmueble,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Right
                                )
                            }

                        }
                        var isToggled by rememberSaveable { mutableStateOf(false) }

                        IconButton(
                            onClick = { isToggled = !isToggled }
                        ) {
                            Icon(
                                imageVector = if (isToggled) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorito usuario",
                                tint = if (isToggled) {
                                    Color.Red
                                } else {
                                    Color.Black
                                }
                            )
                        }

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