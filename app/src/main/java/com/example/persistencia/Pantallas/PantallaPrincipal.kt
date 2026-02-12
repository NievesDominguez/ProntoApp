package com.example.persistencia.Pantallas

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.composables.icons.lucide.ListCheck
import com.composables.icons.lucide.Lucide
import com.example.persistencia.Navegacion.AppScreens
import com.example.persistencia.Herramientas.NotificationHandler
import com.example.persistencia.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@RequiresApi(Build.VERSION_CODES.TIRAMISU) // Sólo Android 13 o superior (API 33)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(navController: NavController) {

    val scope = rememberCoroutineScope() // Para ejecutar corrutinas
    val context = LocalContext.current // Para acceder al sistema
    val postNotificationPermission =
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS) // Control de permisos
    val notificationHandler = NotificationHandler(context) // La clase de notificaciones
    LaunchedEffect(key1 = true) { // Al cargar la ventana pide permiso POST_NOTIFICATIONS si no se pidió. Sólo la primera vez en la primera recomposición. Pide el permiso automáticamente.
        if (!postNotificationPermission.status.isGranted) {
            postNotificationPermission.launchPermissionRequest() // Popup de permiso si no está concedido
        }
    }

    // Clase local para los elementos del carrusel
    data class CarouselItem(
        val id: Int,
        val imgLink: String,
        val contentDescription: String
    )

    // Lista fija de imagenes que se muestran en el carrusel
    val carouselItems = remember {
        listOf(
            CarouselItem(
                0,
                "https://storage.googleapis.com/media.bckts.are.external.alcampo.es/PROMOCIONES%20EXCLUSIVAS%20ONLINE/2026/OFERTAS%20EXCLUSIVAS%20%28Folleto%2003%29/Frescos.jpg",
                "Oferta 1"
            ),
            CarouselItem(
                1,
                "https://storage.googleapis.com/media.bckts.are.external.alcampo.es/PROMOCIONES%20EXCLUSIVAS%20ONLINE/2026/OFERTAS%20EXCLUSIVAS%20%28Folleto%2003%29/DesayunoMerienda.jpg",
                "Oferta 2"
            ),
            CarouselItem(
                2,
                "https://storage.googleapis.com/media.bckts.are.external.alcampo.es/PROMOCIONES%20EXCLUSIVAS%20ONLINE/2026/OFERTAS%20EXCLUSIVAS%20%28Folleto%2003%29/Lacteos.jpg",
                "Oferta 3"
            ),
            CarouselItem(
                3,
                "https://storage.googleapis.com/media.bckts.are.external.alcampo.es/PROMOCIONES%20EXCLUSIVAS%20ONLINE/2026/OFERTAS%20EXCLUSIVAS%20%28Folleto%2003%29/ComidaPreparada.jpg",
                "Oferta 4"
            ),
            CarouselItem(
                4,
                "https://storage.googleapis.com/media.bckts.are.external.alcampo.es/PROMOCIONES%20EXCLUSIVAS%20ONLINE/2026/OFERTAS%20EXCLUSIVAS%20%28Folleto%2003%29/SinGluten.jpg",
                "Oferta 5"
            )
        )
    }

    // Degradado magenta a morado
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFD13CF2), Color(0xFF6C3AEC))
    )

// Fondo degradado de la pantalla
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // Título
            Text(
                modifier = Modifier.padding(24.dp, 40.dp, 24.dp, 10.dp),
                text = "Pronto",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            // Lema
            Text(
                modifier = Modifier.padding(24.dp, 0.dp, 24.dp, 20.dp),
                text = "Escanea, paga y listo.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            // Imagen del supermercado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.5f)
                ),

            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.pronto_logo),
                        contentDescription = "Logo supermercado",
                        modifier = Modifier.size(140.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Ofertas
            Text(
                modifier = Modifier.padding(24.dp, 30.dp, 24.dp, 10.dp),
                text = "Ofertas destacadas",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Carrusel
            HorizontalUncontainedCarousel(
                state = rememberCarouselState { carouselItems.count() },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 16.dp, bottom = 16.dp),
                itemWidth = 186.dp,
                itemSpacing = 12.dp,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) { i ->
                val item = carouselItems[i]
                AsyncImage(
                    model = item.imgLink,
                    contentDescription = item.contentDescription,
                    modifier = Modifier
                        .height(205.dp)
                        .maskClip(MaterialTheme.shapes.extraLarge),
                    contentScale = ContentScale.Crop
                )
            }

            // Botones de escáner y demás
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly, // Distribuye los elementos de manera uniforme
                verticalAlignment = Alignment.Top // Se alinean arriba, así todos los botones están a la misma altura
            ) {

                // Botón Escanear
                Column(
                    modifier = Modifier.height(120.dp), // Altura fija
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        modifier = Modifier.size(65.dp),
                        shape = CircleShape,
                        onClick = { navController.navigate(AppScreens.Escaner.route) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.25f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Escanear")
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Escanear",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Botón Lista de la compra
                Column(
                    modifier = Modifier.height(120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        modifier = Modifier.size(65.dp),
                        shape = CircleShape,
                        onClick = { },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.25f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Lucide.ListCheck),
                            contentDescription = "Lista de la compra"
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Lista de la\ncompra",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Botón Favoritos
                Column(
                    modifier = Modifier.height(120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        modifier = Modifier.size(65.dp),
                        shape = CircleShape,
                        onClick = { },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.25f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Favoritos")
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Favoritos",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }


}
