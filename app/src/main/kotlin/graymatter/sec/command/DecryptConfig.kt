package graymatter.sec.command

import graymatter.sec.command.reuse.group.KeyProvider
import graymatter.sec.command.reuse.group.OutputTargetProvider
import graymatter.sec.command.reuse.group.SourceAsInputProvider
import graymatter.sec.command.reuse.mixin.InputFormatOption
import graymatter.sec.command.reuse.mixin.OutputFormatOption
import graymatter.sec.command.reuse.validation.validateKeyProvider
import graymatter.sec.common.cli.SelfValidatingCommand
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.trimIndentToSentence
import graymatter.sec.common.validation.Validator
import graymatter.sec.common.validation.requiresThat
import graymatter.sec.usecase.DecryptConfigUseCase
import picocli.CommandLine.*

@Command(
    name = "decrypt-config",
    description = ["Decrypts configuration document given an appropriate key."]
)
class DecryptConfig : SelfValidatingCommand() {

    private var actualFormatOut: DocumentFormat? = null
    private var actualFormatIn: DocumentFormat? = null

    @ArgGroup(
        exclusive = true,
        order = 0,
        heading = "Provide a source document to decrypt using one of the following arguments.%n"
    )
    lateinit var source: SourceAsInputProvider

    @ArgGroup(
        exclusive = true,
        order = 1,
        heading = "Provide use one these arguments to determine where decrypted documents should be written to:%n"
    )
    val destination: OutputTargetProvider = OutputTargetProvider()

    @ArgGroup(
        exclusive = true,
        order = 2,
        heading = "Use any of the following arguments or options to specify a key for decryption:%n"
    )
    val keyProvider: KeyProvider = KeyProvider()

    @Mixin
    val inputFormatOverride = InputFormatOption()

    @Mixin
    val outputFormatOverride = OutputFormatOption()

    private fun runUseCase() {
        DecryptConfigUseCase(
            keyWithType = keyProvider.keyWithType!!,
            source = source::openInputStream,
            sourceFormat = actualFormatIn!!,
            destination = destination::openOutputStream,
            destinationFormat = actualFormatOut!!
        ).run()
    }

    override fun applyDefaults() {
        setDefaultToStdOutInAbsenceOfUserInput()
        setActualInputFormat()
        setActualOutputFormat()
    }

    private fun setDefaultToStdOutInAbsenceOfUserInput() {
        destination.takeUnless { it.isAvailable }?.also { it.setOutputToStdOut() }
    }

    /**
     * As a User I want the tool to detect the output format based on (in order of precedence):
     * 1. If I explicitly set the `--format-out` value, choose it.
     * 2. If I name the output file with a known/valid extension then choose a format based on the output extension.
     * 3. As a last resort us the input format.
     */
    private fun setActualOutputFormat() {

        if (actualFormatOut != null) {
            return
        }

        actualFormatOut = outputFormatOverride.value

        if (actualFormatOut == null && destination.isAvailable && !destination.isStdOut) {
            actualFormatOut = destination.uri?.let { DocumentFormat.ofUri(it) }
        }

        if (actualFormatOut == null) {
            setActualInputFormat()
            actualFormatOut = actualFormatIn
        }
    }

    private fun setActualInputFormat() {

        if (actualFormatIn != null) {
            return
        }

        if (actualFormatIn == null) {
            actualFormatIn = inputFormatOverride.value
        }

        if (actualFormatIn == null && source.isAvailable && !source.isStdIn) {
            actualFormatIn = source.uri?.let { DocumentFormat.ofUri(it) }
        }

    }

    override fun Validator.validateSelf() {

        val sourceValidation = requiresThat(source.isAvailable) {
            "Please supply source document to decrypt"
        }

        val outputValidation = requiresThat(destination.isAvailable) {
            "No destination provided to write decrypted document to."
        }

        requiresThat(
            sourceValidation,
            outputValidation
        ) {
            requiresThat(actualFormatIn != null) {
                """
                 Unable to determine input configuration format.
                 Please provide an input format via the command line. 
                """.trimIndentToSentence()
            }
            requiresThat(actualFormatOut != null) {
                """
                 Unable to determine output configuration format. Neither an input format
                 nor output format override has been specified. 
                """.trimIndentToSentence()
            }
        }

        validateKeyProvider(keyProvider,
            keyNotSetMessage = { "Please set a decryption key" },
            keyNotLoadingMessagePreamble = { "Unable to load decryption key from ${keyProvider.keyUri}" }
        )
    }


    override fun performAction() {
        DecryptConfigUseCase(
            keyWithType = keyProvider.keyWithType!!,
            source = source::openInputStream,
            sourceFormat = actualFormatIn!!,
            destination = destination::openOutputStream,
            destinationFormat = actualFormatOut!!
        ).run()
    }
}
