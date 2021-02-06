@file:Suppress("unused")

package becode.sec.common

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

inline fun requireState(inState: Boolean, errorMessage: () -> String) {
    if (!inState) {
        throw IllegalStateException(errorMessage())
    }
}

inline fun requireState(state: String, inState: () -> Boolean) {
    if (!inState()) {
        throw IllegalStateException("Expected state: $state")
    }
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
