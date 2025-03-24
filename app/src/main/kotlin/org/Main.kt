package org

import com.google.gson.Gson
import java.io.File
import java.io.OutputStream
import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.analysis.State
import org.lsp.InitializeRequest
import org.lsp.newInitializeResponse
import org.lsp.textdocument.DidChangeTextDocumentNotification
import org.lsp.textdocument.DidOpenTextDocumentNotification
import org.lsp.textdocument.HoverRequest
import org.lsp.textdocument.HoverResponse
import org.lsp.textdocument.HoverResult
import org.rpc.Rpc

fun main() {
    val logger = getLogger("/home/hemram/lsptester/log.txt")
    var state = State()
    try {
        val inputStream = System.`in`
        val outputStream = System.`out`
        val buffer = ByteArray(4096)
        var readBuffer = ByteArray(0)

        while (true) {
            val bytesRead = inputStream.read(buffer)
            if (bytesRead == -1) break

            readBuffer += buffer.copyOfRange(0, bytesRead)
            while (true) {
                val (advance, message) = Rpc().splitMessage(readBuffer)
                if (advance == 0) break
                val (method, content) = Rpc().decodeMessage(message)

                handleMessage(logger, method, content, outputStream, state)

                readBuffer = readBuffer.copyOfRange(advance, readBuffer.size)
                logger.flush()
            }
        }
    } catch (e: Exception) {
        logger.println("Server error: ${e.stackTraceToString()}")
    }
}

fun handleMessage(
        logger: PrintWriter,
        method: String,
        content: ByteArray,
        outputStream: OutputStream,
        state: State
) {
    val timeStamp =
            "[${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))}]\t"

    logger.println("$timeStamp Recieved message with method: $method")

    when (method) {
        "initialize" -> {
            var req =
                    Gson().fromJson(
                                    content.toString(StandardCharsets.UTF_8),
                                    InitializeRequest::class.java
                            )
            val clientInfo = req.params.clientInfo
            logger.println("$timeStamp Connected to: ${clientInfo.name} ${clientInfo.version}")

            val encodedResp = Rpc().encodeMessage(newInitializeResponse(req.id))
            synchronized(outputStream) {
                outputStream.write(encodedResp.toByteArray())
                outputStream.flush()
            }

            logger.println("$timeStamp Sent response")
        }
        "textDocument/didOpen" -> {
            var req =
                    Gson().fromJson(
                                    content.toString(StandardCharsets.UTF_8),
                                    DidOpenTextDocumentNotification::class.java
                            )
            var textDocument = req.params.textDocument
            logger.println("$timeStamp Opened: ${textDocument.uri}")
            state.documents[textDocument.uri] = textDocument.text
        }
        "textDocument/didChange" -> {
            var req =
                    Gson().fromJson(
                                    content.toString(StandardCharsets.UTF_8),
                                    DidChangeTextDocumentNotification::class.java
                            )
            var params = req.params
            logger.println("$timeStamp Changed: ${params.textDocument.uri}")
            for ((_, change) in params.contentChanges.withIndex()) {
                state.documents[params.textDocument.uri] = change.text
            }
        }
        "textDocument/hover" -> {
            var req =
                    Gson().fromJson(
                                    content.toString(StandardCharsets.UTF_8),
                                    HoverRequest::class.java
                            )
            logger.println("$timeStamp Hover: ${req.params.textDocument.uri}")
            val doc = state.documents[req.params.textDocument.uri]
            var resp = HoverResponse(
                jsonrpc = req.jsonrpc,
                id = req.id,
                result = HoverResult("File: ${req.params.textDocument.uri}, Characters: ${doc?.length ?: 0}")
            )

            synchronized(outputStream) {
                outputStream.write(Rpc().encodeMessage(resp).toByteArray())
                outputStream.flush()
            }
        }
    }
}

fun getLogger(filename: String): PrintWriter {
    val logFile = File(filename).apply { if (!exists()) createNewFile() }
    return logFile.printWriter()
}

operator fun ByteArray.plus(other: ByteArray): ByteArray {
    val result = ByteArray(this.size + other.size)
    System.arraycopy(this, 0, result, 0, this.size)
    System.arraycopy(other, 0, result, this.size, other.size)
    return result
}
