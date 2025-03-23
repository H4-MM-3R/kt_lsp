package org.lsp

class InitializeRequest(
   override val jsonrpc: String,
   override val id: Any,
   override val method: String,
   var params: InitializeRequestParams
) : RequestMessage

class InitializeRequestParams(
    var clientInfo: ClientInfo,
)

class ClientInfo(
    var name: String,
    var version: String,
)

class InitializeResponse(
    override val jsonrpc: String,
    override val id: Any,
    var result: InitializeResult
) : ResponseMessage

class InitializeResult(
    var capabilities: ServerCapabilities,
    var serverInfo: ServerInfo
)

class ServerCapabilities(
    var textDocumentSync: TextDocumentSyncOptions = TextDocumentSyncOptions()
)

class TextDocumentSyncOptions(
    var openClose: Boolean = true,
    var change: Int = 1  // 1 = Full sync mode
)

class ServerInfo(
    var name: String,
    var version: String
)

fun newInitializeResponse(id: Any) : InitializeResponse {
    return InitializeResponse(
        jsonrpc = "2.0",
        id = id,
        result = InitializeResult(
            capabilities = ServerCapabilities(),
            serverInfo = ServerInfo(
                name = "kt_lsp",
                version = "0.1.0"
            )
        )
    )
}
