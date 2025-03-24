package org.lsp

class TextDocumentItem (
    val uri: String,
    val languageId: String,
    val version: Int,
    val text: String
)

interface NormalTextDocumentIdentifier {
    val uri: String
}

class TextDocumentIdentifier (
    override val uri: String
) : NormalTextDocumentIdentifier

class VersionedTextDocumentIdentifier (
    override val uri: String,
    val version: Int
) : NormalTextDocumentIdentifier

interface TextDocumentPositionParams{
    val textDocument: TextDocumentIdentifier
    val position: Position
}

data class Position(
    val line: Int,
    val character: Int
)
