package org.lsp

class TextDocumentItem(
    val uri: String,
    val languageId: String,
    val version: Int,
    val text: String,
)

interface DocumentIdentifier {
  val uri: String
}

class TextDocumentIdentifier(
    override val uri: String,
) : DocumentIdentifier

class VersionedTextDocumentIdentifier(
    override val uri: String,
    val version: Int,
) : DocumentIdentifier

interface TextDocumentPositionParams {
  val textDocument: TextDocumentIdentifier
  val position: Position
}

data class Position(
    val line: Int,
    val character: Int,
)

data class Location(
    val uri: String,
    val range: Range,
)

data class Range(
    val start: Position,
    val end: Position,
)

data class WorkspaceEdit(val changes: MutableMap<String, List<TextEdit>>)

data class TextEdit(val range: Range, val newText: String)
