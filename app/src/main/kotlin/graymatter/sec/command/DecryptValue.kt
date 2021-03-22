package graymatter.sec.command

import com.palantir.config.crypto.EncryptedValue
import graymatter.sec.command.reuse.group.KeyProvider
import graymatter.sec.command.reuse.group.OutputTargetProvider
import graymatter.sec.command.reuse.validation.validateKeyProvider
import graymatter.sec.common.cli.SelfValidatingCommand
import graymatter.sec.common.validation.Validator
import graymatter.sec.common.validation.requiresThat
import picocli.CommandLine.*


@Command(
    name = "decrypt",
    description = ["Decrypts a value given a private key."]
)
@Suppress("unused")
class DecryptValue : SelfValidatingCommand() {

    @Parameters
    var secretText: String? = null

    @ArgGroup
    val output: OutputTargetProvider = OutputTargetProvider()

    @ArgGroup
    val keyProvider = KeyProvider()

    override fun Validator.validateSelf() {

        validateKeyProvider(
            keyProvider,
            keyNotSetMessage = { "Please provide an appropriate key to decrypt with." },
            keyNotLoadingMessagePreamble = { "Failure to load decryption key:" },
        )
     
        requiresThat(secretText != null) {
            "Nothing to do: Please provide secret text."
        }
    }

    override fun performAction() {
        val encrypted = secretText?.let(EncryptedValue::fromString) ?: return
        val kt = keyProvider.keyWithType!!
        val decrypted = encrypted.decrypt(kt)
        output.openOutputStream().bufferedWriter().use { it.appendLine(decrypted) }
    }

    override fun applyDefaults() {
        output.setOutputToStdOut()
    }

}
