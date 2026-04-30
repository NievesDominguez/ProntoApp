package com.example.persistencia.Pantallas

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.composables.icons.lucide.ListCheck
import com.composables.icons.lucide.Lucide
import com.example.persistencia.Herramientas.ListasRepository
import com.example.persistencia.Navegacion.AppScreens
import com.example.persistencia.Herramientas.NotificationHandler
import com.example.persistencia.Herramientas.fondoDegradado
import com.example.persistencia.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(navController: NavController) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current

    // Permiso de notificaciones
    val postNotificationPermission =
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    val notificationHandler = NotificationHandler(context)

    var numInvitaciones by remember { mutableStateOf(0) }
    val usuario = FirebaseAuth.getInstance().currentUser
    LaunchedEffect(usuario!!.uid) {
        numInvitaciones = ListasRepository.getInvitacionesPendientes(usuario.uid).size
    }

    LaunchedEffect(Unit) {
        if (!postNotificationPermission.status.isGranted) {
            postNotificationPermission.launchPermissionRequest()
        }
    }

    // Datos del carrusel
    data class CarouselItem(val id: Int, val imgLink: String, val contentDescription: String)

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

    // Fondo con bordes degradados
    Box(
        modifier = Modifier.fondoDegradado()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fila superior: título y botón de notificaciones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 40.dp, 24.dp, 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pronto",
                    color = colors.onBackground,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
                // Botón de notificaciones con badge
                IconButton(onClick = {
                    navController.navigate(AppScreens.Invitaciones.route)
                }) {
                    BadgedBox(
                        badge = {
                            if (numInvitaciones > 0) {
                                Badge {
                                    Text(numInvitaciones.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Invitaciones",
                            tint = colors.onBackground
                        )
                    }
                }
            }

            // Lema
            Text(
                modifier = Modifier.padding(24.dp, 0.dp, 24.dp, 20.dp),
                text = "Escanea, paga y listo.",
                color = colors.onBackground.copy(alpha = 0.7f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            // Tarjeta con logo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surfaceVariant
                )
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

            // Ofertas destacadas
            Text(
                modifier = Modifier.padding(24.dp, 30.dp, 24.dp, 10.dp),
                text = "Ofertas destacadas",
                color = colors.onBackground,
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

            // Botones de acción
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                // Botón Escanear
                Column(
                    modifier = Modifier.height(120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        modifier = Modifier.size(45.dp),
                        shape = CircleShape,
                        onClick = { navController.navigate(AppScreens.Escaner.route) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = colors.onPrimary.copy(alpha = 0.8f),
                            contentColor = colors.onSurface
                        )
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Escanear")
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Escanear",
                        color = colors.onBackground,
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
                        modifier = Modifier.size(45.dp),
                        shape = CircleShape,
                        onClick = { navController.navigate(AppScreens.ListaCompra.route) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = colors.onPrimary.copy(alpha = 0.8f),
                            contentColor = colors.onSurface
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
                        color = colors.onBackground,
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
                        modifier = Modifier.size(45.dp),
                        shape = CircleShape,
                        onClick = { },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = colors.onPrimary.copy(alpha = 0.8f),
                            contentColor = colors.onSurface
                        )
                    ) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Favoritos")
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Favoritos",
                        color = colors.onBackground,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Botón flotante del chatbot
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 150.dp)
        ) {
            FloatingActionButton(
                onClick = { navController.navigate(AppScreens.Chatbot.route) },
                shape = CircleShape,
                containerColor = colors.primary,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Chatbot",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}