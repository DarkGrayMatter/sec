package graymatter.sec.common.io

import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.File as JavaFile

/**
 * An abstraction to work with common IO, (such as Files StdIn, StdOut and classpath) sources in an unified way.
 */
@Suppress("MemberVisibilityCanBePrivate")
sealed class IOSource<out T> {

    abstract fun open(): T
    abstract val uri: String

    final override fun toString(): String = uri

    sealed class Input : IOSource<InputStream>() {

        val isStdIn: Boolean get() = this is StdIn

        class File(val file: JavaFile) : Input() {
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

    }

    sealed class Output : IOSource<OutputStream>() {

        val isNull: Boolean get() = this is NULL
        val isStdOut: Boolean get() = this is StdOut

        class File(val file: java.io.File) : Output() {
            override val uri: String get() = file.toURI().toString()
            override fun open(): OutputStream = file.outputStream()
        }

        class StdOut : Output() {
            override val uri: String = "stdout://"
            override fun open(): OutputStream = StandardOutputStream()
        }

        object NULL : Output() {
            private object NullOut : OutputStream() {
                override fun write(b: Int) = Unit
                override fun close() = Unit
                override fun toString(): String = "@NULL"
            }
            override fun open(): OutputStream = NullOut
            override val uri: String = NullOut.toString()
        }

    }

    fun tryOpen(): T? {
        return runCatching { open() }
            .onFailure { if (it !is IOException) throw it }
            .getOrNull()
    }


}
