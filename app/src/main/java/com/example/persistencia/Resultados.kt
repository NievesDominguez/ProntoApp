package com.example.persistencia

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import com.example.persistencia.localdb.AppDB
import com.example.persistencia.localdb.Estructura
import com.example.persistencia.localdb.UsuarioData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Resultados(navController: NavController) {

    // Se obtiene el contexto actual, necesario para construir la base de datos Room y mostrar Toasts.
    val context = LocalContext.current

    // --------------------------------------------------------------------------
    // PARTE DE ROOM
    // --------------------------------------------------------------------------

    val db = Room.databaseBuilder(context, AppDB::class.java, Estructura.DB.NAME)
        .allowMainThreadQueries().build()

    // Ejecuta la consulta definida en el DAO que recupera todos los usuarios de la tabla.
    var listaUsuarios = db.usuarioDao().getListaUsuarios().toMutableList()


    /*
    // Lista para guardar los datos de firebase
    val listaUsuarios = remember { mutableStateListOf<UsuarioData>() }

    // Se obtienen los datos de firestore*/
    // Con LaunchedEffect se ejecuta el código solo una vez para que no haya duplicados
    LaunchedEffect(Unit) {
        // Se obtiene una instancia de acceso a Firestore.
        val dbfire = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        // Acceso a la colección "usuarios" almacenada en Firestore.
        dbfire.collection("usuarios")
            .get()
            .addOnSuccessListener { result ->
                listaUsuarios.clear()

                // Recorre todos los documentos encontrados en la colección.
                for (doc in result) {

                    // Convierte los datos del documento en un objeto UsuarioData.
                    val usr = UsuarioData(
                        idUsuario = result.indexOf(doc)+1, // El id de usuario será el número del elemento
                        nombreUsuario = doc.getString("Nombre") ?: "",
                        apellidosUsuario = doc.getString("Apellidos") ?: "",
                        email = doc.getString("Email") ?: "",
                        incorporacionUsuario = doc.getString("Incorporacion") ?: "",
                    )

                    listaUsuarios.add(usr)
                }

                Toast.makeText(context, "Datos cargados desde Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error leyendo Firestore: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // --------------------------------------------------------------------------
    // BARRA SUPERIOR
    // --------------------------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(60.dp),
                title = {
                    Text(text = "Listado de usuarios", fontSize = 15.sp)
                },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                        Toast.makeText(context, "Volver atrás", Toast.LENGTH_SHORT).show()
                    }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "backIcon")
                    }
                }
            )
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Text(text = "ID", modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                Text(text = "Nombre", modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                Text(text = "Apellidos", modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                Text(text = "Email", modifier = Modifier.weight(2.5f), textAlign = TextAlign.Center)
                Text(text = "Fecha", modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
            }
            LazyColumn() {
                itemsIndexed(listaUsuarios) { index, user ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "${user.idUsuario}", modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                        Text(text = "${user.nombreUsuario} ", modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                        Text(text = "${user.apellidosUsuario} ", modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                        Text(text = "${user.email} ", modifier = Modifier.weight(2.5f), textAlign = TextAlign.Center)
                        Text(text = "${user.incorporacionUsuario} ", modifier = Modifier.weight(2f), textAlign =
                            TextAlign.Center)
                    }
                }
            }
            Spacer(modifier = Modifier.padding(top = 30.dp))
            Button(
                onClick = {
                    navController.popBackStack()
                    Toast.makeText(context, "Volver atrás", Toast.LENGTH_SHORT).show()
                }) {
                Text(text = "Volver")
            }
        }
    }
}
