package me.andriefc.secj.core.io

import java.io.InputStream

/**
 * Adapts the [System.in] function in such a manner that it behaves like a normal
 * [InputStream].
 */
class StandardInputInputStream : InputStream() {

    private val input = IOTarget(System::`in`, ignoreClosingTarget)

    override fun close() = input.close()
    override fun read(): Int = input { read() }
    override fun read(b: ByteArray): Int = input { read(b) }
    override fun read(b: ByteArray, off: Int, len: Int): Int = input { read(b, off, len) }
    override fun available(): Int =  input { available() }

    companion object {
        private val ignoreClosingTarget = { _: Any? -> }
    }

}
