package graymatter.sec.command.reuse.mixin

import graymatter.sec.common.document.DocumentFormat
import picocli.CommandLine.Option

class InputFormatMixin {
    @Option(
        names = ["-F", "--format"],
        required = false,
        description = [
            "Optionally override the input format, or set it if the format cannot " +
                    "be derived from the source URI (such as STDIN)."]
    )
    var value: DocumentFormat? = null
}

class OutputFormatMixin {
    @Option(
        names = ["--format-out"],
        required = false,
        description = [
            "Use a different format for output. If none is set, the" +
                    " supplied input format will be used instead."]
    )
    var value: DocumentFormat? = null
}
