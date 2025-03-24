package org.lsp.textdocument

import org.lsp.NotificationMessage
import org.lsp.TextDocumentItem


class DidOpenTextDocumentNotification (
    override val jsonrpc: String,
    override val method: String,
    val params: DidOpenTextDocumentParams
) : NotificationMessage

class DidOpenTextDocumentParams (
    val textDocument: TextDocumentItem
)
