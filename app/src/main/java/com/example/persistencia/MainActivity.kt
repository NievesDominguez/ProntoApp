package com.example.persistencia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import coil.compose.AsyncImage
import com.example.persistencia.ui.theme.PersistenciaTheme
import com.example.persistencia.Navegacion.AppNavigation

/*
 * Estructura.kt -> Nombre de las constantes, unificar BBDD
 * Data.kt -> Clase que crea la entidad de la BBDD
 * Dao.kt -> Funciones de acceso a la BBDD
 * AppBD -> Archivo principal de la BBDD
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNavigation()
            //Formularios()
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

fun phoneVisualTransformation(): VisualTransformation {
    return VisualTransformation { text ->
        val trimmed = text.text.take(11)
        val formatted = buildString {
            if (trimmed.isNotEmpty()) append("(")
            if (trimmed.length > 3) {
                append(trimmed.substring(0, 3)).append(")")
                if (trimmed.length > 6) {
                    append(trimmed.substring(3, 6)).append("-")
                    append(trimmed.substring(6))
                } else {
                    append(trimmed.substring(3))
                }
            } else {
                append(trimmed)
            }
        }
        TransformedText(AnnotatedString(formatted), OffsetMapping.Identity)
    }
}

@Composable
fun Formularios(modifier: Modifier = Modifier) {
    var telefono by rememberSaveable { mutableStateOf("") }

    TextField(
        value = telefono,
        onValueChange = { nuevo ->
            if (nuevo.all { it.isDigit() } && nuevo.length <= 11) {
                telefono = nuevo
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        visualTransformation = phoneVisualTransformation(),
        label = { Text("Teléfono") }
    )
}


@Composable
fun Layouts() {
    Column(
        modifier = Modifier.fillMaxSize()
    )

    {
        Text(
            text = "Desarrollo en Android",
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.Cyan)
                .wrapContentHeight(align = Alignment.CenterVertically),
            textAlign = TextAlign.Center,
            textDecoration = TextDecoration.Underline,
            fontSize = 30.sp
        )
        Row(
            modifier = Modifier
                //.height(200.dp)
                .fillMaxWidth()
                .background(Color.LightGray),
            //.padding(75.dp,5.dp)
            horizontalArrangement = Arrangement.Center
        )
        {
            Text(
                text = "DAM",
                modifier = Modifier
                    .background(Color.Yellow)
                    .border(width = 3.dp, color = Color.Black)
                    .size(80.dp)
                    .padding(4.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                fontSize = 11.sp,
            )

            Spacer(Modifier.width(20.dp))

            Text(
                text = "DAW",
                modifier = Modifier
                    .size(80.dp)
                    .border(width = 3.dp, color = Color.Black, shape = CircleShape)
                    .background(Color.Green, shape = CircleShape),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
            )

            Spacer(Modifier.width(20.dp))

            Text(
                text = "ASIR",
                modifier = Modifier
                    .background(Color.Magenta)
                    .border(width = 3.dp, color = Color.Black)
                    .size(80.dp)
                    .padding(4.dp,0.dp)
                    .wrapContentHeight(align = Alignment.Bottom),
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
            )
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .background(Color.Gray)
                .size(200.dp)
                .align(Alignment.CenterHorizontally),
        )
        {
            Text(
                text = "Texto en un box",
                modifier = Modifier
                    .background(Color.DarkGray)
                    .align(Alignment.Center),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,

                )
        }
    }

}

@Composable
fun EstadoCompose(){
    Column(modifier = Modifier
        .padding(16.dp)
        .padding(top = 30.dp)
        //.width(100.dp)
        //.horizontalScroll(state = rememberScrollState())
    ){
        var name by remember { mutableStateOf("") }
        var golpes by remember { mutableStateOf(0) }
        Text(
            text = "El número de golpes es: $golpes"
        )
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .padding(horizontal = 5.dp)
                .fillMaxWidth(),
            //horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {golpes++}) {
                Text("Click")
            }
            OutlinedButton(onClick = {golpes=0}) {
                Text("Reset")
            }
        }

        if (name.isNotEmpty()) {
            Text(
                text = "Hola, $name!",
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        OutlinedTextField(
            value = name, //Valor actual del campo de texto es la variable name
            onValueChange = { name = it }, //Actualiza la variable name con el nuevo valor escrito
            label = { Text("Nombre") } //Etiqueta dentro del campo de texto para indicar qué información debe ingresarse

        )
        var isToggled by rememberSaveable { mutableStateOf(false) } //False es el valor inicial
        IconButton(onClick = { isToggled = !isToggled }) {
            Icon(
                imageVector = if (isToggled) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (isToggled) "Selected icon button" else "Unselected icon button",
                modifier = Modifier.size(25.dp)
            )
        }
        Text(
            text = "Holaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!",
            modifier = Modifier
                .padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium

        )



    }
//    Icon(
//        imageVector = Icons.Filled.Call,
//        contentDescription = null,
//        tint = Color.Red
//    )



}


@Composable
fun FilledButtonExample(onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text("Filled")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PersistenciaTheme() {
        EstadoCompose()
    }
}
