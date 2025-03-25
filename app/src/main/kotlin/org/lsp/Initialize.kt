package org.lsp

class InitializeRequest(
    override val jsonrpc: String,
    override val id: Any,
    override val method: String,
    val params: InitializeRequestParams
) : RequestMessage

class InitializeRequestParams(
    val clientInfo: ClientInfo,
)

class ClientInfo(
    val name: String,
    val version: String,
)

class InitializeResponse(
    override val jsonrpc: String,
    override val id: Any,
    val result: InitializeResult
) : ResponseMessage

class InitializeResult(val capabilities: ServerCapabilities, val serverInfo: ServerInfo)

class ServerCapabilities(
    val textDocumentSync: Int = 1,
    val hoverProvider: Boolean = true,
    val definitionProvider: Boolean = true,
    val codeActionProvider: Boolean = true,
    val completionProvider: MutableMap<String, Any> = mutableMapOf<String, Any>()
)

class ServerInfo(val name: String, val version: String)

fun newInitializeResponse(id: Any): InitializeResponse {
  return InitializeResponse(
      jsonrpc = "2.0",
      id = id,
      result =
          InitializeResult(
              capabilities =
                  ServerCapabilities(
                      textDocumentSync = 1,
                      hoverProvider = true,
                      definitionProvider = true,
                      codeActionProvider = true,
                      completionProvider = mutableMapOf<String, Any>()),
              serverInfo = ServerInfo(name = "kt_lsp", version = "0.1.0")))
}
