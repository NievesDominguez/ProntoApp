package com.example.persistencia.Pantallas

import android.util.Log
import com.example.persistencia.Modelos.Mensaje
import com.example.persistencia.Herramientas.ChatViewModel
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.example.persistencia.BuildConfig
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chatbot(
    viewModel: ChatViewModel = viewModel(),
    onBack: () -> Unit
) {
    // Estado de los mensajes
    val messages by viewModel.messages
    val isLoading by viewModel.isLoading

    // Texto del input
    var inputText by remember { mutableStateOf("") }

    // Estado de scroll de la lista
    val listState = rememberLazyListState()

    // Contexto y control del teclado
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-scroll al último mensaje
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    // Función para enviar mensaje
    fun sendMessage() {
        if (inputText.isNotBlank() && !isLoading) {
            viewModel.sendMessage(inputText, context)
            inputText = ""
            // Mantener el foco para que el teclado no se cierre
            focusRequester.requestFocus()
        }
        Log.d("API_KEY_TEST", "Key: ${BuildConfig.GROQ_API_KEY}")

    }

    // Estructura principal de la pantalla
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Asistente Pronto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Lucide.ChevronLeft,
                            contentDescription = "Atrás"
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->

        // Contenedor principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
        ) {

            // Lista de mensajes
            LazyColumn(
                modifier = Modifier
                    .weight(1f) // Ocupa todo el espacio disponible
                    .padding(horizontal = 12.dp),
                state = listState,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {

                // Mensajes del chat
                items(messages) { message ->
                    MessageBubble(message = message)
                }

                // Animación de "escribiendo..."
                if (isLoading) {
                    item {
                        var dotCount by remember { mutableStateOf(1) }

                        LaunchedEffect(Unit) {
                            while (true) {
                                delay(500)
                                dotCount = (dotCount % 3) + 1
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp),
                                tonalElevation = 3.dp,
                                modifier = Modifier.widthIn(max = 80.dp)
                            ) {
                                Text(
                                    text = "·".repeat(dotCount),
                                    modifier = Modifier.padding(14.dp),
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Barra de entrada del mensaje
            ChatInputBar(
                inputText = inputText,
                onTextChange = { inputText = it },
                isLoading = isLoading,
                onSend = { sendMessage() },
                focusRequester = focusRequester,
                modifier = Modifier.imePadding() // Ajuste al teclado
            )
        }
    }
}

@Composable
fun ChatInputBar(
    inputText: String,
    onTextChange: (String) -> Unit,
    isLoading: Boolean,
    onSend: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    // Contenedor visual del input
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface
    ) {

        // Fila que contiene el campo de texto y el botón
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Caja del campo de texto
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .focusRequester(focusRequester)
            ) {

                // Campo de texto
                BasicTextField(
                    value = inputText,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    enabled = !isLoading,
                    textStyle = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Send,
                        autoCorrect = true
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { onSend() }
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Placeholder
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "Escribe un mensaje...",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
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
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Mensaje) {

    // Determinar si el mensaje es del usuario
    val isUser = message.sender == Mensaje.Sender.USER

    // Colores según quién envía
    val bubbleColor =
        if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant

    val textColor =
        if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    // Forma del bocadillo
    val shape = if (isUser)
        RoundedCornerShape(16.dp, 0.dp, 16.dp, 16.dp)
    else
        RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp)

    // Contenedor del mensaje
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
            modifier = Modifier.widthIn(max = 280.dp)
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
