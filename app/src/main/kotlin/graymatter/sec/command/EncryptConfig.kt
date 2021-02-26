package graymatter.sec.command

import com.palantir.config.crypto.KeyWithType
import graymatter.sec.command.reuse.group.DocumentProcessingPathsArgGroup
import graymatter.sec.command.reuse.group.InputSourceArgGroup
import graymatter.sec.command.reuse.group.KeyProviderArgGroup
import graymatter.sec.command.reuse.group.OutputTargetArgGroup
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.exception.failCommand
import graymatter.sec.usecase.EncryptConfigurationUseCase
import picocli.CommandLine
import picocli.CommandLine.ArgGroup
import picocli.CommandLine.Option

@CommandLine.Command(name = "encrypt-config", description = ["Encrypt a configuration document given a key"])
class EncryptConfig : Runnable {

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
    lateinit var processPathGroup: DocumentProcessingPathsArgGroup

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

    override fun run() {
        val format = resolveInputFormat()
        EncryptConfigurationUseCase(
            openInput = configInput::openInputStream,
            openOutput = configOutput::openOutput,
            inputFormat = format,
            outputFormat = resolveOutputFormat(format),
            keyWithType = resolveKeyWithType(),
            encryptablePaths = processPathGroup.expandPats()
        ).run()
    }

    private fun resolveKeyWithType(): KeyWithType {
        return keyProvider.keyWithType ?: failCommand("No key specified.")
    }

    private fun resolveInputFormat(): DocumentFormat {
        return configInputFormat
            ?: configInput.uri?.let { DocumentFormat.ofName(it) }
            ?: failCommand("Unable to determine inputFormat")
    }

    private fun resolveOutputFormat(inputFormat: DocumentFormat) = configOutputFormat ?: inputFormat

}


