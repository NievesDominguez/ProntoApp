package com.example.persistencia.Pantallas

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.persistencia.Firestore.ProductosDao
import com.example.persistencia.Modelos.Producto

// ---------------------------------------------------------
// 1. EFECTO SHIMMER (para la carga)
// ---------------------------------------------------------
@SuppressLint("ModifierFactoryExtensionFunction")
@Composable
fun shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        )
    )
    return Modifier.background(Color.LightGray.copy(alpha = alpha))
}

// ---------------------------------------------------------
// 2. SKELETON DE CARGA (mientras llega Firestore)
// ---------------------------------------------------------
@Composable
fun ProductoSkeleton() {
    // Degradado magenta a morado
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFD13CF2), Color(0xFF6C3AEC))
    )

    // Da el color de fondo y permite que los elementos de dentro tengan un margen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // Imagen
            Box(
                modifier = Modifier
                    .height(280.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .then(shimmerEffect())
            )

            Spacer(Modifier.height(20.dp))

            // Nombre
            Box(
                modifier = Modifier
                    .height(26.dp)
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(8.dp))
                    .then(shimmerEffect())
            )

            Spacer(Modifier.height(12.dp))

            // Precio
            Box(
                modifier = Modifier
                    .height(22.dp)
                    .fillMaxWidth(0.3f)
                    .clip(RoundedCornerShape(8.dp))
                    .then(shimmerEffect())
            )
        }
    }
}

// ---------------------------------------------------------
// 3. PANTALLA DE PRODUCTO
// ---------------------------------------------------------
@Composable
fun PantallaProducto(idProducto: String) {

    val dao = ProductosDao()
    var producto by remember { mutableStateOf<Producto?>(null) }

    // Cargar el producto desde Firestore
    LaunchedEffect(idProducto) {
        producto = dao.getProducto(idProducto)
    }

    // Mientras carga se muestra el Skeleton como placeholder
    if (producto == null) {
        ProductoSkeleton()
        return
    }

    val p = producto!! // Hace que no pueda ser null

    // Degradado magenta a morado
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFD13CF2), Color(0xFF6C3AEC))
    )

    // Da el color de fondo y permite que los elementos de dentro tengan un margen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center,
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),
        ) {

            // Imagen del producto
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 01f)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
            ) {
                AsyncImage(
                    model = p.imagenUrl,
                    contentDescription = p.nombre,
                    modifier = Modifier
                        .height(280.dp)
                        .padding(5.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Fit,
                )
            }

            Spacer(Modifier.height(20.dp))

            // Nombre del producto
            Text(
                text = p.nombre,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Precio
            Text(
                text = "${p.precio} €",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                //color = Color(0xFF6C3AEC),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Descripción
            Text(
                text = p.descripcion,
                fontSize = 16.sp,
                color = Color.DarkGray,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(32.dp))

            // ---------------------------------------------------------
            // BOTONES
            // ---------------------------------------------------------
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Botón añadir al carrito
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF6C3AEC)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Añadir al carrito",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Botón añadir a la lista de la compra
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF3A8AEC)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Añadir a la lista de la compra",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
