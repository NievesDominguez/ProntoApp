package com.example.persistencia.Pantallas

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Catalogo(navController: NavController) {
    data class CarouselItem(
        val id: Int,
        val imgLink: String,
        val contentDescription: String
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
}