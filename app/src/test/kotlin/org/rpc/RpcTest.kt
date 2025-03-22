package org.rpc

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class RpcTest {
    @Test
    fun validateEncodeMessage() {
        val rpc = Rpc()
        val message =  mapOf("testing" to true)
        val encodedMessage = rpc.encodeMessage(message)
        assertEquals(
                "Content-Length: 16\r\n\r\n{\"testing\":true}",
                encodedMessage
        )
    }

    @Test
    fun validateDecodeMessage() {
        val rpc = Rpc()
        val (message, content) = rpc.decodeMessage("Content-Length: 13\r\n\r\n{\"method\":hi}".toByteArray())
        val (num, arr) = rpc.splitMessage("Content-Length: 13\r\n\r\n{\"method\":hi}".toByteArray())
        println(num)
        println(arr.toString(StandardCharsets.UTF_8))
        assertEquals("hi", message)
        assertArrayEquals("{\"method\":hi}".toByteArray(), content)
    }
}
