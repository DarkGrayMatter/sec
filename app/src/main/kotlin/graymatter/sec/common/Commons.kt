@file:Suppress("unused")

package graymatter.sec.common

import graymatter.sec.common.crypto.BinaryEncoding
import java.util.*

fun String.tr(): String {
    return when {
        isEmpty() -> this
        else -> lineSequence()
            .map { it.trim() }
            .filterNot { it.isEmpty() }
            .let { parts -> buildString { parts.joinTo(this, separator = " ") } }
    }
}

inline fun requiresStateOf(inState: Boolean, errorMessage: () -> String) {
    if (!inState) {
        throw IllegalStateException(errorMessage())
    }
}

inline fun requiresStateOf(stateLabel: String, inState: () -> Boolean) {
    if (!inState()) {
        throw IllegalStateException("Expected state: $stateLabel")
    }
}

inline fun <T> T.requiresStateOf(stateLabel: String, inState: (T) -> Boolean): T {
    if (!inState(this)) {
        throw IllegalStateException("Expected state: $stateLabel")
    }
    return this
}

inline fun <T> T?.requireNonNullStateOf(stateLabel: String, inState: (T) -> Boolean): T {

    if (this == null || !inState(this)) {
        throw IllegalStateException("Expected state: $stateLabel")
    }

    return this
}

enum class Separator(val char: Char) {
    Comma(','),
    Dot('.'),
    Slash('/'),
    DosSlash('\\'),
    Pipe('|'),
    Tab('\t'),
    SemiColon(';')
}


fun ByteArray.encodeBinary(encoding: BinaryEncoding): String = encoding.encode(this)
fun String.decodeBinary(encoding: BinaryEncoding): ByteArray = encoding.decode(this)


fun <T> queueOf(vararg initial: T): Queue<T> {
    return LinkedList<T>().apply { initial.forEach { add(it) } }
}

fun <T> linkedListOf(vararg initial: T): LinkedList<T> {
    return LinkedList<T>().also { it.addAll(initial) }
}


fun <E, T> Iterator<E>.collect(dest: T): T where  T : MutableCollection<E> {
    while (hasNext()) dest += next()
    return dest
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


inline fun <T> memoize(crossinline get: () -> T): () -> T {
    var holder: Holder<T>? = null
    return {
        when (val v = holder?.value) {
            null -> get().also { holder = Holder(it) }
            else -> v
        }
    }
}

data class
Holder<out T>(val value: T)





