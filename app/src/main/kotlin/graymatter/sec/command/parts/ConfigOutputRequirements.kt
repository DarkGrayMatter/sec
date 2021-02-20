package graymatter.sec.command.parts

import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.io.IOSource
import picocli.CommandLine

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

    @CommandLine.Parameters(paramLabel = "OUTPUT", arity = "1", description = ["Output of processed configuration file."])
    lateinit var output: IOSource.Output

}
