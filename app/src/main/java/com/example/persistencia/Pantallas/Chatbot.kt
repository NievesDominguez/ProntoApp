package com.example.persistencia.Pantallas

import android.util.Log
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.example.persistencia.Herramientas.ChatViewModel
import com.example.persistencia.Modelos.Mensaje
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chatbot(
    viewModel: ChatViewModel = viewModel(),
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current

    val backgroundModifier = Modifier
        .fillMaxSize()
        .background(colors.background)
        .drawBehind {
            val edgeWidth = with(density) { 25.dp.toPx() }
            val primaryColor = colors.primary.copy(alpha = 0.1f)
            val secondaryColor = colors.secondary.copy(alpha = 0.05f)
            val width = size.width
            val height = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor, Color.Transparent),
                    startY = 0f, endY = edgeWidth
                ),
                topLeft = Offset(0f, 0f), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, primaryColor),
                    startY = height - edgeWidth, endY = height
                ),
                topLeft = Offset(0f, height - edgeWidth), size = Size(width, edgeWidth)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(secondaryColor, Color.Transparent),
                    startX = 0f, endX = edgeWidth
                ),
                topLeft = Offset(0f, 0f), size = Size(edgeWidth, height)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, secondaryColor),
                    startX = width - edgeWidth, endX = width
                ),
                topLeft = Offset(width - edgeWidth, 0f), size = Size(edgeWidth, height)
            )
        }

    val messages by viewModel.messages
    val isLoading by viewModel.isLoading

    var inputText by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    fun sendMessage() {
        if (inputText.isNotBlank() && !isLoading) {
            viewModel.sendMessage(inputText, context)
            inputText = ""
            focusRequester.requestFocus()
        }
        Log.d("API_KEY_TEST", "Key: ${BuildConfig.GROQ_API_KEY}")
    }

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
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(message = message)
                    }

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

                ChatInputBar(
                    inputText = inputText,
                    onTextChange = { inputText = it },
                    isLoading = isLoading,
                    onSend = { sendMessage() },
                    focusRequester = focusRequester,
                    modifier = Modifier.imePadding()
                )
            }
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
    val colors = MaterialTheme.colorScheme


    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.onPrimary.copy(alpha = 0.8f))
                .focusRequester(focusRequester)
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
                    onSend = { onSend() }
                ),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (inputText.isEmpty()) {
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

        FilledIconButton(
            onClick = onSend,
            enabled = !isLoading,
            shape = RoundedCornerShape(50),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = colors.primary
            )
        ) {
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

@Composable
fun MessageBubble(message: Mensaje) {
    val colors = MaterialTheme.colorScheme
    val isUser = message.sender == Mensaje.Sender.USER

    val bubbleColor = if (isUser) colors.primary else colors.onPrimary.copy(alpha = 0.8f)
    val textColor = if (isUser) Color.White else colors.onSurfaceVariant
    val shape = if (isUser)
        RoundedCornerShape(16.dp, 0.dp, 16.dp, 16.dp)
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