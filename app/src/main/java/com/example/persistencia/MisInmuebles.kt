package com.example.persistencia

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import coil.compose.AsyncImage
import com.composables.icons.lucide.CircleUserRound
import com.composables.icons.lucide.HousePlus
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.example.persistencia.Navegacion.AppScreens
import com.example.persistencia.localdb.AppDB
import com.example.persistencia.localdb.Estructura
import com.example.persistencia.localdb.InmuebleDao
import com.example.persistencia.localdb.InmuebleData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisInmuebles(navController: NavController) {

    val context = LocalContext.current
    val dbl = Room.databaseBuilder(context, AppDB::class.java, Estructura.DB.NAME)
        .allowMainThreadQueries().build()

    // Id del usuario de la sesión actual.
    var idSesionActual by remember {
        mutableIntStateOf(dbl.sesionDao().getEstadoSesion()?.idUsuario ?: 0)
    }

    // Listado de inmuebles del usuario actual.
    var misInmuebles = dbl.inmuebleDao().getListaInmueblesMy(idSesionActual).toMutableList()

    // Variables de los datos recogidos en los formularios
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var imagen by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var contrato by remember { mutableStateOf("Alquiler") }

    // Variables de los datos recogidos en los formularios
    var tituloActual by remember { mutableStateOf("") }
    var descripcionActual by remember { mutableStateOf("") }
    var imagenActual by remember { mutableStateOf("") }
    var precioActual by remember { mutableStateOf("") }
    var contratoActual by remember { mutableStateOf("Alquiler") }
    var idActual by remember { mutableStateOf(0) }


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
                            text = "Mis inmuebles",
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
                            navController.navigate(route = AppScreens.InmueblesTodos.route)
                            Toast.makeText(context, "Todos los inmuebles", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.HousePlus,
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
        var editar by rememberSaveable { mutableStateOf(false) } // Variable para mostrar opciones de edición o no
        var insertar by rememberSaveable { mutableStateOf(false) } // Variable para mostrar opciones de inserción o no

        // Acceso a los inmuebles para mostrar la información, presentandolo con distintos elementos componibles y formatos.
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            itemsIndexed(misInmuebles) { index, propiedad ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .padding(8.dp, 10.dp)
                            .size(85.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        model = propiedad.imagenInmueble,
                        contentScale = ContentScale.Crop,
                        contentDescription = "Imagen del inmueble",
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
                                text = propiedad.precioInmueble+" € ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                horizontalArrangement = Arrangement.End
                            ){
                                Text(
                                    text = propiedad.contratoInmueble,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Right
                                )
                            }

                        }

                        Row() {
                            IconButton(onClick = {
                                editar =
                                    !editar // Al hacer click alterna entre mostrar las opciones de edición o no

                                // Variables de los datos del inmueble actual
                                tituloActual = propiedad.tituloInmueble
                                descripcionActual =propiedad.descripcionInmueble
                                imagenActual = propiedad.imagenInmueble
                                precioActual = propiedad.precioInmueble
                                contratoActual = propiedad.contratoInmueble
                                idActual = propiedad.idInmueble

                                Toast.makeText(context, "Editar usuario", Toast.LENGTH_SHORT).show()
                            }
                            ) {
                                Icon(Icons.Filled.Edit, "edit")
                            }

                            IconButton(onClick = {
                                idActual = propiedad.idInmueble
                                inmuebleD.deleteInmueble(idActual)
                                Toast.makeText(context, "Inmueble eliminado", Toast.LENGTH_SHORT).show()
                                navController.navigate(route = AppScreens.MisInmuebles.route)
                            }
                            ) {
                                Icon(Icons.Filled.Delete, "eliminar")
                            }
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
        if (insertar) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .background(Color(0xE6F2F0F0))
                        .width(350.dp)
                        .padding(20.dp),
                ) {

                    Column(
                        modifier = Modifier.fillMaxWidth(), // Necesario para alinear la X a la derecha
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // Fila para alinear la X a la derecha
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = {
                                    insertar = !insertar // Alterna entre mostrar/ocultar edición
                                }
                            ) {
                                Icon(Lucide.X, "edit")
                            }
                        }

                        Text(
                            text = "Añadir un inmueble",
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )

                        // Pedimos el titulo
                        OutlinedTextField(
                            value = titulo,
                            onValueChange = {
                                if (it.length < 40) {
                                    titulo = it
                                }
                            },
                            label = { Text("Título del inmueble") },
                            modifier = Modifier.width(300.dp)
                        )

                        // Pedimos la descripción
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = {
                                if (it.length < 200) {
                                    descripcion = it
                                }
                            },
                            label = { Text("Descripción del inmueble") },
                            modifier = Modifier.width(300.dp)
                        )

                        // Pedimos url a la imagen
                        OutlinedTextField(
                            value = imagen,
                            onValueChange = {
                                if (it.length < 200) {
                                    imagen = it
                                }
                            },
                            label = { Text("Imagen del inmueble (url)") },
                            modifier = Modifier.width(300.dp)
                        )

                        // Pedimos el precio
                        OutlinedTextField(
                            value = precio,
                            onValueChange = {
                                if (it.length < 40) {
                                    precio = it
                                }
                            },
                            label = { Text("Precio del inmueble") },
                            modifier = Modifier.width(300.dp)
                        )

                        // Pedimos el tipo de contrato
                        val options = listOf(
                            "Alquiler",
                            "Venta"
                        )
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {

                            TextField(
                                value = contrato,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                options.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                option,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            contrato = option
                                            expanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }


                        //Spacer(modifier = Modifier.size(30.dp))


                        Spacer(Modifier.size(20.dp))

                        // Botón para mandar el formulario
                        Button(
                            onClick = {
                                // Validaciones básicas de campos
                                when {
                                    titulo.isBlank() -> {
                                        Toast.makeText(
                                            context,
                                            "El titulo no puede estar vacío",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    descripcion.isBlank() -> {
                                        Toast.makeText(
                                            context,
                                            "La descripción no puede estar vacía",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    imagen.isBlank() -> {
                                        Toast.makeText(
                                            context,
                                            "La imagen no puede estar vacía",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    precio.isBlank() -> {
                                        Toast.makeText(
                                            context,
                                            "El precio no puede estar vacío",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    contrato.isBlank() -> {
                                        Toast.makeText(
                                            context,
                                            "El tipo de contrato no puede estar vacío",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }


                                    else -> {
                                        insertar=!insertar

                                        // Almacena los datos en la base de datos Room
                                        val propiedad = InmuebleData(
                                            tituloInmueble = titulo,
                                            descripcionInmueble = descripcion,
                                            imagenInmueble = imagen,
                                            precioInmueble = precio,
                                            contratoInmueble = contrato,
                                            propietarioInmueble = idSesionActual
                                        )

                                        // Inserta una nueva propiedad
                                        inmuebleD.nuevoInmueble(propiedad)

                                        Toast.makeText(
                                            context,
                                            "Propiedad añadida",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        navController.navigate(route = AppScreens.MisInmuebles.route)


                                    }
                                }
                            }
                        ) {
                            Text(text = "Añadir")
                        }

                    }
                }
            }
        }

        if (editar) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .background(Color(0xE6F2F0F0))
                        .width(350.dp)
                        .padding(20.dp),
                ) {

                    Column(
                        modifier = Modifier.fillMaxWidth(), // Necesario para alinear la X a la derecha
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // Fila para alinear la X a la derecha
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = {
                                    editar = !editar // Alterna entre mostrar/ocultar edición
                                }
                            ) {
                                Icon(Lucide.X, "edit")
                            }
                        }

                        Text(
                            text = "Editar inmueble",
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )

                        // Pedimos el titulo
                        OutlinedTextField(
                            value = tituloActual,
                            onValueChange = {
                                if (it.length < 40) {
                                    tituloActual = it
                                }
                            },
                            label = { Text("Título del inmueble") },
                            modifier = Modifier.width(300.dp)
                        )

                        // Pedimos la descripción
                        OutlinedTextField(
                            value = descripcionActual,
                            onValueChange = {
                                if (it.length < 200) {
                                    descripcionActual = it
                                }
                            },
                            label = { Text("Descripción del inmueble") },
                            modifier = Modifier.width(300.dp)
                        )

                        // Pedimos url a la imagen
                        OutlinedTextField(
                            value = imagenActual,
                            onValueChange = {
                                if (it.length < 200) {
                                    imagenActual = it
                                }
                            },
                            label = { Text("Imagen del inmueble (url)") },
                            modifier = Modifier.width(300.dp)
                        )

                        // Pedimos el precio
                        OutlinedTextField(
                            value = precioActual,
                            onValueChange = {
                                if (it.length < 40) {
                                    precioActual = it
                                }
                            },
                            label = { Text("Precio del inmueble") },
                            modifier = Modifier.width(300.dp)
                        )

                        // Pedimos el tipo de contrato
                        val options = listOf(
                            "Alquiler",
                            "Venta"
                        )
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {

                            TextField(
                                value = contratoActual,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                options.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                option,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            contratoActual = option
                                            expanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }


                        //Spacer(modifier = Modifier.size(30.dp))


                        Spacer(Modifier.size(20.dp))

                        // Botón para mandar el formulario
                        Button(
                            onClick = {
                                // Validaciones básicas de campos
                                when {
                                    tituloActual.isBlank() -> {
                                        Toast.makeText(
                                            context,
                                            "El titulo no puede estar vacío",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    descripcionActual.isBlank() -> {
                                        Toast.makeText(
                                            context,
                                            "La descripción no puede estar vacía",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    imagenActual.isBlank() -> {
                                        Toast.makeText(
                                            context,
                                            "La imagen no puede estar vacía",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    precioActual.isBlank() -> {
                                        Toast.makeText(
                                            context,
                                            "El precio no puede estar vacío",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    contratoActual.isBlank() -> {
                                        Toast.makeText(
                                            context,
                                            "El tipo de contrato no puede estar vacío",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }


                                    else -> {
                                        // Edita los datos del inmueble
                                        inmuebleD.editar(tituloActual,descripcionActual,imagenActual,precioActual,contratoActual,idActual)
                                        editar =!editar

                                        Toast.makeText(
                                            context,
                                            "Inmueble editado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate(route = AppScreens.MisInmuebles.route)
                                    }
                                }
                            }
                        ) {
                            Text(text = "Editar inmueble")
                        }

                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp, 0.dp, 30.dp, 50.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            FloatingActionButton(
                onClick = {
                    // Al hacer click se muestra la ventana para añadir un nuevo inmueble
                    insertar =
                        !insertar // Al hacer click alterna entre mostrar las opciones de edición o no
                    Toast.makeText(
                        context,
                        "Añadir usuarios",
                        Toast.LENGTH_SHORT
                    ).show()
                },
            ) {
                Icon(Icons.Filled.Add, "Floating action button.")
            }
        }

    }
}

