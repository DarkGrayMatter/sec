@file:Suppress("unused")

package becode.sec.common

import java.util.*
import org.apache.commons.codec.binary.Base16 as ABase16
import org.apache.commons.codec.binary.Base32 as ABase32
import org.apache.commons.codec.binary.Hex as AHex

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


enum class BinaryEncoding(name: String, vararg alternates: String) {

    Base64("64", "base64") {
        override fun encode(bytes: ByteArray): String {
            return java.util.Base64.getEncoder().encodeToString(bytes)
        }

        override fun decode(byteString: String): ByteArray {
            return java.util.Base64.getDecoder().decode(byteString)
        }
    },
    Base32("32", "base32") {
        override fun encode(bytes: ByteArray): String {
            return ABase32().encodeAsString(bytes)
        }

        override fun decode(byteString: String): ByteArray {
            return ABase32().decode(byteString)
        }
    },
    Hex("hex") {
        override fun encode(bytes: ByteArray): String {
            return AHex.encodeHexString(bytes)
        }

        override fun decode(byteString: String): ByteArray {
            return AHex.decodeHex(byteString)
        }
    },
    Base16("16", "base16") {

        override fun encode(bytes: ByteArray): String {
            return ABase16().encodeAsString(bytes)
        }

        override fun decode(byteString: String): ByteArray {
            return ABase16().decode(byteString.toLowerCase())
        }
    },
    ;


    abstract fun encode(bytes: ByteArray): String
    abstract fun decode(byteString: String): ByteArray
    private val names = setOf(name) + alternates

    companion object {

        fun fromName(namedEncoding: String): BinaryEncoding {
            return values().first { encoding -> namedEncoding in encoding.names }
        }
    }
}


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





