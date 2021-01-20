@file:Suppress("unused", "ClassName")

package me.andriefc.secj.common

import me.andriefc.secj.common.io.StandardInputInputStream
import me.andriefc.secj.common.io.StandardOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import java.io.File as PlatformFile


/**
 * An abstraction to work with common IO, (such as Files StdIn, StdOut and classpath) sources in an unified way.
 */
@Suppress("MemberVisibilityCanBePrivate")
sealed class IOSource<out T> {

    abstract fun open(): T
    abstract val uri: String

    final override fun toString(): String = uri

    sealed class Input : IOSource<InputStream>() {

        class File(val file: PlatformFile) : Input() {
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
        }

        companion object {
            fun fromString(string: String): Input {
                return when {
                    string == COMMON_STDIO_IDENTIFIER -> StdIn()
                    string.startsWith("classpath:") -> ClassPath(string.substringAfter(":classpath"))
                    else -> File(PlatformFile(string))
                }
            }
        }
    }

    sealed class Output : IOSource<OutputStream>() {

        class File(val file: PlatformFile) : Output() {
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
                    COMMON_STDIO_IDENTIFIER -> StdOut()
                    else -> File(PlatformFile(string))
                }
            }
        }
    }

    companion object {
        private const val COMMON_STDIO_IDENTIFIER = "-"
    }

}


/**
 * An enum to indicate supported formats of documents this tool can process.
 */
enum class StructuredDocumentFormat(vararg validExtensions: String) {

    JSON("json"),
    YAML("yml", "yaml"),
    PROPERTIES("properties");

    private val extensions = validExtensions.map(String::toLowerCase)

    companion object {

        private val formatsWithSuffixes: List<Pair<StructuredDocumentFormat, String>> =
            values().flatMap { format ->
                format.extensions.map { ext ->
                    val suffix = ".${ext}".toLowerCase()
                    format to suffix
                }
            }

        /**
         * Determine yhe document format
         */
        @JvmStatic
        fun ofFile(file: java.io.File): StructuredDocumentFormat? {
            return fromName(file.name)
        }

        /**
         * Determines the format denoted by the last part of a file name after the DOT ("**`.`**") character.
         */
        @JvmStatic
        fun fromName(name: String): StructuredDocumentFormat? {
            return formatsWithSuffixes.firstOrNull { (_, suffix) -> name.endsWith(suffix, ignoreCase = true) }?.first
        }
    }

}


