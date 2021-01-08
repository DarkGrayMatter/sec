@file:Suppress("MemberVisibilityCanBePrivate")

package me.andriefc.secj.io

import java.io.EOFException
import java.io.File
import java.io.OutputStream
import java.util.*

sealed class Output {

    abstract fun <T> invoke(emit: OutputStream.() -> T): T

    class FilePath(val path: String, val canonical: Boolean, val bufferSize: Int = DEFAULT_BUFFER_SIZE) : Output() {
        override fun <T> invoke(emit: OutputStream.() -> T): T {

            val file = File(path).run {
                when {
                    canonical -> canonicalFile
                    else -> this
                }
            }

            val out = when (bufferSize) {
                0 -> file.outputStream()
                else -> file.outputStream().buffered(bufferSize)
            }

            return out.use(emit)
        }
    }

    object StdOut : Output() {
        override fun <T> invoke(emit: OutputStream.() -> T): T {

            val stdout = object : OutputStream() {

                private var eof = false

                @Synchronized
                override fun close() {
                    if (!eof) {
                        eof = true
                    }
                }

                @Synchronized
                override fun write(b: Int) {
                    checkEof()
                    System.out.write(b)
                }

                @Synchronized
                override fun write(b: ByteArray, off: Int, len: Int) {
                    checkEof()
                    Objects.checkFromIndexSize(off, len, b.size)
                    System.out.write(b, off, len)
                }

                @Synchronized
                override fun flush() {
                    checkEof()
                    System.out.flush()
                }

                fun checkEof() {
                    if (eof) {
                        throw EOFException()
                    }
                }
            }

            return stdout.use(emit)
        }
    }
}
