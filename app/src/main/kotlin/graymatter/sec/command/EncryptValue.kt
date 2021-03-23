package graymatter.sec.command

import graymatter.sec.command.reuse.group.KeyProvider
import graymatter.sec.command.reuse.group.OutputTargetProvider
import graymatter.sec.command.reuse.validation.validateKeyProvider
import graymatter.sec.common.cli.SelfValidatingCommand
import graymatter.sec.common.validation.Validator
import graymatter.sec.usecase.EncryptValueUseCase
import picocli.CommandLine.*

@Command(
    name = "encrypt",
    description = ["Encrypt value based on the supplied public key."]
)
class EncryptValue : SelfValidatingCommand() {

    @Parameters(index = "0", description = ["A value to encrypt."], arity = "1")
    lateinit var plainText: String

    @ArgGroup(exclusive = true, order = 2, heading = "Supply an appropriate encryption key using:%n")
    val keyProvider = KeyProvider()

    @ArgGroup(
        exclusive = true,
        order = 2,
        heading = "Output of the encrypted value can be send to the following:%n"
    )
    val output = OutputTargetProvider()


    override fun Validator.validateSelf() {
        validateKeyProvider(
            keyProvider,
            keyNotSetMessage = { "Please provide an encryption key." },
            keyNotLoadingMessagePreamble = { "Unable to load encryption key:" }
        )
    }

    override fun performAction() {
        EncryptValueUseCase(
            plainText = plainText,
            secretOut = output::openOutputStream,
            key = keyProvider.keyWithType!!
        ).run()
    }

    override fun applyDefaults() {
        output.setOutputToStdOut()
    }

}
