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
import org.lsp.Location
import org.lsp.Position
import org.lsp.Range
import org.lsp.TextEdit
import org.lsp.WorkspaceEdit
import org.lsp.newInitializeResponse
import org.lsp.textdocument.CodeAction
import org.lsp.textdocument.CodeActionRequest
import org.lsp.textdocument.CodeActionResponse
import org.lsp.textdocument.CompletionItem
import org.lsp.textdocument.CompletionRequest
import org.lsp.textdocument.CompletionResponse
import org.lsp.textdocument.DefinitionRequest
import org.lsp.textdocument.DefinitionResponse
import org.lsp.textdocument.DiagnosticsNotification
import org.lsp.textdocument.DiagnosticsParams
import org.lsp.textdocument.DidChangeTextDocumentNotification
import org.lsp.textdocument.DidOpenTextDocumentNotification
import org.lsp.textdocument.HoverRequest
import org.lsp.textdocument.HoverResponse
import org.lsp.textdocument.HoverResult
import org.lsp.textdocument.getDiagnosticsForFile
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
    state: State,
) {
  val timeStamp =
      "[${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))}]\t"

  logger.println("$timeStamp Recieved message with method: $method")

  when (method) {
    "initialize" -> {
      var req =
          Gson().fromJson(content.toString(StandardCharsets.UTF_8), InitializeRequest::class.java)
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
          Gson()
              .fromJson(
                  content.toString(StandardCharsets.UTF_8),
                  DidOpenTextDocumentNotification::class.java)
      var textDocument = req.params.textDocument
      logger.println("$timeStamp Opened: ${textDocument.uri}")
      state.documents[textDocument.uri] = textDocument.text

      var resp =
          DiagnosticsNotification(
              "textDocument/publishDiagnostics",
              req.jsonrpc,
              DiagnosticsParams(
                  req.params.textDocument.uri, getDiagnosticsForFile(textDocument.text)))
      synchronized(outputStream) {
        outputStream.write(Rpc().encodeMessage(resp).toByteArray())
        outputStream.flush()
      }
    }
    "textDocument/didChange" -> {
      var req =
          Gson()
              .fromJson(
                  content.toString(StandardCharsets.UTF_8),
                  DidChangeTextDocumentNotification::class.java)
      var params = req.params
      logger.println("$timeStamp Changed: ${params.textDocument.uri}")
      for ((_, change) in params.contentChanges.withIndex()) {
        state.documents[params.textDocument.uri] = change.text
        var resp =
            DiagnosticsNotification(
                "textDocument/publishDiagnostics",
                req.jsonrpc,
                DiagnosticsParams(req.params.textDocument.uri, getDiagnosticsForFile(change.text)))
        synchronized(outputStream) {
          outputStream.write(Rpc().encodeMessage(resp).toByteArray())
          outputStream.flush()
        }
      }
    }
    "textDocument/hover" -> {
      var req = Gson().fromJson(content.toString(StandardCharsets.UTF_8), HoverRequest::class.java)
      logger.println("$timeStamp Hover: ${req.params.textDocument.uri}")
      val doc = state.documents[req.params.textDocument.uri]
      var resp =
          HoverResponse(
              jsonrpc = req.jsonrpc,
              id = req.id,
              result =
                  HoverResult(
                      "File: ${req.params.textDocument.uri}, Characters: ${doc?.length ?: 0}"))

      synchronized(outputStream) {
        outputStream.write(Rpc().encodeMessage(resp).toByteArray())
        outputStream.flush()
      }
    }
    "textDocument/definition" -> {
      var req =
          Gson().fromJson(content.toString(StandardCharsets.UTF_8), DefinitionRequest::class.java)
      logger.println("$timeStamp Definition: ${req.params.textDocument.uri}")
      val position = Position(req.params.position.line - 1, 0)
      var resp =
          DefinitionResponse(
              req.id,
              req.jsonrpc,
              Location(req.params.textDocument.uri, Range(start = position, end = position)))
      synchronized(outputStream) {
        outputStream.write(Rpc().encodeMessage(resp).toByteArray())
        outputStream.flush()
      }
    }
    "textDocument/codeAction" -> {
      var req =
          Gson().fromJson(content.toString(StandardCharsets.UTF_8), CodeActionRequest::class.java)

      var uri = req.params.textDocument.uri
      var text = state.documents[uri] ?: ""
      var actions = mutableListOf<CodeAction>()

      for ((row, line) in text.split("\n").withIndex()) {
        var idx = line.indexOf("Bad Word", 0, true)
        if (idx != -1) {
          var replaceChange: MutableMap<String, List<TextEdit>> = mutableMapOf()
          replaceChange[uri] =
              listOf(TextEdit(Range(Position(row, idx), Position(row, idx + 8)), "Good Word"))
          actions.add(CodeAction("Replace Bad Word", WorkspaceEdit(replaceChange), null))

          var censorChange: MutableMap<String, List<TextEdit>> = mutableMapOf()
          censorChange[uri] =
              listOf(TextEdit(Range(Position(row, idx), Position(row, idx + 8)), "B** **rd"))
          actions.add(CodeAction("Censor Bad Word", WorkspaceEdit(censorChange), null))
        }
      }

      val resp = CodeActionResponse(req.id, req.jsonrpc, actions)

      synchronized(outputStream) {
        outputStream.write(Rpc().encodeMessage(resp).toByteArray())
        outputStream.flush()
      }
    }
    "textDocument/completion" -> {
      var req =
          Gson().fromJson(content.toString(StandardCharsets.UTF_8), CompletionRequest::class.java)

      val resp =
          CompletionResponse(
              req.id,
              req.jsonrpc,
              listOf(
                  CompletionItem(
                      "languageServerProtocol",
                      "Full form of LSP",
                      "its litrally a protocol which needs current set of data to send and receive, EZ")))

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
