package graymatter.sec.command.reuse.mixin

import graymatter.sec.command.reuse.group.InputSourceArgGroup
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.trimToLine
import picocli.CommandLine
import java.io.InputStream

/**
 * Describes the CLI for configuration sources to process.
 */
class ConfigSourceRequirements {

    @CommandLine.Option(
        names = ["-F", "--format", "--format-in"],
        required = false,
        description = [
            "Overrides, or sets the format of the configuration document.",
            "Use this option if you want to override the document format, or if",
            "you the preferred input source (such as STDIN) does not contain enough",
            "information to derive the document type."
        ]
    )
    var overriddenInputFormat: DocumentFormat? = null

    @CommandLine.ArgGroup(
        exclusive = true,
        heading = "Use any of the following options to supply inout for \${COMMAND-NAME}%n"
    )
    lateinit var input: InputSourceArgGroup

    val requestedFormat: DocumentFormat?
        get() = overriddenInputFormat ?: DocumentFormat.ofName(input.source.uri)

    fun open(): InputStream = input.source.open()

    override fun toString(): String {
        return """
            ConfigurationDocumentInput {
              "format": ${requestedFormat},
              "input": $input
            }
        """.trimToLine()
    }
}
