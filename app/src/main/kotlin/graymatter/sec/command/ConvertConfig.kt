package graymatter.sec.command

import graymatter.sec.command.reuse.group.InputTargetProvider
import graymatter.sec.command.reuse.group.OutputTargetProvider
import graymatter.sec.common.cli.SelfValidatingCommand
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.exception.failCommandOn
import graymatter.sec.common.func.right
import graymatter.sec.common.validation.Validator
import graymatter.sec.common.validation.requiresThat
import graymatter.sec.usecase.ConvertConfigUseCase
import picocli.CommandLine.*

@Command(
    name = "convert-config",
    description = [
        "Converts one format of configuration to another.",
        "The following formats are supported:",
        "- Java Properties",
        "- Yaml",
        "- Json"
    ]
)
class ConvertConfig : SelfValidatingCommand() {

    @ArgGroup(
        exclusive = true,
        heading = "Use these options to control where a document should be read from:%n",
        order = 1
    )
    val inputProvider = InputTargetProvider()

    @ArgGroup(
        order = 2,
        exclusive = true,
        heading = "Use any of the following options write the translated document to a configuration document:%n"
    )
    val outputProvider = OutputTargetProvider()

    @Option(
        names = ["--format"],
        description = ["Target configuration format."],
        required = true
    )
    var formatOut: DocumentFormat? = null

    @Option(
        names = ["--source-format"],
        description = [
            "Explicitly set the source format. This is required if the source format cannot be derived from",
            "the name, (such as reading a configuration document from STDIN)",
        ]
    )
    var overrideFormatIn: DocumentFormat? = null

    private var resolvedFormatOut: DocumentFormat? = null
    private var resolvedFormatIn: DocumentFormat? = null

    override fun applyDefaults() {

        resolvedFormatIn = overrideFormatIn
        if (resolvedFormatIn == null) {
            val uri = inputProvider.takeIf { it.isAvailable }?.uri
            if (uri != null) {
                resolvedFormatIn = DocumentFormat.ofUri(uri)
            }
        }

        resolvedFormatOut = formatOut
        if (resolvedFormatOut == null && outputProvider.isFile) {
            val uri = outputProvider.uri
            if (uri != null) {
                resolvedFormatOut = DocumentFormat.ofUri(uri)
            }
        }
    }


    override fun Validator.validateSelf() {
        requiresThat(resolvedFormatIn != null) { "Unable to determine the format of the supplied configuration." }
        requiresThat(resolvedFormatOut != null) { "Please supply the format you want convert to." }
    }

    override fun performAction() {

        val completionState = ConvertConfigUseCase(
            sourceProvider = inputProvider::openInputStream,
            sourceFormat = requireNotNull(resolvedFormatIn),
            targetProvider = outputProvider::openOutputStream,
            targetFormat = requireNotNull(resolvedFormatOut)
        ).call().right

        failCommandOn(
            condition = completionState != ConvertConfigUseCase.CompletionState.Completed,
            exitCode = ExitCode.SOFTWARE,
            message = completionState.message)
    }

}
