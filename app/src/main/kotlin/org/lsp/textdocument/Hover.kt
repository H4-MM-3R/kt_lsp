package org.lsp.textdocument

import org.lsp.Position
import org.lsp.RequestMessage
import org.lsp.ResponseMessage
import org.lsp.TextDocumentIdentifier
import org.lsp.TextDocumentPositionParams

class HoverRequest(
    override val id: Any,
    override val method: String,
    override val jsonrpc: String,
    val params: HoverParams,
) : RequestMessage

class HoverParams(
    override val textDocument: TextDocumentIdentifier,
    override val position: Position,
) : TextDocumentPositionParams

class HoverResponse(
    override val id: Any,
    override val jsonrpc: String,
    val result: HoverResult,
) : ResponseMessage

class HoverResult(
    val contents: String,
)
