package org.lsp.textdocument

import org.lsp.Position
import org.lsp.RequestMessage
import org.lsp.ResponseMessage
import org.lsp.TextDocumentIdentifier
import org.lsp.TextDocumentPositionParams

class CompletionRequest(
    override val id: Any,
    override val method: String,
    override val jsonrpc: String,
    val params: CompletionParams
) : RequestMessage

class CompletionParams(
    override val textDocument: TextDocumentIdentifier,
    override val position: Position
) : TextDocumentPositionParams

class CompletionResponse(
    override val id: Any,
    override val jsonrpc: String,
    val result: List<CompletionItem>
) : ResponseMessage

class CompletionItem(val label: String, val detail: String, val documentation: String)
