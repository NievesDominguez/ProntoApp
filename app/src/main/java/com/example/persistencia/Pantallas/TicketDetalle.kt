package com.example.persistencia.Pantallas

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.persistencia.Firestore.TicketsDao
import com.example.persistencia.Herramientas.TicketsRepository
import com.example.persistencia.Herramientas.toFormattedString
import com.example.persistencia.Modelos.DescuentoTicket
import com.example.persistencia.Modelos.ProductoTicket
import com.example.persistencia.Modelos.Ticket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetalleScreen(ticketId: String, navController: NavController) {
    var ticket by remember { mutableStateOf<Ticket?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(ticketId) {
        ticket = TicketsRepository.getTicketById(ticketId)
        cargando = false
    }

    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val ticketBackground = Color(0xFFFFFFFF)
    val ticketText = Color(0xFF000000)
    val ticketDivider = Color(0xFFCCCCCC)

    val backgroundModifier = Modifier
        .fillMaxSize()
        .background(ticketBackground)
        .drawBehind {
            val edgeWidth = with(density) { 25.dp.toPx() }
            val primaryColor = colors.primary.copy(alpha = 0.1f)
            val secondaryColor = colors.secondary.copy(alpha = 0.05f)
            val width = size.width
            val height = size.height
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(primaryColor, Color.Transparent),
                    0f,
                    edgeWidth
                ), topLeft = Offset(0f, 0f), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, primaryColor),
                    height - edgeWidth,
                    height
                ), topLeft = Offset(0f, height - edgeWidth), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(secondaryColor, Color.Transparent),
                    0f,
                    edgeWidth
                ), topLeft = Offset(0f, 0f), size = Size(edgeWidth, height)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, secondaryColor),
                    width - edgeWidth,
                    width
                ), topLeft = Offset(width - edgeWidth, 0f), size = Size(edgeWidth, height)
            )
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de compra", color = ticketText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = ticketText)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            ticket?.let { t ->
                                try {
                                    generarPDF(context, t)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } ?: Toast.makeText(context, "Ticket no cargado", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }) {
                        Icon(Icons.Default.FileDownload, "Descargar PDF", tint = ticketText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ticketBackground)
            )
        },
        containerColor = ticketBackground
    ) { padding ->
        Box(modifier = backgroundModifier.padding(padding)) {
            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Gray)
                }
            } else if (ticket == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontró el ticket", color = ticketText)
                }
            } else {
                val t = ticket!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    TicketContent(ticket = t, textColor = ticketText, dividerColor = ticketDivider)
                }
            }
        }
    }
}

@Composable
fun TicketContent(ticket: Ticket, textColor: Color, dividerColor: Color) {
    Column {
        Text(
            text = "***** PRONTO *****",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = ticket.fecha.toFormattedString(),
                fontSize = 12.sp,
                color = textColor,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "CIF: B-12345678",
                fontSize = 12.sp,
                color = textColor,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = dividerColor, thickness = 1.dp)
        Spacer(modifier = Modifier.height(25.dp))

        ticket.productos.forEach { producto ->
            ProductoRow(producto = producto, textColor = textColor)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = dividerColor, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(12.dp))

        val totalCantidad = ticket.productos.sumOf { it.cantidad.toInt() }
        val subtotal = ticket.productos.sumOf { it.subtotal }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "$totalCantidad ART. TOTAL",
                fontSize = 14.sp,
                color = textColor,
                fontFamily = FontFamily.Monospace
            )
            Text(
                "%.2f €".format(subtotal),
                fontSize = 14.sp,
                color = textColor,
                fontFamily = FontFamily.Monospace
            )
        }

        // Desglose IVA
        val gruposIva = ticket.productos.groupBy { it.iva ?: 21 }
        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = dividerColor, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "DESGLOSE DE IVA",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Cabecera de columnas
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "TIPO",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = textColor,
                modifier = Modifier.width(50.dp)
            )
            Text(
                "BASE",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                color = textColor,
                textAlign = TextAlign.Center
            )
            Text(
                "CUOTA",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.width(60.dp),
                color = textColor,
                textAlign = TextAlign.End
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        for ((iva, productos) in gruposIva) {
            val subtotalIva = productos.sumOf { it.subtotal }
            val base = subtotalIva / (1 + iva / 100.0)
            val cuota = subtotalIva - base
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "$iva%",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = textColor,
                    modifier = Modifier.width(50.dp)
                )
                Text(
                    "%.2f".format(base),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    "%.2f".format(cuota),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.width(60.dp),
                    color = textColor,
                    textAlign = TextAlign.End
                )
            }
        }


        if (ticket.descuentos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            //Divider(color = dividerColor, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "DESCUENTOS Y CUPONES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            ticket.descuentos.forEach { descuento ->
                DescuentoRow(descuento = descuento, textColor = textColor)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = dividerColor, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "TOTAL",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontFamily = FontFamily.Monospace
            )
            Text(
                "%.2f €".format(ticket.total),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = dividerColor, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "PAGADO CON ${ticket.metodoPago.uppercase()}",
            fontSize = 12.sp,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Gracias por su compra",
            fontSize = 12.sp,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 24.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ProductoRow(producto: ProductoTicket, textColor: Color) {
    val precioUnitario = producto.subtotal / producto.cantidad
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = producto.nombre,
                fontSize = 13.sp,
                color = textColor,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )
            Text(
                text = "%.2f".format(producto.subtotal),
                fontSize = 13.sp,
                color = textColor,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.End
            )
        }
        if (producto.cantidad > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${producto.cantidad.toInt()} x ( %.2f )".format(precioUnitario),
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(60.dp))
            }
        }
    }
}

@Composable
fun DescuentoRow(descuento: DescuentoTicket, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = descuento.nombre ?: descuento.codigo,
            fontSize = 13.sp,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        Text(
            text = "-%.2f".format(descuento.descuentoAplicado),
            fontSize = 13.sp,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End
        )
    }
}


suspend fun generarPDF(context: Context, ticket: Ticket) {
    withContext(Dispatchers.IO) {

        val pageWidth = 500

        // Márgenes (más aire lateral y superior)
        val marginLeft = 35f
        val marginRight = 35f
        val marginTop = 60f

        val contentRight = pageWidth - marginRight

        var y = marginTop

        val paint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 14f
            typeface = Typeface.MONOSPACE
        }

        val boldPaint = Paint(paint).apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 16f
        }

        fun centerX(text: String, p: Paint): Float {
            return (pageWidth - p.measureText(text)) / 2
        }

        fun cortarTexto(text: String, maxWidth: Float, p: Paint): String {
            var result = text
            while (p.measureText(result) > maxWidth && result.isNotEmpty()) {
                result = result.dropLast(1)
            }
            return if (result != text) result.dropLast(3) + "..." else result
        }

        fun dividir2Lineas(text: String, maxWidth: Float, p: Paint): Pair<String, String?> {
            if (p.measureText(text) <= maxWidth) return Pair(text, null)

            var cut = text.length / 2
            while (cut > 0 && text[cut] != ' ') cut--

            val l1 = text.substring(0, cut).trim()
            val l2 = text.substring(cut).trim()

            return Pair(l1, l2)
        }

        val altura = 1200

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, altura, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val contentWidth = contentRight - marginLeft

        // Título
        val titulo = "***** PRONTO *****"
        canvas.drawText(titulo, centerX(titulo, boldPaint), y, boldPaint)
        y += 40

        // Fecha y CIF
        canvas.drawText(ticket.fecha.toFormattedString(), marginLeft, y, paint)
        canvas.drawText("CIF: B-12345678", contentRight - 140f, y, paint)
        y += 30

        canvas.drawLine(marginLeft, y, contentRight, y, paint)
        y += 35

        // Productos
        ticket.productos.forEach { p ->

            val nombre = cortarTexto(p.nombre, contentWidth - 90f, paint)

            canvas.drawText(nombre, marginLeft, y, paint)
            canvas.drawText("%.2f".format(p.subtotal), contentRight - 70f, y, paint)
            y += 22

            if (p.cantidad > 1) {
                val unitario = p.subtotal / p.cantidad

                canvas.drawText(
                    "${p.cantidad.toInt()} x (%.2f)".format(unitario),
                    marginLeft + 10,
                    y,
                    paint
                )
                y += 22
            }

            y += 12
        }

        canvas.drawLine(marginLeft, y, contentRight, y, paint)
        y += 35

        // Total artículos
        val totalCantidad = ticket.productos.sumOf { it.cantidad.toInt() }
        val subtotal = ticket.productos.sumOf { it.subtotal }

        canvas.drawText("$totalCantidad ART. TOTAL", marginLeft, y, paint)
        canvas.drawText("%.2f €".format(subtotal), contentRight - 70f, y, paint)
        y += 35

        //canvas.drawLine(marginLeft, y, contentRight, y, paint)
        //y += 35

        // Desglose IVA
        canvas.drawLine(marginLeft, y, contentRight, y, paint)
        y += 35

        // Título
        canvas.drawText("DESGLOSE DE IVA", marginLeft, y, boldPaint)
        y += 25

        // Cabeceras
        canvas.drawText("TIPO", marginLeft, y, paint)
        canvas.drawText("BASE", marginLeft + 70f, y, paint)
        canvas.drawText("CUOTA", contentRight - 70f, y, paint)
        y += 22

        val gruposIva = ticket.productos.groupBy { it.iva ?: 21 }
        for ((iva, productos) in gruposIva) {
            val subtotalIva = productos.sumOf { it.subtotal }
            val base = subtotalIva / (1 + iva / 100.0)
            val cuota = subtotalIva - base
            canvas.drawText("$iva%", marginLeft, y, paint)
            canvas.drawText("%.2f".format(base), marginLeft + 70f, y, paint)
            canvas.drawText("%.2f".format(cuota), contentRight - 70f, y, paint)
            y += 22
        }

        // Descuentos
        if (ticket.descuentos.isNotEmpty()) {
            canvas.drawText("Descuentos y cupones", marginLeft, y, boldPaint)
            y += 30

            ticket.descuentos.forEach { d ->

                val nombre = d.nombre ?: d.codigo
                val (l1, l2) = dividir2Lineas(nombre, contentWidth - 90f, paint)

                canvas.drawText(l1, marginLeft, y, paint)
                canvas.drawText("-%.2f".format(d.descuentoAplicado), contentRight - 70f, y, paint)
                y += 22

                l2?.let {
                    canvas.drawText(it, marginLeft, y, paint)
                    y += 22
                }

                y += 14
            }
        } else {
            canvas.drawText("", marginLeft, y, paint)
            y += 25
        }

        canvas.drawLine(marginLeft, y, contentRight, y, paint)
        y += 35

        // Total final
        canvas.drawText("Total", marginLeft, y, boldPaint)
        canvas.drawText("%.2f €".format(ticket.total), contentRight - 70f, y, boldPaint)
        y += 40

        canvas.drawLine(marginLeft, y, contentRight, y, paint)
        y += 40

        // Método de pago
        canvas.drawText(
            "Pagado con ${ticket.metodoPago.uppercase()}",
            centerX("Pagado con ${ticket.metodoPago.uppercase()}", paint),
            y,
            paint
        )
        y += 25

        canvas.drawText("Gracias por su compra", centerX("Gracias por su compra", paint), y, paint)

        pdfDocument.finishPage(page)

        // Guardar en Descargas
        val resolver = context.contentResolver
        val values = android.content.ContentValues().apply {
            put(
                android.provider.MediaStore.MediaColumns.DISPLAY_NAME,
                "ticket_${System.currentTimeMillis()}.pdf"
            )
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(
                android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            )
        }

        val uri = resolver.insert(
            android.provider.MediaStore.Files.getContentUri("external"),
            values
        )

        uri?.let {
            resolver.openOutputStream(it)?.use { out ->
                pdfDocument.writeTo(out)
            }
        }

        pdfDocument.close()

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "PDF generado correctamente", Toast.LENGTH_LONG).show()
        }
    }
}