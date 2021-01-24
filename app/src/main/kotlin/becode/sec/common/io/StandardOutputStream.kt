package becode.sec.common.io

import java.io.OutputStream

/**
 * Adapts the [System.out] function in such manner that behaves like an normal
 * [OutputStream], with one important difference: Closing an instance of it
 * will just flush the underlying [System.out] stream.
 */
class StandardOutputStream : OutputStream() {

    private val out = IOTarget(System::out, OutputStream::flush)

    override fun close() = out.close()
    override fun flush() = out { flush() }
    override fun write(b: Int) = out { write(b) }
    override fun write(b: ByteArray) = out { write(b) }
    override fun write(b: ByteArray, off: Int, len: Int) = out { write(b, off, len) }
}
