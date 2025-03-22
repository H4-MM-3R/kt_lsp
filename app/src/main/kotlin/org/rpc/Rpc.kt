package org.rpc

import com.google.gson.Gson
import java.nio.charset.StandardCharsets

data class BaseMessage(val method: String?)

class Rpc {
    fun encodeMessage(message: Any): String {
        val content = Gson().toJson(message).toByteArray(StandardCharsets.UTF_8)
        return "Content-Length: ${content.size}\r\n\r\n${String(content, StandardCharsets.UTF_8)}"
    }

    fun decodeMessage(msg: ByteArray): Pair<String?, ByteArray> {
        val (header, content) = msg.toString(StandardCharsets.UTF_8).split("\r\n\r\n", limit = 2)
        val contentLength = header.substring("Content-Length: ".length).trim().toInt()
        val baseMessage =
                Gson().fromJson(content.substring(0, contentLength), BaseMessage::class.java).method
        return Pair(baseMessage, content.toByteArray())
    }

    fun splitMessage(data: ByteArray): Pair<Int, ByteArray> {
        val (header, content) = data.toString(StandardCharsets.UTF_8).split("\r\n\r\n", limit = 2)
        val contentLength = header.substring("Content-Length: ".length).trim().toInt()

        if (content.length < contentLength) return Pair(0, ByteArray(0))
        
        val totalLength = header.length + contentLength + 4
        return Pair(totalLength, data.copyOfRange(0, totalLength))
    }
}
