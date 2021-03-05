package graymatter.sec.common.crypto

import com.palantir.config.crypto.EncryptedValue
import com.palantir.config.crypto.Key
import com.palantir.config.crypto.KeyWithType
import com.palantir.config.crypto.algorithm.KeyType

operator fun KeyWithType.component1(): Key = key
operator fun KeyWithType.component2(): KeyType = type

object CryptoConstants {
    const val PREFIX_ID = "\$"
    const val ENCRYPTED_VALUE_OPEN_TAG = '{'
    const val ENCRYPTED_VALUE_PREFIX = "$PREFIX_ID{enc:"
    const val ENCRYPTED_VALUE_CLOSE_TAG = "}"
    const val EMPTY_CRYPTO_TEXT = "$ENCRYPTED_VALUE_PREFIX$ENCRYPTED_VALUE_CLOSE_TAG"
}

/**
 * Tries to extract encrypted string content from an existing string.
 * A result value of `null` indicates that the string does not contain
 * any encrypted text, e.g it either either empty, null, or blank.
 *
 * @return The extract encrypted text, or `null` if it could not be found.
 */
fun String?.tryExtractEncryptedContent(): String? {
    return when {
        this == null -> null
        this == CryptoConstants.EMPTY_CRYPTO_TEXT -> null
        startsWith(CryptoConstants.ENCRYPTED_VALUE_PREFIX)
                && endsWith(CryptoConstants.ENCRYPTED_VALUE_CLOSE_TAG) -> substring(
            CryptoConstants.ENCRYPTED_VALUE_PREFIX.length,
            length - CryptoConstants.ENCRYPTED_VALUE_CLOSE_TAG.length
        )
        else -> null
    }
}

fun EncryptedValue.encodeToString() = buildString {
    append(CryptoConstants.PREFIX_ID)
    append(CryptoConstants.ENCRYPTED_VALUE_OPEN_TAG)
    append(this@encodeToString.toString())
    append(CryptoConstants.ENCRYPTED_VALUE_CLOSE_TAG)
}


