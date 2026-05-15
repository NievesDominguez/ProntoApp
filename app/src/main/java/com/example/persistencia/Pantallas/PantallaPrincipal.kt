package com.example.persistencia.Pantallas

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import com.example.persistencia.Herramientas.ProductosRepository
import com.example.persistencia.Modelos.Producto
import kotlinx.coroutines.launch

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
    var productosNuevos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val usuario = FirebaseAuth.getInstance().currentUser

    // Comprueba si hay invitaciones pendientes
    LaunchedEffect(usuario!!.uid) {
        numInvitaciones = ListasRepository.getInvitacionesPendientes(usuario.uid).size
    }

    // Obtine los productos nuevos
    LaunchedEffect(Unit) {
        scope.launch {
            productosNuevos = ProductosRepository.getProductosRecientes(8)
        }
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
                "https://res.cloudinary.com/ddofwf5aq/image/upload/v1778877976/Promo1_d3ignw.png",
                "2ª unidad al 50% de descuento en productos de la marca Coca-Cola"
            ),
            CarouselItem(
                1,
                "https://res.cloudinary.com/ddofwf5aq/image/upload/v1778877985/Promo2_wgmjpz.png",
                "3x2 en tabletas de chocolate Milka"
            ),
            CarouselItem(
                2,
                "https://res.cloudinary.com/ddofwf5aq/image/upload/v1778879266/Promo3_xtqe4t.png",
                "Gratis 4 yogures naturales Danone por compras superiores a 40€"
            ),
//            CarouselItem(
//                3,
//                "https://storage.googleapis.com/media.bckts.are.external.alcampo.es/PROMOCIONES%20EXCLUSIVAS%20ONLINE/2026/OFERTAS%20EXCLUSIVAS%20%28Folleto%2003%29/ComidaPreparada.jpg",
//                "Oferta 4"
//            ),
//            CarouselItem(
//                4,
//                "https://storage.googleapis.com/media.bckts.are.external.alcampo.es/PROMOCIONES%20EXCLUSIVAS%20ONLINE/2026/OFERTAS%20EXCLUSIVAS%20%28Folleto%2003%29/SinGluten.jpg",
//                "Oferta 5"
//            )
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
                Row() {
                    // Botón de escanear
                    IconButton(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        onClick = { navController.navigate(AppScreens.Escaner.route) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = colors.onPrimary.copy(alpha = 0.8f),
                            contentColor = colors.onSurface
                        )
                    ) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = "Escanear",
                            //tint = colors.onBackground
                        )
                    }
                    Spacer(Modifier.width(6.dp))

                    // Botón de notificaciones con badge
                    IconButton(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = colors.onPrimary.copy(alpha = 0.8f),
                            contentColor = colors.onSurface
                        ),
                        onClick = {
                            navController.navigate(AppScreens.Invitaciones.route)
                        }) {
                        BadgedBox(
                            badge = {
                                if (numInvitaciones > 0) {
                                    Badge {
                                        Text(numInvitaciones.toString(), color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = colors.onBackground
                            )
                        }
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
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
                    itemWidth = 280.dp,
                    itemSpacing = 16.dp,
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) { i ->
                    val item = carouselItems[i]
                    AsyncImage(
                        model = item.imgLink,
                        contentDescription = item.contentDescription,
                        modifier = Modifier
                            .aspectRatio(1020f / 720f)
                            .maskClip(MaterialTheme.shapes.extraLarge)
                            .shadow(
                                elevation = 8.dp,
                                shape = MaterialTheme.shapes.extraLarge,
                                ambientColor = Color.Black.copy(alpha = 0.15f),
                                spotColor = Color.Black.copy(alpha = 0.15f)
                            ),
                        contentScale = ContentScale.Fit
                    )
                }

                // Novedades
                Text(
                    modifier = Modifier.padding(24.dp, 20.dp, 24.dp, 10.dp),
                    text = "Novedades",
                    color = colors.onBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    items(productosNuevos, key = { it.id }) { producto ->
                        Card(
                            modifier = Modifier
                                .width(130.dp)
                                .height(170.dp)
                                .clickable {
                                    navController.navigate(AppScreens.PantallaProducto.route + "/${producto.id}")
                                },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(1.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                // Contenedor de imagen con altura fija
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .padding(vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = producto.imagenUrl,
                                        contentDescription = producto.nombre,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Contenedor del texto con altura fija para centrar verticalmente
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp),  // Altura aproximada para 2 líneas
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = producto.nombre,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                // Precio
                                Text(
                                    text = "%.2f €".format(producto.precio),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = colors.onSurface
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(160.dp))
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