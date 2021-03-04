package graymatter.sec.command

import com.palantir.config.crypto.KeyWithType
import graymatter.sec.command.reuse.group.ProcessingPathsArgGroup
import graymatter.sec.command.reuse.group.InputSourceArgGroup
import graymatter.sec.command.reuse.group.KeyProviderArgGroup
import graymatter.sec.command.reuse.group.OutputTargetArgGroup
import graymatter.sec.command.reuse.mixin.InputFormatMixin
import graymatter.sec.command.reuse.mixin.OutputFormatMixin
import graymatter.sec.common.cli.validate
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.exception.failCommand
import graymatter.sec.common.validation.ValidationTarget
import graymatter.sec.common.validation.ValidationContext
import graymatter.sec.usecase.EncryptConfigurationUseCase
import picocli.CommandLine
import picocli.CommandLine.*

@CommandLine.Command(name = "encrypt-config", description = ["Encrypt a configuration document given a key"])
class EncryptConfig : Runnable, ValidationTarget {

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


    @Mixin
    val inputFormatMixin = InputFormatMixin()

    @Mixin
    val outputFormatMixin = OutputFormatMixin()

    override fun validate(validation: ValidationContext) {

        val cmd = this

        val keyProviderValidation = validation.requires(cmd::keyProvider.isInitialized) {
            "Please supply an encryption parameter"
        }

        val sourceDocValidation = validation.requires(cmd::configInput.isInitialized) {
            "Source document to encrypt needs to be provided."
        }

        if (validation.passed(sourceDocValidation)) {
            validation.requires(this.resolveInputFormat() != null) {
                buildString {
                    this.appendLine("No valid source document format detected: ")
                    when {
                        this@EncryptConfig.configInput.uri == null && this@EncryptConfig.inputFormatMixin.value == null -> {
                            this.appendLine(
                                "\t- Document source does not specify an regular file (such as STDIN) " +
                                        "to derive the file format from."
                            )
                        }
                        this@EncryptConfig.configInput.uri != null && this@EncryptConfig.inputFormatMixin.value == null -> {
                            this.appendLine(
                                "\t- Document `${this@EncryptConfig.configInput.uri}` does not have any known " +
                                        "standard supported extensions."
                            )
                        }
                    }
                }
            }
        }

        if (validation.passed(keyProviderValidation)) {
            val r = keyProvider.runCatching { keyWithType }
            validation.requires(r.isSuccess && r.getOrNull() != null) {
                buildString {
                    this.append("Unable to load key from ${this@EncryptConfig.keyProvider.keyUri}")
                    val cause = r.exceptionOrNull()
                    if (cause != null) {
                        this.append(" cause [${cause.javaClass.simpleName}]")
                        cause.message?.also { message ->
                            this.append(": $message")
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
            openOutput = configOutput::openOutputStream,
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
        return inputFormatMixin.value
            ?: configInput.uri?.let { DocumentFormat.ofUri(it) }
    }

    private fun resolveOutputFormat(inputFormat: DocumentFormat): DocumentFormat = outputFormatMixin.value ?: inputFormat

}


