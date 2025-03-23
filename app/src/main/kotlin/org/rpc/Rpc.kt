package org.rpc

import com.google.gson.Gson
import java.nio.charset.StandardCharsets

data class BaseMessage(val method: String?)

class Rpc {
    fun encodeMessage(message: Any): String {
        val content = Gson().toJson(message).toByteArray(StandardCharsets.UTF_8)
        return "Content-Length: ${content.size}\r\n\r\n${String(content, StandardCharsets.UTF_8)}"
    }

    fun decodeMessage(msg: ByteArray): Pair<String, ByteArray> {
        val parts = msg.toString(StandardCharsets.UTF_8).split("\r\n\r\n", limit = 2)
        if (parts.size < 2) return Pair("", ByteArray(0))
        val content = parts[1]
        val baseMessage = Gson().fromJson(content, BaseMessage::class.java).method ?: ""
        return Pair(baseMessage, content.toByteArray())
    }

    fun splitMessage(data: ByteArray): Pair<Int, ByteArray> {
        val parts = data.toString(StandardCharsets.UTF_8).split("\r\n\r\n", limit = 2)
        if (parts.size < 2) return Pair(0, ByteArray(0))
        val (header, content) = parts
        val headers = header.split("\r\n")
        val contentLengthLine =
                headers.find { it.startsWith("Content-Length:") } ?: return Pair(0, ByteArray(0))

        val contentLength = contentLengthLine.substring("Content-Length:".length).trim().toInt()
        if (content.length < contentLength) return Pair(0, ByteArray(0))

        val totalLength = header.length + 4 + contentLength
        return Pair(totalLength, data.copyOfRange(0, totalLength))
    }
}
