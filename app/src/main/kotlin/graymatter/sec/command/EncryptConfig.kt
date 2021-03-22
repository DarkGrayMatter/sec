package graymatter.sec.command

import com.palantir.config.crypto.KeyWithType
import graymatter.sec.command.reuse.group.KeyProvider
import graymatter.sec.command.reuse.group.OutputTargetArgGroup
import graymatter.sec.command.reuse.group.ProcessingPathsArgGroup
import graymatter.sec.command.reuse.group.SourceAsInputProvider
import graymatter.sec.command.reuse.mixin.InputFormatOption
import graymatter.sec.command.reuse.mixin.OutputFormatOption
import graymatter.sec.command.reuse.validation.validateKeyProvider
import graymatter.sec.common.cli.SelfValidatingCommand
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.exception.failCommand
import graymatter.sec.common.trimIndentToSentence
import graymatter.sec.common.validation.Validator
import graymatter.sec.common.validation.requiresThat
import graymatter.sec.usecase.EncryptConfigUseCase
import picocli.CommandLine
import picocli.CommandLine.ArgGroup
import picocli.CommandLine.Mixin

@CommandLine.Command(name = "encrypt-config",
    description = ["Encrypt a configuration document given an appropriate key"])
class EncryptConfig : SelfValidatingCommand() {

    @ArgGroup(
        exclusive = true,
        order = 1,
        validate = true,
        heading = "Choose one of the following unencrypted configuration sources:%n"
    )
    val source: SourceAsInputProvider = SourceAsInputProvider()

    @ArgGroup(
        exclusive = true,
        order = 2,
        validate = true,
        heading = "Choose one of the following methods to output the encrypted document to:%n"
    )
    val destination: OutputTargetArgGroup = OutputTargetArgGroup()

    @ArgGroup(
        exclusive = false,
        validate = false,
        heading = "Use any of the following options to choose which paths in the document to encrypt:%n"
    )
    val processPaths: ProcessingPathsArgGroup = ProcessingPathsArgGroup()

    @ArgGroup(
        heading = "Provide encryption key by using one the following options:%n",
        validate = true,
        exclusive = true,
    )
    val keyProvider: KeyProvider = KeyProvider()

    private var actualFormatOut: DocumentFormat? = null
    private var actualFormatIn: DocumentFormat? = null

    @Mixin
    val inputFormatOverride = InputFormatOption()

    @Mixin
    val outputFormatOverride = OutputFormatOption()

    override fun performAction() {
        EncryptConfigUseCase(
            openInput = source::openInputStream,
            openOutput = destination::openOutputStream,
            inputFormat = actualFormatIn!!,
            outputFormat = actualFormatOut!!,
            keyWithType = resolveKeyWithType(),
            encryptedPaths = processPaths.expandPaths()
        ).run()
    }

    override fun applyDefaults() {
        processPaths.takeUnless { it.isAvailable }?.setPaths(emptyList())
        destination.takeUnless { it.isAvailable }?.setOutputToStdOut()
        setActualInputFormat()
        setActualOutputFormat()

    }

    override fun Validator.validateSelf() {

        val sourceValidation = requiresThat(source.isAvailable) {
            "Please supply source document to encrypt"
        }

        val outputValidation = requiresThat(destination.isAvailable) {
            "No destination provided to write encrypted document to."
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
            keyNotSetMessage = { "Please set encryption key." },
            keyNotLoadingMessagePreamble = { "Unable load encryption key from ${keyProvider.keyUri}" }
        )
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

    private fun resolveKeyWithType(): KeyWithType {
        return keyProvider.keyWithType ?: failCommand("No key specified.")
    }

}


