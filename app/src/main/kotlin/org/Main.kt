package org

import java.io.File
import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.rpc.Rpc

fun main() {
    val logger = getLogger("/home/hemram/lsptester/log.txt")

    try {
        val inputStream = System.`in`
        val buffer = ByteArray(4096)
        var readBuffer = ByteArray(0)

        while (true) {
            val bytesRead = inputStream.read(buffer)
            if (bytesRead == -1) break

            readBuffer += buffer.copyOfRange(0, bytesRead)
            while (true) {
                val (advance, message) = Rpc().splitMessage(readBuffer)
                if (advance == 0) break

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                val timeStamp = LocalDateTime.now().format(formatter)
                logger.println("[INFO][$timeStamp]${String(message, StandardCharsets.UTF_8)}")
                logger.flush()

                readBuffer = readBuffer.copyOfRange(advance, readBuffer.size)
            }
        }
    } catch (e: Exception) {
        logger.println("Server error: ${e.stackTraceToString()}")
    }
}

fun getLogger(filename: String): PrintWriter {
    val logFile = File(filename).apply { if (!exists()) createNewFile() }
    return logFile.printWriter()
}

operator fun ByteArray.plus(other: ByteArray): ByteArray {
    val result = ByteArray(this.size + other.size)
    System.arraycopy(this, 0, result, 0, this.size)
    System.arraycopy(other, 0, result, this.size, other.size)
    return result
}

