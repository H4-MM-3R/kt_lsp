package org.lsp.textdocument

import org.lsp.Range
import org.lsp.RequestMessage
import org.lsp.ResponseMessage
import org.lsp.TextDocumentIdentifier
import org.lsp.WorkspaceEdit

class CodeActionRequest(
    override val id: Any,
    override val method: String,
    override val jsonrpc: String,
    val params: CodeActionParams
) : RequestMessage

class CodeActionParams(
    val textDocument: TextDocumentIdentifier,
    val range: Range,
    val context: CodeActionContext
)

class CodeActionResponse(
    override val id: Any,
    override val jsonrpc: String,
    val result: List<CodeAction>
) : ResponseMessage

class CodeActionContext()

class CodeAction(
    val title: String,
    val edit: WorkspaceEdit?,
    val command: Command?,
)

class Command(val title: String, val command: String, val arguments: List<Any>?)
