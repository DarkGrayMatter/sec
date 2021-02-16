package graymatter.sec.command.parts

import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.io.IOSource
import picocli.CommandLine

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
    var inputFormat: DocumentFormat? = null
        internal set

    @CommandLine.Parameters(
        index = "0",
        arity = "1",
        paramLabel = "CONFIG_SOURCE",
        description = [
            "Input source of the document. Could be a file, a resource on the classpath, or even STDIN.",
            "NOTE: If using STDIN, make sure you are setting the format via the \"-F\", or \"--format\" switch.",
        ]
    )
    lateinit var inputDocument: IOSource.Input
        internal set

}
