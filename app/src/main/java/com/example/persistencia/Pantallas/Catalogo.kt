package com.example.persistencia.Pantallas

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
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
    data class CarouselItem(
        val id: Int,
        val imgLink: String,
        val contentDescription: String
    )

    // Degradado magenta a morado
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFD13CF2), Color(0xFF6C3AEC))
    )

    // Degradado magenta a morado con menos opacidad
    val gradient2 = Brush.verticalGradient(
        colors = listOf(Color(0xFF6C3AEC).copy(alpha = 0.5f), Color(0xFF6C3AEC).copy(alpha = 0.5f))
    )

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
            ),
        )
    }


    // Observamos los productos en tiempo real
        val productos by CatalogoVistaModelo.productos.collectAsState()


    // Da el color de fondo y permite que los elementos de dentro tengan un margen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient2)
            //.padding(24.dp)
    ) {

        Column(

        ) {

            Text(
                modifier = Modifier.padding(24.dp, 30.dp,24.dp,24.dp),
                text = "Novedades",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

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
                    //clipToBounds = TODO()
                )
            }


            Text(
                modifier = Modifier.padding(24.dp),
                text = "Alimentación",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(productos) { producto ->
                    ProductoCard(producto)
                }
            }


            Text(
                modifier = Modifier.padding(24.dp),
                text = "Textil",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                modifier = Modifier.padding(24.dp),
                text = "Electrónica",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun ProductoCard(producto: Producto) {
    Column(
        modifier = Modifier
            .width(170.dp)
            .background(Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(16.dp))
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
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(8.dp))

        // Nombre del producto
        Text(
            text = producto.nombre,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(4.dp))

        // Precio del producto
        Text(
            text = "${producto.precio} €",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6C3AEC)
        )
    }
}
