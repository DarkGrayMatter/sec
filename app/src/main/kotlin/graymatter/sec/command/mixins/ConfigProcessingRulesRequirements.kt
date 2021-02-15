package graymatter.sec.command.mixins

import picocli.CommandLine
import java.io.File
import java.io.FileNotFoundException

class ConfigProcessingRulesRequirements {

    @CommandLine.Option(
        names = ["-P", "--encode-path"],
        required = false,
        split = ";",
        description = [
            "Additional paths to encode. These paths will only be activated the " +
                    "in the absence of rules supplied by the selected processing rule source.",
        ]
    )
    var additionalPaths: List<String> = emptyList()
        internal set

    @CommandLine.ArgGroup(exclusive = true, heading = "Choose one fhe following sources to of encoding rules:%n")
    lateinit var processingRulesSource: ProcessingRulesSource
        internal set

    class ProcessingRulesSource {

        private var ruleSupplier: (() -> List<String>)? = null

        @CommandLine.Option(
            names = ["--encoding-rules-file"],
            description = ["A simple text file which contains one line per matching rule."]
        )
        fun setProcessingRuleFile(file: File) {
            ruleSupplier = { file.readLines() }
        }

        @CommandLine.Option(
            names = ["--encoding-rules-resource"],
            description = ["A simple text file resource which contains one line per matching rule."]
        )
        fun setProcessingRuleResource(resource: String) {
            ruleSupplier = {
                javaClass.getResourceAsStream(resource)?.bufferedReader()?.use { it.readLines() }
                    ?: throw FileNotFoundException("No classpath resource: $resource")
            }
        }

        @CommandLine.Option(
            names = ["--encoding-rules"],
            arity = "1..*",
            description = ["Specified encoding rules from the command line."],
            split = ";"
        )
        fun setProcessingEncodingRules(encodingRules: List<String>) {
            ruleSupplier = { encodingRules }
        }

        fun rules(): List<String> {
            return ruleSupplier?.invoke() ?: emptyList()
        }
    }
}
