package org.lsp

interface Message {
    val jsonrpc: String
}

interface RequestMessage : Message{
    val id: Any
    val method: String

    // params...
}

interface ResponseMessage : Message {
    val id: Any

    // Result or Error
}

interface NotificationMessage : Message{
    val method: String

    // params...
}
