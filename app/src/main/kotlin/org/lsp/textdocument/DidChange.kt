package org.lsp.textdocument

import org.lsp.NotificationMessage
import org.lsp.TextDocumentItem
import org.lsp.VersionedTextDocumentIdentifier


class DidChangeTextDocumentNotification (
    override val jsonrpc: String,
    override val method: String,
    val params: DidChangeTextDocumentParams
) : NotificationMessage

class DidChangeTextDocumentParams (
    val textDocument: VersionedTextDocumentIdentifier,
    val contentChanges: List<TextDocumentContextChangeEvent>
)

class TextDocumentContextChangeEvent (
    val text :String
)
