package com.example.botones

import android.R.attr.offset
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonColors
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation.width
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.botones.ui.theme.BotonesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            //Botones()
            //Textos()
            Imagenes()
        }
    }
}

// Formatear documento: Ctrl + Alt + L
@Composable
fun Botones() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            // Aquí el código que quieres ejecutar al hacer clic en el botón
        }) {
            Text("Filled")
        }

        FilledTonalButton(onClick = {
            // Aquí el código que quieres ejecutar al hacer clic en el botón
        }) {
            Text("Tonal")
        }

        OutlinedButton(onClick = {
            // Aquí el código que quieres ejecutar al hacer clic en el botón
        }) {
            Text("Outlined")
        }

        ElevatedButton(onClick = {
            // Aquí el código que quieres ejecutar al hacer clic en el botón
        }) {
            Text("Elevated")
        }

        TextButton(onClick = {
            // Aquí el código que quieres ejecutar al hacer clic en el botón
        }) {
            Text("Text Button")
        }

        var isToggled by rememberSaveable { mutableStateOf(false) } // Variable para saber si el corazón está relleno o no
        // Botón de icono
        IconButton(
            onClick = { isToggled = !isToggled } // Al hacer click alterna entre activo o no
        ) {

            Icon(

                imageVector = if (isToggled) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (isToggled) "Selected icon button" else "Unselected icon button.",
                modifier = Modifier.size(25.dp)
            )
        }

        var golpes by remember { mutableStateOf(0) } // Inicialización de la variable mutable golpes

        Text(
            text = "El número de golpes es: $golpes",
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        Row() {
            Button(onClick = {
                golpes++;
            }) {
                Text("Click")
            }

            OutlinedButton(onClick = {
                golpes = 0;
            }) {
                Text("Reset")
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            var name by remember { mutableStateOf("") }
            if (name.isNotEmpty()) {
                Text(
                    text = "Hola, $name!",
                    modifier = Modifier.padding(bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            OutlinedTextField(
                value = name, // Valor actual del campo de texto es la variable name
                onValueChange = {
                    name = it
                }, // Actualiza la variable name con el nuevo valor escrito
                label = { Text("Nombre") } // Etiqueta dentro del campo de texto para indicar qué información debe ingresarse
            )
        }


        var img by rememberSaveable { mutableStateOf(false) } // Variable para mostrar una imagen o no
        Button(
            onClick = { img = !img } // Al hacer click alterna entre mostrar la imagen o no
        ) {
            Text("Mostrar imagen")
        }

        // La imagen solo se muestra si la variable img es verdadera
        if (img) {
            Image(
                painter = painterResource(id = R.drawable.netsuite),
                contentDescription = "Imagen con toggle de visibilidad",

                Modifier.size(100.dp)
            )
        }


        // Botón segmentado de selección única
        var selectedIndex by remember { mutableIntStateOf(0) }
        val options = listOf("Day", "Month", "Week") // Lista con las opciones en el botón
        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, label -> // Coloca cada opción
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape( // Forma del botón
                        index = index,
                        count = options.size
                    ),
                    colors = SegmentedButtonDefaults.colors(),
                    //colors = SegmentedButtonColors(Color.Red,Color.Blue,Color.Black,Color.White,Color.Blue,Color.Black,Color.Gray,Color.Gray,Color.Gray,Color.Gray,Color.Gray,Color.Gray),
                    onClick = {
                        selectedIndex = index
                    }, // Al hacer click marca como seleccionado ese botón
                    selected = index == selectedIndex,
                    label = {
                        Text(label)
                    }
                )
            }
        }

        // Botón segmentado de selección múltiple
        Column(modifier = Modifier.padding(30.dp)) {
            val selectedOptions = remember {
                mutableStateListOf(
                    false,
                    false,
                    false
                )
            } // Lista mutable que guarda qué opciones están marcadas (true/false)
            val options = listOf(
                "Buscar",
                "Compartir",
                "Ajustes"
            ) // Lista de etiquetas que representan cada botón del segmento

            // Fila que contiene los botones segmentados de selección múltiple.
            MultiChoiceSegmentedButtonRow {
                // Recorre cada opción usando su índice para relacionarla con selectedOptions
                options.forEachIndexed { index, label ->
                    // Cada botón segmentado
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, // Índice del botón dentro de la fila.
                            count = options.size // Total de botones para definir la forma correcta
                        ),

                        checked = selectedOptions[index],
                        // Alterna el estado de selección al pulsarlo.
                        onCheckedChange = {
                            selectedOptions[index] = !selectedOptions[index]
                        },
                        // Icono que cambia automáticamente según si está seleccionado
                        icon = { SegmentedButtonDefaults.Icon(selectedOptions[index]) },
                        // Contenido visual del botón (en este caso, solo un icono según la opción)
                        label = {
                            when (label) {
                                "Buscar" -> Icon(
                                    imageVector =
                                        Icons.Default.Search,
                                    contentDescription = "Búsqueda"
                                )

                                "Compartir" -> Icon(
                                    imageVector =
                                        Icons.Default.Share,
                                    contentDescription = "Compartir"
                                )

                                "Ajustes" -> Icon(
                                    imageVector =
                                        Icons.Default.Settings,
                                    contentDescription = "Ajustes"
                                )
                            }
                        }
                    )
                }
            }
        }


    }




    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp, 0.dp, 0.dp, 30.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        FloatingActionButton(
            onClick = {
                // Aquí el código que quieres ejecutar al hacer clic en el botón
            },
        ) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }

        SmallFloatingActionButton(
            onClick = {// Aquí el código que quieres ejecutar al hacer clic en el botón
            },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.secondary
        ) {
            Icon(Icons.Filled.Add, "Small floating action button.")
        }

        LargeFloatingActionButton(
            onClick = {// Aquí el código que quieres ejecutar al hacer clic en el botón
            },
            shape = CircleShape,
        ) {
            Icon(Icons.Filled.Add, "Large floating action button")
        }

        ExtendedFloatingActionButton(
            onClick = { // Aquí el código que quieres ejecutar al hacer clic en el botón
            },
            icon = { Icon(Icons.Filled.Edit, "Extended floating action button.") },
            text = { Text(text = "Extended FAB") },
        )


    }


}

@Composable
fun Imagenes() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.netsuite),
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier.width(100.dp)
        )

        Icon(
            imageVector = Icons.Filled.Call,
            contentDescription = "icono de llamada",
            tint = Color.Red
        )

        Image(
            modifier = Modifier.width(100.dp).height(100.dp),
            painter = painterResource(id = R.drawable.giorno),
            contentDescription = "Giorno Giovanna",
            contentScale = ContentScale.Crop
        )

        /**
         * Necesita varios imports para funcionar:
         * import coil.compose.AsyncImage
         * import coil.request.ImageRequest
         * import androidx.compose.ui.platform.LocalContext
         * Y la implementación:
         * implementation("io.coil-kt:coil-compose:2.7.0")
         * También dar permiso de internet en AndroidManifest.xml:
         * <uses-permission android:name="android.permission.INTERNET" />
         */
        AsyncImage(
            model = "https://i.pinimg.com/originals/ce/2a/a4/ce2aa4b802e2645bb741353f3e519d9f.jpg",
            contentDescription = "Imagen de un león",
        )
    }

}

@Composable
fun Textos() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row() {
            // Scroll vertical
            Column(
                modifier = Modifier
                    .background(color = Color.LightGray)
                    .padding(horizontal = 10.dp, vertical = 30.dp)
                    .size(100.dp)
                    .verticalScroll(state = rememberScrollState())
            ) {
                repeat(10) {
                    Text("Item $it", modifier = Modifier.padding(2.dp))
                }
            }

            // Scroll vertical y horizontal
            Column(
                modifier = Modifier
                    .background(color = Color.Gray)
                    .padding(horizontal = 0.dp, vertical = 30.dp)
                    .size(100.dp)
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(state = rememberScrollState())
            ) {
                repeat(10) {
                    Text(
                        "Item $it y texto para que se salga por la derecha.",
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }

        Text(
            text = "Hola, mundo",
            color = Color.Red,
            fontSize = 30.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
            textAlign = TextAlign.Right
        )

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append("H")
                }
                append("ello ")

                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Red)) {
                    append("W")
                }
                append("orld")
            }
        )

        Box(
            modifier = Modifier.background(Color.Gray) // Visualizar el color de fondo gris
        ) {
            Text(
                text = "Texto muestra",
                modifier = Modifier
                    .size(100.dp) // Tamaño del texto
                    .background(Color.Blue) // Fondo para visualizar el texto de color azul
                    .offset(x = 20.dp, y = 30.dp) // Desplaza texto 20dp a la derecha y 30dp abajo
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BotonesTheme {

    }
}

/*
@Preview
@Composable
fun ToggleIconButtonExample() {
    // isToggled initial value should be read from a view model or persistent storage.
    var isToggled by rememberSaveable { mutableStateOf(false) }

    IconButton(
        onClick = { isToggled = !isToggled }
    ) {
        Icon(
            painter = if (isToggled) painterResource(R.drawable.favorite_filled) else painterResource(R.drawable.favorite),
            contentDescription = if (isToggled) "Selected icon button" else "Unselected icon button."
        )
    }
}*/