package me.andriefc.secj.common.io

import java.io.Closeable
import java.io.File as JavaFile
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

/**
 * An abstraction to work with common IO, (such as Files StdIn, StdOut and classpath) sources in an unified way.
 */
@Suppress("MemberVisibilityCanBePrivate")
sealed class IOSource<out T> {

    abstract fun open(): T
    abstract val uri: String

    final override fun toString(): String = uri

    sealed class Input : IOSource<InputStream>() {

        class File(val file: java.io.File) : Input() {
            override val uri: String get() = file.toURI().toString()
            override fun open(): InputStream = file.inputStream()
        }

        class StdIn : Input() {
            override val uri: String = "stdin://"
            override fun open(): InputStream = StandardInputInputStream()
        }

        class ClassPath(private val resource: String) : Input() {
            override val uri: String = "classpath:$resource"
            override fun open(): InputStream {
                return javaClass.getResourceAsStream(resource)
                    ?: throw FileNotFoundException("No resource found: $uri")
            }
            companion object {
                internal const val PREFIX = ":classpath"
            }
        }

        companion object {
            fun fromString(string: String): Input {
                return when {
                    string == STDIO_IDENTIFIER -> StdIn()
                    string.startsWith(ClassPath.PREFIX) -> ClassPath(string.substringAfter(ClassPath.PREFIX))
                    else -> File(JavaFile(string))
                }
            }
        }
    }

    sealed class Output : IOSource<OutputStream>() {

        class File(val file: java.io.File) : Output() {
            override val uri: String get() = file.toURI().toString()
            override fun open(): OutputStream = file.outputStream()
        }

        class StdOut : Output() {
            override val uri: String = "stdout://"
            override fun open(): OutputStream = StandardOutputStream()
        }

        companion object {
            fun fromString(string: String): Output {
                return when (string) {
                    STDIO_IDENTIFIER -> StdOut()
                    else -> File(JavaFile(string))
                }
            }
        }
    }

    companion object {
        private const val STDIO_IDENTIFIER = "-"
    }
}

interface Resource : Closeable {
    abstract val uri: String
    fun exist(): Boolean
}

interface Readable<R> : Resource where R: Closeable {
    fun input(): R
}

interface Writable<R> : Resource where R:Closeable {
    fun output(): R
}



