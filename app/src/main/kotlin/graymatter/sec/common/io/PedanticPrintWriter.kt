@file:Suppress("MemberVisibilityCanBePrivate")

package graymatter.sec.common.io

import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.Writer
import java.nio.charset.Charset

/**
 * A special [PrintWriter] which uses a specific [Eol] sequence. This is mostly use to force formatting
 * of text for a specific platform. For example generating a certificate unix/macosx EOL on windows, or visa versa.
 */
class PedanticPrintWriter(
    out: Writer,
    val eol: Eol = Eol.default(),
    autoFlush: Boolean = false
) : PrintWriter(out, autoFlush) {

    constructor(
        output: OutputStream,
        eol: Eol = Eol.default(),
        charset: Charset = Charset.defaultCharset(),
        autoFlush: Boolean = true
    ) : this(OutputStreamWriter(output, charset), eol)

    override fun println() {
        write(eol)
    }

    override fun println(x: Boolean) {
        print(x)
        println()
    }

    override fun println(x: Char) {
        print(x)
        println()
    }

    override fun println(x: Int) {
        print(x)
        println()
    }

    override fun println(x: Long) {
        print(x)
        println()
    }

    override fun println(x: Float) {
        print(x)
        println()
    }

    override fun println(x: Double) {
        print(x)
        println()
    }

    override fun println(x: CharArray) {
        print(x)
        println()
    }

    override fun println(x: String?) {
        print(x)
        println()
    }

    override fun println(x: Any?) {
        print(x)
        println()
    }

}
