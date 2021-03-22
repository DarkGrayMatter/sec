package graymatter.sec.command.reuse.validation

import com.google.common.io.BaseEncoding
import graymatter.sec.command.reuse.group.KeyProvider
import graymatter.sec.common.validation.Validator
import java.io.IOException
import java.security.spec.InvalidKeySpecException

fun Validator.validateKeyProvider(
    keyProvider: KeyProvider,
    keyNotSetMessage: () -> String,
    keyNotLoadingMessagePreamble: () -> String,
) {

    fun throwIfNotValidating(e: Exception) {
        val validating = when {
            e is IOException -> true

            e is IllegalArgumentException
                    && e.message?.toLowerCase()?.contains("key must be in the format") == true -> true

            e is IllegalArgumentException
                    && e.message?.toLowerCase()?.contains("Unable to parse") == true -> true

            e is IllegalArgumentException && e.cause is BaseEncoding.DecodingException -> true

            e is RuntimeException && e.cause is InvalidKeySpecException -> true

            else -> false
        }
        if (!validating) throw e
    }

    validate {
        if (!keyProvider.isAvailable) {
            failed(keyNotSetMessage())
        } else try {
            keyProvider.keyWithType
                ?: failed(keyNotLoadingMessagePreamble())
        } catch (e: Exception) {
            throwIfNotValidating(e)
            failed("${keyNotLoadingMessagePreamble()}: ${e.message}", e)
        }
    }
}
