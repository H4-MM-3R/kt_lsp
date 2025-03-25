package org.lsp.textdocument

import org.lsp.NotificationMessage
import org.lsp.Position
import org.lsp.Range

class DiagnosticsNotification(
    override val method: String,
    override val jsonrpc: String,
    val params: DiagnosticsParams
) : NotificationMessage

data class DiagnosticsParams(val uri: String, val diagnostics: List<Diagnostic>)

data class Diagnostic(val range: Range, val severity: Int, val source: String, val message: String)

fun LineRange(line: Int, start: Int, end: Int): Range {
  return Range(Position(line, start), Position(line, end))
}

fun getDiagnosticsForFile(text: String): List<Diagnostic> {
  val diagnostics = mutableListOf<Diagnostic>()
  for ((row, line) in text.split("\n").withIndex()) {
    if (line.contains("Bad Word")) {
      val idx = line.indexOf("Bad Word")
      diagnostics.add(Diagnostic(LineRange(row, idx, idx + 8), 2, "Its Bad", "Make sure its Good"))
    }
    if (line.contains("Worst Word")) {
      val idx = line.indexOf("Worst Word")
      diagnostics.add(
          Diagnostic(LineRange(row, idx, idx + 10), 1, "Its Worst", "Make sure its Good"))
    }
  }

  return diagnostics
}
