package graymatter.sec.common

import graymatter.sec.common.crypto.BinaryEncoding
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintStream
import java.net.URI
import java.net.URL
import java.util.*

fun <T> Boolean.value(truth: T, notTrue: T): T {
    return when (this) {
        true -> truth
        else -> notTrue
    }
}

private fun Sequence<String>.join() = joinToString("")

fun String.trimToLine() = lineSequence().map { it.trim() }.join()
fun String.trimIndentToLine() = trimIndent().lineSequence().join()
fun String.trimMarginToLine(marginPrefix: String = "|") = trimMargin(marginPrefix).lineSequence().join()


fun ByteArray.encodeBinary(encoding: BinaryEncoding = BinaryEncoding.Base64): String = encoding.encode(this)
fun String.decodeBinary(encoding: BinaryEncoding): ByteArray = encoding.decode(this)


fun <T> queueOf(vararg initial: T): Queue<T> {
    return LinkedList<T>().apply { initial.forEach { add(it) } }
}


inline fun <E, T, V> Iterator<E>.collect(dest: T, valueOf: (E) -> T): T where T : MutableCollection<V> {
    while (hasNext()) dest += valueOf(next())
    return dest
}

/**
 * Keeps on consuming from this [Iterator] until [taking] returns `false`
 *
 * @receiver The [Iterator] which supplies the next value to consume
 * @param taking A lambda to check it it should consume the next available element.
 * @param consume A lambda which consumes the next element.
 */

inline fun <E> Iterator<E>.consumeWhile(taking: () -> Boolean, consume: (E) -> Unit) {
    while (hasNext() && taking()) consume(next())
}

/**
 * Keeps on consuming from this sequence until [taking] returns `false`
 *
 * @receiver The sequence to consume from
 * @param taking A lambda to check it it should consume the next available element.
 * @param consume A lambda which consumes the next element.
 */
inline fun <T> Sequence<T>.consumeWhile(taking: () -> Boolean, consume: (T) -> Unit) {
    iterator().consumeWhile(taking, consume)
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

val stderr: PrintStream get() = System.err

fun UUID(): UUID = UUID.randomUUID()
fun UUID(named: String): UUID = UUID.fromString(named)
fun UUID(namedByBytes: ByteArray): UUID = UUID.nameUUIDFromBytes(namedByBytes)
