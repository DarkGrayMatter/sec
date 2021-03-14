package graymatter.sec.command

import graymatter.sec.command.reuse.group.InputSourceArgGroup
import graymatter.sec.command.reuse.group.KeyProviderArgGroup
import graymatter.sec.command.reuse.group.OutputTargetArgGroup
import graymatter.sec.command.reuse.mixin.InputFormatOption
import graymatter.sec.command.reuse.mixin.OutputFormatOption
import graymatter.sec.common.cli.validate
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.trimIndentToLine
import graymatter.sec.usecase.DecryptConfigUseCase
import picocli.CommandLine.*
import java.io.IOException

@Command(
    name = "decrypt-config",
    description = ["Decrypts configuration document given an appropriate key."]
)
class DecryptConfig : Runnable {

    @Spec
    lateinit var spec: Model.CommandSpec

    @ArgGroup(
        exclusive = true,
        order = 0,
        heading = "Provide a source document to decrypt using one of the following arguments.%n"
    )
    lateinit var source: InputSourceArgGroup

    @ArgGroup(
        exclusive = true,
        order = 1,
        heading = "Provide use one these arguments to determine where decrypted documents should be written to:%n"
    )
    lateinit var destination: OutputTargetArgGroup

    @ArgGroup(
        exclusive = true,
        order = 2,
        heading = "Use any of the following arguments or options to specify a key for decryption:%n"
    )
    lateinit var keyProvider: KeyProviderArgGroup

    @Mixin
    val inputFormatOverride = InputFormatOption()

    @Mixin
    val outputFormatOverride = OutputFormatOption()

    override fun run() {
        validate()
        DecryptConfigUseCase(
            keyWithType = keyProvider.keyWithType!!,
            source = source::openInputStream,
            sourceFormat = requireNotNull(resolveInputFormat()),
            destination = destination::openOutputStream,
            destinationFormat = requireNotNull(resolveInputFormat())
        ).run()
    }

    private fun validate() {
        validate(spec) {

            val inputIsPresent = requires(this@DecryptConfig::source.isInitialized) {
                "No source document to decrypt was supplied."
            }

            val outputIsPresent = requires(this@DecryptConfig::destination.isInitialized) {
                "No destination provided to output the decrypted document to."
            }

            val encryptionKeyIsPresent = requires(this@DecryptConfig::keyProvider.isInitialized) {
                "No decryption key supplied."
            }

            requires(passed(inputIsPresent) && resolveInputFormat() != null) {
                """
                Unable to determine input configuration format.
                Please provide an input format via the command line. 
                """.trimIndentToLine()
            }

            requires(passed(outputIsPresent) && resolveOutputFormat() != null) {
                """
                Unable to determine output configuration format. Neither an input format
                nor output format override has been specified. 
                """.trimIndentToLine()
            }

            if (passed(encryptionKeyIsPresent)) {
                try {
                    val loadedKey = keyProvider.keyWithType
                    requires(loadedKey != null) { "No encryption key source provided" }
                } catch (e: IOException) {
                    requires(false) { "Error reading encryption key from ${keyProvider.keyUri}: ${e.message}" }
                }
            }

        }
    }

    private fun resolveInputFormat(): DocumentFormat? {
        return inputFormatOverride.value ?: source.uri?.let { DocumentFormat.ofUri(it) }
    }

    private fun resolveOutputFormat(): DocumentFormat? {
        return outputFormatOverride.value ?: resolveInputFormat()
    }

}
