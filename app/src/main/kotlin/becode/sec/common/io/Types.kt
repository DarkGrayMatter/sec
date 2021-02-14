package becode.sec.common.io

import java.io.Writer

enum class Eol(val value: String) {

    CRLF("\r\n"),
    CR("\r"),
    LF("\n");

    companion object {
        @JvmStatic
        fun default(): Eol = values().first { it.value == System.getProperty("line.separator") }
    }
}

fun Writer.write(eol: Eol) = write(eol.value)
