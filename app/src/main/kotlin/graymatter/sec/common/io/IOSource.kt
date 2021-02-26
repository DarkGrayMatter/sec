package graymatter.sec.common.io

import java.io.*
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
            fun fromString(inputSpec: String): Input {
                return when {
                    inputSpec == STDIO_IDENTIFIER -> StdIn()
                    inputSpec.startsWith(ClassPath.PREFIX) -> ClassPath(inputSpec.substringAfter(ClassPath.PREFIX))
                    else -> File(JavaFile(inputSpec))
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

        object NULL : Output() {
            object NullOut : OutputStream() {
                override fun write(b: Int) = Unit
                override fun close() = Unit
                override fun toString(): String = "@NULL"
            }
            override fun open(): OutputStream = NullOut
            override val uri: String = NullOut.toString()
        }

        companion object {
            fun fromString(outputSpec: String): Output {
                return when (outputSpec) {
                    STDIO_IDENTIFIER -> StdOut()
                    "NULL" -> NULL
                    else -> File(JavaFile(outputSpec))
                }
            }
        }
    }

    fun tryOpen(): T? {
        return runCatching { open() }
            .onFailure { if (it !is IOException) throw it }
            .getOrNull()
    }

    companion object {
        private const val STDIO_IDENTIFIER = "-"
    }
}
