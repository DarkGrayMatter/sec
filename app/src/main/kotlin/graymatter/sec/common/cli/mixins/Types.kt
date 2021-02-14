@file:Suppress("unused")

package graymatter.sec.common.cli.mixins

import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.io.IOSource.Input
import graymatter.sec.common.io.IOSource.Output
import picocli.CommandLine.*
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

class ConfigSourceRequirements {

    @Option(
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

    @Parameters(
        index = "0",
        arity = "1",
        paramLabel = "CONFIG_SOURCE",
        description = [
            "Input source of the document. Could be a file, a resource on the classpath, or even STDIN.",
            "NOTE: If using STDIN, make sure you are setting the format via the \"-F\", or \"--format\" switch.",
        ]
    )
    lateinit var inputDocument: Input
        internal set

}

class ConfigOutputRequirements {

    @Option(
        names = ["--format-out"],
        required = false,
        description = ["Use a different output format. Normally the output format corresponds to the input format"]
    )
    var outputFormat: DocumentFormat? = null

    @Parameters(paramLabel = "OUTPUT", arity = "1", description = ["Output of processed configuration file."])
    lateinit var output: Output

}

class KeyRequirements {

    @ArgGroup(exclusive = true, heading = "Key used during encryption (use any one of these:)%n")
    lateinit var provider: InputProvider
        internal set

    class InputProvider {

        private lateinit var openKeyFile: () -> InputStream

        @Option(names = ["--key-file"], description = ["File containing encryption key"])
        fun setKeyFile(keyFile: File) {
            openKeyFile = keyFile::inputStream
        }

        @Option(names = ["-k", "--key"], description = ["Key value on command line."])
        fun setKeyFromCommandLine(keyFromCommandLine: String) {
            openKeyFile = { keyFromCommandLine.byteInputStream() }
        }

        @Option(names = ["--key-resource"], description = ["Key file as a resource on the classpath."])
        fun setKeyFromClassPath(keyFromClassPath: String) {
            openKeyFile = {
                javaClass.getResourceAsStream(keyFromClassPath)
                    ?: throw FileNotFoundException(
                        "Could not find key file on classpath: $keyFromClassPath"
                    )
            }
        }

        fun readKeyFile(): InputStream = openKeyFile()
    }

}

class ConfigProcessingRulesRequirements {

    @Option(
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

    @ArgGroup(exclusive = true, heading = "Choose one fhe following sources to of encoding rules:%n")
    lateinit var processingRulesSource: ProcessingRulesSource
        internal set

    class ProcessingRulesSource {

        private var ruleSupplier: (() -> List<String>)? = null

        @Option(
            names = ["--encoding-rules-file"],
            description = ["A simple text file which contains one line per matching rule."]
        )
        fun setProcessingRuleFile(file: File) {
            ruleSupplier = { file.readLines() }
        }

        @Option(
            names = ["--encoding-rules-resource"],
            description = ["A simple text file resource which contains one line per matching rule."]
        )
        fun setProcessingRuleResource(resource: String) {
            ruleSupplier = {
                javaClass.getResourceAsStream(resource)?.bufferedReader()?.use { it.readLines() }
                    ?: throw FileNotFoundException("No classpath resource: $resource")
            }
        }

        @Option(
            names = ["--encoding-rule"],
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
