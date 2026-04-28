package com.example.persistencia.Pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.example.persistencia.Herramientas.ChatViewModel
import com.example.persistencia.Herramientas.fondoDegradado
import com.example.persistencia.Modelos.Mensaje
import kotlinx.coroutines.delay

/**
 * Pantalla principal del chatbot.
 * Muestra el historial de mensajes, un campo de entrada y envía mensajes al ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chatbot(
    viewModel: ChatViewModel = viewModel(),
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme // Colores del tema

    val backgroundModifier = Modifier.fondoDegradado()

    // Estados del ViewModel
    val messages by viewModel.messages // Lista de mensajes
    val isLoading by viewModel.isLoading // Indicador de carga

    var inputText by remember { mutableStateOf("") } // Texto que el usuario está escribiendo

    val listState = rememberLazyListState() // Estado para el scroll automático de la lista
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() } // Para solicitar foco al campo de texto

    // Cada vez que se añade un mensaje nuevo, se desplaza automáticamente al final
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    // Función para enviar el mensaje. Limpia el input y llama al ViewModel
    fun sendMessage() {
        if (inputText.isNotBlank() && !isLoading) {
            viewModel.sendMessage(inputText, context)
            inputText = ""
            focusRequester.requestFocus() // Mantiene el foco para seguir escribiendo
        }
    }

    // Barra superior y contenido
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Asistente Pronto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onBackground
                    )
                },
                navigationIcon = {
                    // Botón para volver atrás
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Lucide.ChevronLeft,
                            contentDescription = "Atrás",
                            tint = colors.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colors.onBackground
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->

        Box(modifier = backgroundModifier.padding(paddingValues)) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Área de mensajes
                LazyColumn(
                    modifier = Modifier
                        .weight(1f) // Ocupa todo el espacio disponible
                        .padding(horizontal = 12.dp),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(message = message)
                    }

                    // Animación de ... para indicar que el bot está escribiendo
                    if (isLoading) {
                        item {
                            var dotCount by remember { mutableStateOf(1) }

                            LaunchedEffect(Unit) {
                                while (true) {
                                    delay(500)
                                    dotCount = (dotCount % 3) + 1 // Ciclo 1,2,3,1,2,3...
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Surface(
                                    color = colors.surfaceVariant,
                                    shape = RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp),
                                    tonalElevation = 3.dp,
                                    modifier = Modifier.widthIn(max = 80.dp)
                                ) {
                                    Text(
                                        text = "·".repeat(dotCount),
                                        modifier = Modifier.padding(14.dp),
                                        fontSize = 20.sp,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Barra inferior con el campo de texto y botón de enviar
                ChatInputBar(
                    inputText = inputText,
                    onTextChange = { inputText = it },
                    isLoading = isLoading,
                    onSend = { sendMessage() },
                    focusRequester = focusRequester,
                    modifier = Modifier.imePadding() // Se ajusta cuando el teclado está abierto
                )
            }
        }
    }
}

// Barra de entrada de texto y botón de enviar.
@Composable
fun ChatInputBar(
    inputText: String,
    onTextChange: (String) -> Unit,
    isLoading: Boolean,
    onSend: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Contenedor del campo de texto con fondo redondeado
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.onPrimary.copy(alpha = 0.8f))
                .focusRequester(focusRequester) // Permite solicitar el foco automáticamente
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                enabled = !isLoading,
                textStyle = LocalTextStyle.current.copy(
                    color = colors.onSurface,
                    fontSize = 16.sp
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send,
                    autoCorrect = true
                ),
                keyboardActions = KeyboardActions(
                    onSend = { onSend() } // Enviar al presionar el botón enviar del teclado
                ),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (inputText.isEmpty()) {
                            // Placeholder cuando no se ha escrito
                            Text(
                                text = "Escribe un mensaje...",
                                color = colors.onSurface.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Botón de enviar
        FilledIconButton(
            onClick = onSend,
            enabled = !isLoading,
            shape = RoundedCornerShape(50),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = colors.primary
            )
        ) {
            // Muestra un progreso mientras se espera respuesta
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colors.onPrimary
                )
            } else {
                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
            }
        }
    }
}

// Burbuja de mensaje individual
@Composable
fun MessageBubble(message: Mensaje) {
    val colors = MaterialTheme.colorScheme
    val isUser = message.sender == Mensaje.Sender.USER

    // Colores y forma según el emisor
    val bubbleColor = if (isUser) colors.primary else colors.onPrimary.copy(alpha = 0.8f)
    val textColor = if (isUser) Color.White else colors.onSurfaceVariant
    // Usuario: esquina superior derecha recta
    val shape = if (isUser)
        RoundedCornerShape(16.dp, 0.dp, 16.dp, 16.dp)
    // Bot: esquina superior izquierda recta
    else
        RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = shape,
            tonalElevation = 3.dp,
            shadowElevation = 6.dp,
            modifier = Modifier.widthIn(max = 280.dp) // Evita que la burbuja sea demasiado ancha
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(14.dp),
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = textColor
            )
        }
    }
}