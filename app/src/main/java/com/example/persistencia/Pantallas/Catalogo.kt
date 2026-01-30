package com.example.persistencia.Pantallas

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.scrollableArea
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.persistencia.Firestore.CatalogoVistaModelo
import com.example.persistencia.Modelos.Producto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Catalogo(navController: NavController, vistaModelo: CatalogoVistaModelo = viewModel()) {

    // Clase local para los elementos del carousel
    data class CarouselItem(
        val id: Int,
        val imgLink: String,
        val contentDescription: String
    )

    // Degradado de fondo semitransparente
    val gradient2 = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF6C3AEC).copy(alpha = 0.5f),
            Color(0xFF6C3AEC).copy(alpha = 0.5f)
        )
    )

    // Lista fija de imagenes que se muestran en el carousel
    val carouselItems = remember {
        listOf(
            CarouselItem(
                0,
                "https://static.wikia.nocookie.net/stormlightarchive/images/7/74/DYKTW_AI.jpg/revision/latest?cb=20200907042721",
                "Kaladin Stormblessed"
            ),
            CarouselItem(
                1,
                "https://preview.redd.it/druxluzse9251.jpg?auto=webp&s=94df46fa03e0c806d7c34960c8a7f01e13ff68c3",
                "Shallan Davar"
            ),
            CarouselItem(
                2,
                "https://mir-s3-cdn-cf.behance.net/project_modules/hd_webp/a7604883668543.5d4333d984f3d.jpg",
                "Dalinar Kholin"
            ),
            CarouselItem(
                3,
                "https://i.redd.it/bfo6og6cj69b1.jpg",
                "Jasnah Kholin"
            ),
            CarouselItem(
                4,
                "https://uploads.coppermind.net/thumb/Skybreaker_by_Petar_Penev.jpg/800px-Skybreaker_by_Petar_Penev.jpg",
                "Szeth"
            )
        )
    }

    // Observamos en tiempo real la lista de productos del ViewModel
    val productos by vistaModelo.productos.collectAsState()

    // Caja principal que ocupa toda la pantalla y aplica el fondo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient2)
    ) {

        // Con un LazyColumn único se puede hacer scroll vertical de toda la pantalla
        // No usar Column infinito y dentro un LazyColumn
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {

           // Novedades
            item {
                Text(
                    modifier = Modifier.padding(24.dp, 30.dp, 24.dp, 24.dp),
                    text = "Novedades",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Carrusel
            item {
                HorizontalUncontainedCarousel(
                    state = rememberCarouselState { carouselItems.count() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 16.dp, bottom = 16.dp),
                    itemWidth = 186.dp,
                    itemSpacing = 8.dp,
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
            }

            // Alimentación
            item {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = "Alimentación",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }


            // Grid con dos columnas. No se usa grid porque entra el conflicto con el scroll de la pantalla
            items(productos.chunked(2)) { fila ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Dibujamos los productos de la fila
                    fila.forEach { producto ->
                        TarjetaProducto(producto)
                    }

                    // Si la fila tiene solo 1 producto, rellenamos el hueco
                    if (fila.size == 1) {
                        Spacer(modifier = Modifier.width(170.dp))
                    }
                }
            }

            // Textil
            item {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = "Textil",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Electrónica
            item {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = "Electrónica",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Espacio final para que el ultimo elemento no quede pegado al borde
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}



@Composable
fun TarjetaProducto(producto: Producto) {

    // Tarjeta blanca redondeada
    Column(
        modifier = Modifier
            .width(170.dp)
            .height(170.dp)
            .background(
                Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {

        // Imagen del producto
        AsyncImage(
            model = producto.imagenUrl,
            contentDescription = producto.nombre,
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(8.dp))

        // Nombre del producto (maximo 2 lineas)
        Text(
            text = producto.nombre,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(4.dp))

        // Precio del producto destacado en color
        Text(
            text = "${producto.precio} €",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6C3AEC)
        )
    }
}
