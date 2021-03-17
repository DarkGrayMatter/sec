package graymatter.sec.command

import com.palantir.config.crypto.KeyWithType
import graymatter.sec.command.reuse.group.ProcessingPathsArgGroup
import graymatter.sec.command.reuse.group.InputSourceArgGroup
import graymatter.sec.command.reuse.group.KeyProviderArgGroup
import graymatter.sec.command.reuse.group.OutputTargetArgGroup
import graymatter.sec.command.reuse.mixin.InputFormatOption
import graymatter.sec.command.reuse.mixin.OutputFormatOption
import graymatter.sec.common.cli.validate
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.exception.failCommand
import graymatter.sec.usecase.EncryptConfigUseCase
import picocli.CommandLine
import picocli.CommandLine.*

@CommandLine.Command(name = "encrypt-config",
    description = ["Encrypt a configuration document given an appropriate key"])
class EncryptConfig : Runnable {

    @CommandLine.Spec
    lateinit var spec: Model.CommandSpec

    @ArgGroup(
        exclusive = true,
        order = 1,
        validate = true,
        heading = "Choose one of the following unencrypted configuration sources:%n"
    )
    val source: InputSourceArgGroup = InputSourceArgGroup()

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
    val keyProvider: KeyProviderArgGroup = KeyProviderArgGroup()

    private var actualFormatOut: DocumentFormat? = null
    private var actualFormatIn: DocumentFormat? = null

    @Mixin
    val inputFormatOverride = InputFormatOption()

    @Mixin
    val outputFormatOverride = OutputFormatOption()

    override fun run() {
        applyDefaults()
        validate()
        runUseCase()
    }

    private fun runUseCase() {
        EncryptConfigUseCase(
            openInput = source::openInputStream,
            openOutput = destination::openOutputStream,
            inputFormat = actualFormatIn!!,
            outputFormat = actualFormatOut!!,
            keyWithType = resolveKeyWithType(),
            encryptedPaths = processPaths.expandPaths()
        ).run()
    }

    private fun validate() = validate(spec) {

        requires(keyProvider.isAvailable) {
            "Please supply and key to encrypt the configuration with."
        }.andThenWith({keyProvider.runCatching { keyWithType }}) { r ->
            requires(r.isSuccess && r.getOrThrow() != null) {
                buildString {
                    append("Unable to load key from ${keyProvider.keyUri}")
                    r.exceptionOrNull()?.also { cause ->
                        append(" cause [${cause.javaClass.simpleName}]")
                        cause.message?.also { message -> append(": $message") }
                    }
                }
            }
        }

        requires(source.isAvailable) {
            "No source document to encrypt was provided."
        }.andThen({ resolveInputFormat() != null }) {
            buildString {
                this.appendLine("No valid source document format detected: ")
                when {
                    source.uri == null && inputFormatOverride.value == null -> {
                        this.appendLine(
                            "\t- Document source does not specify an regular file (such as STDIN) " +
                                    "to derive the file format from."
                        )
                    }
                    source.uri != null && inputFormatOverride.value == null -> {
                        this.appendLine(
                            "\t- Document `${this@EncryptConfig.source.uri}` does not have any known " +
                                    "standard supported extensions."
                        )
                    }
                }
            }
        }

    }

    private fun applyDefaults() {

        processPaths.takeUnless { it.isAvailable }?.setPaths(emptyList())
        destination.takeUnless { it.isAvailable }?.setOutputToStdOut()

        setActualInputFormat()
        setActualOutputFormat()

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

    private fun resolveInputFormat(): DocumentFormat? {
        return inputFormatOverride.value
            ?: source.uri?.let { DocumentFormat.ofUri(it) }
    }

}


