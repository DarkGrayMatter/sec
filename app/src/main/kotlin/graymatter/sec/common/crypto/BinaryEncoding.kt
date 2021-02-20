package graymatter.sec.common.crypto

import org.apache.commons.codec.binary.Base16
import org.apache.commons.codec.binary.Base32
import org.apache.commons.codec.binary.Hex
import java.util.*

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
            return Base32().encodeAsString(bytes)
        }

        override fun decode(byteString: String): ByteArray {
            return Base32().decode(byteString)
        }
    },
    Hex("hex") {
        override fun encode(bytes: ByteArray): String {
            return org.apache.commons.codec.binary.Hex.encodeHexString(bytes)
        }

        override fun decode(byteString: String): ByteArray {
            return org.apache.commons.codec.binary.Hex.decodeHex(byteString)
        }
    },
    Base16("16", "base16") {

        override fun encode(bytes: ByteArray): String {
            return Base16().encodeAsString(bytes)
        }

        override fun decode(byteString: String): ByteArray {
            return Base16().decode(byteString.toLowerCase())
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
