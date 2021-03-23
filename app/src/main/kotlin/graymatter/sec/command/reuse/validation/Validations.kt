package graymatter.sec.command.reuse.validation

import com.google.common.io.BaseEncoding.DecodingException
import graymatter.sec.command.reuse.group.KeyProvider
import graymatter.sec.common.validation.Validator
import java.io.IOException
import java.security.spec.InvalidKeySpecException

private fun Exception.hasError(error: String): Boolean {
    return when (val message = this.message?.toLowerCase()) {
        null -> false
        else -> error in message
    }
}

private inline fun <reified X:Throwable> Throwable.causedBy(): Boolean {
    return when (cause) {
        null -> false
        else -> cause is X
    }
}

fun Validator.validateKeyProvider(
    keyProvider: KeyProvider,
    keyNotSetMessage: () -> String,
    keyNotLoadingMessagePreamble: () -> String,
) {

    fun throwIfNotValidating(e: Exception) {
        val validating = when {
            e is IOException -> true
            e is IllegalArgumentException && e.hasError("key must be in the format") -> true
            e is IllegalArgumentException && e.hasError("Unable to parse") -> true
            e is IllegalArgumentException && e.causedBy<DecodingException>()-> true
            e is RuntimeException && e.causedBy<InvalidKeySpecException>() -> true
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
