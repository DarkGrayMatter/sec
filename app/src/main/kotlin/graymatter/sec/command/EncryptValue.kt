package graymatter.sec.command

import graymatter.sec.command.reuse.group.InputSourceArgGroup
import graymatter.sec.command.reuse.group.KeyProviderArgGroup
import graymatter.sec.command.reuse.group.OutputTargetArgGroup
import graymatter.sec.common.cli.validate
import graymatter.sec.usecase.EncryptValueUseCase
import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec

@Suppress("unused")
@Command(
    name = "encrypt",
    description = ["Encrypt value based on the supplied public key."]
)
class EncryptValue : Runnable {

    @Spec
    lateinit var spec: CommandSpec

    @Parameters(index = "0", description = ["A value to encrypt."], arity = "1")
    lateinit var plainText: String

    @ArgGroup(
        exclusive = true,
        order = 1,
        heading = "Output of the encrypted value can be send to the following:%n"
    )
    val output = OutputTargetArgGroup()

    @ArgGroup(exclusive = true, order = 2, heading = "Supply an appropriate encryption key using:%n")
    val keyProvider = KeyProviderArgGroup()

    override fun run() {
        applyDefaults()
        validateSpec()
        EncryptValueUseCase(
            plainText = plainText,
            secretOut = output::openOutputStream,
            key = requireNotNull(keyProvider.keyWithType)
        ).run()
    }

    private fun validateSpec() {
        validate(spec) {
            requires(keyProvider.isSupplied) { "Please provide an key to encrypt with" }
        }
    }

    private fun applyDefaults() {
        output.setOutputToStdOut()
    }

}
