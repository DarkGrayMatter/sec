@file:Suppress("unused")

package becode.sec.common

import java.security.SecureRandom
import kotlin.random.Random
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


enum class BinaryEncoding {

    Base64 {
        override fun encode(bytes: ByteArray): String {
            return java.util.Base64.getEncoder().encodeToString(bytes)
        }

        override fun decode(byteString: String): ByteArray {
            return java.util.Base64.getDecoder().decode(byteString)
        }
    },
    Base32 {
        override fun encode(bytes: ByteArray): String {
            return ABase32().encodeAsString(bytes)
        }

        override fun decode(byteString: String): ByteArray {
            return ABase32().decode(byteString)
        }
    },
    Hex {
        override fun encode(bytes: ByteArray): String {
            return AHex.encodeHexString(bytes)
        }

        override fun decode(byteString: String): ByteArray {
            return AHex.decodeHex(byteString)
        }
    },
    Base16 {
        override fun encode(bytes: ByteArray): String {
            return ABase16().encodeAsString(bytes)
        }

        override fun decode(byteString: String): ByteArray {
            return ABase16().decode(byteString)
        }
    };

    abstract fun encode(bytes: ByteArray): String
    abstract fun decode(byteString: String): ByteArray

    companion object {

        private val encodingNames: Map<String, BinaryEncoding> = listOf(
            Base16 to listOf("base16", "16"),
            Base32 to listOf("base32", "32"),
            Base64 to listOf("base64", "64"),
            Hex to listOf("hex")
        ).flatMap { (enc, opts) -> opts.map { opt -> opt to enc } }.toMap()

        fun fromName(namedEncoding: String): BinaryEncoding {
            return encodingNames[namedEncoding.toLowerCase().trim()]
                ?: throw IllegalArgumentException(buildString {
                    append("Unsupported encoding name: ").append(namedEncoding)
                    append(". The following encodings are available: ")
                    encodingNames.keys.joinTo(this, prefix = "[", postfix = "]")
                })
        }
    }
}

sealed class RandomBytesGenerator {

    abstract operator fun invoke(bytes: ByteArray)

    object Unsafe : RandomBytesGenerator() {
        override fun invoke(bytes: ByteArray) {
            Random.nextBytes(bytes)
        }
    }

    class DefaultSecure(private val strong: Boolean) : RandomBytesGenerator() {
        override fun invoke(bytes: ByteArray) {
            val rnd = if (strong) SecureRandom.getInstanceStrong() else SecureRandom()
            return rnd.nextBytes(bytes)
        }
    }

}
