@file:Suppress("unused")

package becode.sec.command

import becode.sec.common.exception.failCommand
import becode.sec.common.io.IOSource
import becode.sec.common.parsing.ConfigurationFormat
import becode.sec.common.parsing.readTree
import becode.sec.model.ConfigurationDocument
import picocli.CommandLine.*

@Command(
    name = "process-config",
    description = ["Encryption/Decryption of configuration documents "],
    mixinStandardHelpOptions = true
)
class ProcessConfiguration {

    @Option(
        names = ["-K", "--key"],
        required = true,
        description = ["Appropriate key to encrypt or decrypt a configuration document."]
    )
    lateinit var key: String

    @Parameters(index = "0", paramLabel = "INPUT", description = ["Configuration to process."])
    lateinit var input: IOSource.Input

    @Option(
        names = ["-F", "--format"],
        required = false,
        description = [
            "Explicitly set the format of the configuration INPUT.",
            "Use this if the format cannot be derived from the INPUT (for example STDIN), or ",
            "if you want the name extension does not match the actual type."]
    )
    var inputFormat: ConfigurationFormat? = null

    @Command(name = "encrypt", description = ["Encrypts a configuration file given a key, and a path specification."])
    fun encrypt(
        @ArgGroup(
            heading = "Determine which paths in the document to encrypt%n",
            exclusive = true
        ) encryptionPaths: PathSpecification
    ) {
        val configFormat = inputFormat
            ?: ConfigurationFormat.fromName(input.uri)
            ?: failCommand("Unable to determine the configuration format of $input")

        val configDocument = ConfigurationDocument(input.open().use { it.readTree(configFormat) })
    }

    class PathSpecification {

        lateinit var selection: Set<String>
            private set

        @Option(
            names = ["--encrypted-paths"],
            required = true,
            description = [
                "Represents a external source of paths to encrypt (for example a file).",
                "Each line on the file represents a single path."
            ]
        )
        fun setFromExternalInput(externalInput: IOSource.Input) {

            val selected by lazy {
                externalInput.open()
                    .use { fs -> fs.bufferedReader().lineSequence().map(String::trim).filterNot(String::isEmpty) }
                    .toSet()
            }

            selection = selected
        }

        @Option(
            names = ["-P", "--path"],
            arity = "1..*",
            required = true,
            description = [
                "Represents a path in the source document to encrypt",
                "To encrypt you need at least one path."],
        )
        fun setViaOptions(optionSelected: List<String>) {
            selection = optionSelected.map(String::trim).filterNot(String::isEmpty).toSet()
        }
    }
}
