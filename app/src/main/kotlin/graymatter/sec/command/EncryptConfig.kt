package graymatter.sec.command

import com.palantir.config.crypto.KeyWithType
import graymatter.sec.command.reuse.group.ProcessingPathsArgGroup
import graymatter.sec.command.reuse.group.InputSourceArgGroup
import graymatter.sec.command.reuse.group.KeyProviderArgGroup
import graymatter.sec.command.reuse.group.OutputTargetArgGroup
import graymatter.sec.common.cli.validate
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.exception.failCommand
import graymatter.sec.common.validation.ValidationTarget
import graymatter.sec.common.validation.Validator
import graymatter.sec.usecase.EncryptConfigurationUseCase
import picocli.CommandLine
import picocli.CommandLine.ArgGroup
import picocli.CommandLine.Option

@CommandLine.Command(name = "encrypt-config", description = ["Encrypt a configuration document given a key"])
class EncryptConfig : Runnable, ValidationTarget {

    @CommandLine.Spec
    lateinit var spec: CommandLine.Model.CommandSpec

    private var configInputFormat: DocumentFormat? = null
    private var configOutputFormat: DocumentFormat? = null

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
    lateinit var configOutput: OutputTargetArgGroup

    @ArgGroup(
        exclusive = false,
        validate = false,
        heading = "Use any of the following options to choose which paths in the document to encrypt:%n"
    )
    lateinit var processPaths: ProcessingPathsArgGroup

    @ArgGroup(
        heading = "Provide encryption key by using one the following options:%n",
        validate = true,
        exclusive = true,
    )
    lateinit var keyProvider: KeyProviderArgGroup


    @Option(
        names = ["-F", "--format"],
        description = [
            "Set this option if the format cannot be derived from file/resource extension.",
            "NB: This mandatory if you use STDIN as a source."
        ]
    )
    fun setInputFormat(format: DocumentFormat) {
        this.configInputFormat = format
    }

    @Option(
        names = ["--format-out"],
        description = [
            "Override the output format. If not set the same format as the input will be used."
        ]
    )
    fun setOutputFormat(format: DocumentFormat) {
        this.configOutputFormat = format
    }

    override fun Validator.validate() {

        val cmd = this@EncryptConfig

        val keyProviderValidation = requires(cmd::keyProvider.isInitialized) {
            "Please supply an encryption parameter"
        }

        val sourceDocValidation = requires(cmd::configInput.isInitialized) {
            "Source document to encrypt needs to be provided."
        }

        if (passed(sourceDocValidation)) {
            requires(resolveInputFormat() != null) {
                buildString {
                    appendLine("No valid source document format detected: ")
                    when {
                        configInput.uri == null && configInputFormat == null -> {
                            appendLine(
                                "\t- Document source does not specify an regular file (such as STDIN) " +
                                        "to derive the file format from."
                            )
                        }
                        configInput.uri != null && configInputFormat == null -> {
                            appendLine(
                                "\t- Document `${configInput.uri}` does not have any known " +
                                        "standard supported extensions."
                            )
                        }
                    }
                }
            }
        }

        if (passed(keyProviderValidation)) {
            val r = keyProvider.runCatching { keyWithType }
            requires(r.isSuccess && r.getOrNull() != null) {
                buildString {
                    append("Unable to load key from ${keyProvider.keyUri}")
                    val cause = r.exceptionOrNull()
                    if (cause != null) {
                        append(" cause [${cause.javaClass.simpleName}]")
                        cause.message?.also { message ->
                            append(": $message")
                        }
                    }
                }
            }
        }
    }

    override fun run() {
        ensureProcessingPathsAvailability()
        spec.validate(this)
        val format = requireNotNull(resolveInputFormat())
        EncryptConfigurationUseCase(
            openInput = configInput::openInputStream,
            openOutput = configOutput::openOutput,
            inputFormat = format,
            outputFormat = resolveOutputFormat(format),
            keyWithType = resolveKeyWithType(),
            encryptablePaths = processPaths.expandPaths()
        ).run()
    }

    private fun ensureProcessingPathsAvailability() {
        if (!this::processPaths.isInitialized) {
            processPaths = ProcessingPathsArgGroup()
            processPaths.setPaths(emptyList())
        }
    }

    private fun resolveKeyWithType(): KeyWithType {
        return keyProvider.keyWithType ?: failCommand("No key specified.")
    }

    private fun resolveInputFormat(): DocumentFormat? {
        return configInputFormat
            ?: configInput.uri?.let { DocumentFormat.ofName(it) }
    }


    private fun resolveOutputFormat(inputFormat: DocumentFormat) = configOutputFormat ?: inputFormat

}


