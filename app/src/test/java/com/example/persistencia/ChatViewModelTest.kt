package com.example.persistencia

import com.example.persistencia.Herramientas.ChatViewModel
import com.example.persistencia.Modelos.Mensaje
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Estado inicial contiene un mensaje de bienvenida del bot
    @Test
    fun CV01() {
        val vm = ChatViewModel()
        val mensajes = vm.messages.value

        assertEquals(1, mensajes.size)
        assertEquals(Mensaje.Sender.BOT, mensajes[0].sender)
        assertTrue(mensajes[0].content.contains("Bienvenido"))
    }

    // isLoading es false al inicio
    @Test
    fun CV02() {
        val vm = ChatViewModel()
        assertFalse(vm.isLoading.value)
    }
}