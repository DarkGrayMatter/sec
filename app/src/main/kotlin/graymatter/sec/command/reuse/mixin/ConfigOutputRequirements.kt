package graymatter.sec.command.reuse.mixin

import graymatter.sec.command.reuse.group.OutputTargetArgGroup
import graymatter.sec.common.document.DocumentFormat
import picocli.CommandLine
import java.io.OutputStream

/**
 * This class models the CLI for processing the output of configuration processing.
 */
class ConfigOutputRequirements {

    @CommandLine.Option(
        names = ["--format-out"],
        required = false,
        description = ["Use a different output format. Normally the output format corresponds to the input format"]
    )
    var outputFormat: DocumentFormat? = null

    @CommandLine.ArgGroup(
        exclusive = true,
        heading = "Use any of the following command line options to supply input for \${COMMAND-NAME}%n"
    )
    lateinit var target: OutputTargetArgGroup

    fun open(): OutputStream = target.output.open()
}
