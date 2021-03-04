package graymatter.sec.common.crypto

import com.palantir.config.crypto.Key
import com.palantir.config.crypto.KeyWithType
import com.palantir.config.crypto.algorithm.KeyType


operator fun KeyWithType.component1(): Key = key
operator fun KeyWithType.component2(): KeyType = type


object CryptoConstants {
    const val ENCRYPTED_VALUE_PREFIX = "\${enc:"
    const val ENCRYPTED_VALUE_SUFFIX = "}"
}


/**
 * Attempts to extract encrypted string content from an existing string.
 * A result value of `null` indicates that the string does not contain
 * any encrypted text, e.g it either either empty, null, or blank.
 */
fun String?.extractEncryptedContent(): String? {
    TODO()
}
