package org.lsp.textdocument

import org.lsp.Location
import org.lsp.Position
import org.lsp.RequestMessage
import org.lsp.ResponseMessage
import org.lsp.TextDocumentIdentifier
import org.lsp.TextDocumentPositionParams

class DefinitionRequest(
    override val id: Any,
    override val method: String,
    override val jsonrpc: String,
    val params: DefinitionParams,
) : RequestMessage

class DefinitionParams(
    override val textDocument: TextDocumentIdentifier,
    override val position: Position,
) : TextDocumentPositionParams

class DefinitionResponse(
    override val id: Any,
    override val jsonrpc: String,
    val result: Location,
) : ResponseMessage
