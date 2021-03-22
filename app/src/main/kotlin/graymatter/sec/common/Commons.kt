package graymatter.sec.common

import graymatter.sec.common.crypto.BinaryEncoding
import java.io.File
import java.io.FileNotFoundException
import java.net.URI
import java.net.URL
import java.util.*

fun <T> Boolean.value(truth: T, notTrue: T): T {
    return when (this) {
        true -> truth
        else -> notTrue
    }
}

fun Sequence<String>.joinAsSentence() = joinToString(" ", transform = {it.trimEnd()}).trim()

fun String.trimIndentToSentence() = trimIndent().lineSequence().joinAsSentence()
fun String.trimMarginToSentence(marginPrefix: String = "|") = trimMargin(marginPrefix).lineSequence().joinAsSentence()

fun ByteArray.encodeBinary(encoding: BinaryEncoding = BinaryEncoding.Base64): String = encoding.encode(this)


fun <T> queueOf(vararg initial: T): Queue<T> {
    return LinkedList<T>().apply { initial.forEach { add(it) } }
}


inline fun <E, T, V> Iterator<E>.collect(dest: T, valueOf: (E) -> T): T where T : MutableCollection<V> {
    while (hasNext()) dest += valueOf(next())
    return dest
}

class ClassPathResourceNotFoundException(resourcePath: String) : FileNotFoundException(resourcePath)
class UndefinedLocalFileUriException(uri: URI) : FileNotFoundException("Undefined uri: $uri")

inline fun <reified T> resourceAt(resourcePath: String): URL {
    return T::class.java.getResource(resourcePath) ?: throw ClassPathResourceNotFoundException(resourcePath)
}

inline fun <reified T> resourceFile(resourcePath: String): File = resourceAt<T>(resourcePath).file()

fun Any.resourceFile(path: String): File {
    return javaClass.getResource(path)?.file() ?: throw ClassPathResourceNotFoundException(path)
}

fun URL.file(): File {
    return toURI().let { uri ->
        when (val file = uri?.let(::File)?.canonicalFile) {
            null -> throw UndefinedLocalFileUriException(uri)
            else -> file
        }
    }
}

fun UUID(): UUID = UUID.randomUUID()

inline fun <reified X:Exception> Result<*>.onFailureOf(handleException:(X) -> Unit) {
    onFailure {
        if (it is X) {
            handleException(it)
        }
    }
}
