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
    lateinit var configInput: InputSourceArgGroup

    @ArgGroup(
        exclusive = true,
        order = 2,
        validate = true,
        heading = "Choose one of the following methods to output the encrypted document to:%n"
    )
    val configOutput: OutputTargetArgGroup = OutputTargetArgGroup()

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

    var outputFormat: DocumentFormat? = null
        private set

    @Mixin
    val inputFormatOption = InputFormatOption()

    @Mixin
    val outputFormatOption = OutputFormatOption()

    override fun run() {
        applyDefaults()
        validate()
        val format = requireNotNull(resolveInputFormat())
        EncryptConfigUseCase(
            openInput = configInput::openInputStream,
            openOutput = configOutput::openOutputStream,
            inputFormat = format,
            outputFormat = outputFormat!!,
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

        requires(configInput.isAvailable) {
            "No source document to encrypt was provided."
        }.andThen({ resolveInputFormat() != null }) {
            buildString {
                this.appendLine("No valid source document format detected: ")
                when {
                    configInput.uri == null && inputFormatOption.value == null -> {
                        this.appendLine(
                            "\t- Document source does not specify an regular file (such as STDIN) " +
                                    "to derive the file format from."
                        )
                    }
                    configInput.uri != null && inputFormatOption.value == null -> {
                        this.appendLine(
                            "\t- Document `${this@EncryptConfig.configInput.uri}` does not have any known " +
                                    "standard supported extensions."
                        )
                    }
                }
            }
        }

    }

    private fun applyDefaults() {

        processPaths.takeUnless { it.isAvailable }?.setPaths(emptyList())
        configOutput.takeUnless { it.isAvailable }?.setOutputToStdOut()

        when {

            outputFormatOption.value != null ->
                outputFormat = outputFormatOption.value

            configOutput.uri?.let { DocumentFormat.ofUri(it) } != null ->
                outputFormat = configOutput.uri?.let { DocumentFormat.ofUri(it) }

            resolveInputFormat() != null ->
                outputFormat = resolveInputFormat()
        }

    }

    private fun resolveKeyWithType(): KeyWithType {
        return keyProvider.keyWithType ?: failCommand("No key specified.")
    }

    private fun resolveInputFormat(): DocumentFormat? {
        return inputFormatOption.value
            ?: configInput.uri?.let { DocumentFormat.ofUri(it) }
    }

}


