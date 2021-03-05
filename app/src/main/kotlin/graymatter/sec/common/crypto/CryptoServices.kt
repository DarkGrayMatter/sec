package graymatter.sec.common.crypto

import com.palantir.config.crypto.Key
import com.palantir.config.crypto.KeyWithType
import com.palantir.config.crypto.algorithm.KeyType

operator fun KeyWithType.component1(): Key = key
operator fun KeyWithType.component2(): KeyType = type

private const val PREFIX_ID = "\$"
private const val ENCRYPTED_VALUE_OPEN_TAG = '{'
private const val ENCRYPTED_VALUE_PREFIX = "$PREFIX_ID{enc:"
private const val ENCRYPTED_VALUE_CLOSE_TAG = "}"

fun String?.tryExtractEncryptedContent(): String? {
    return when {
        this == null -> null
        endsWith(ENCRYPTED_VALUE_CLOSE_TAG) && startsWith(ENCRYPTED_VALUE_PREFIX) ->
            substring(
                ENCRYPTED_VALUE_PREFIX.length,
                length - ENCRYPTED_VALUE_CLOSE_TAG.length
            ).takeUnless { it.isBlank() || it.isEmpty() }
        else -> null
    }
}

fun String.wrapAsEncryptedContent(): String {
    return buildString {
        append(PREFIX_ID)
        append(ENCRYPTED_VALUE_OPEN_TAG)
        append(this@wrapAsEncryptedContent)
        append(ENCRYPTED_VALUE_CLOSE_TAG)
    }
}

