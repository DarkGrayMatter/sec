@file:Suppress("unused")

package becode.sec.command

import com.fasterxml.jackson.databind.JsonNode
import com.palantir.config.crypto.KeyWithType
import becode.sec.common.cli.converter.CommaSeparatedListConverter
import becode.sec.common.exception.failCommand
import becode.sec.common.io.IOSource
import becode.sec.common.parsing.StructuredDocumentType.mapper
import becode.sec.common.parsing.Format
import picocli.CommandLine.*

@Command(
    name = "encrypt-config",
    description = ["Encrypts a configuration document. (Supports yaml, JSon, & java properties)"]
)
class EncryptConfigCommand : Runnable {

    private var formatOverride: Format? = null
    private lateinit var configSource: IOSource.Input
    private lateinit var publicKeySource: IOSource.Input

    @ArgGroup(
        exclusive = true,
        multiplicity = "1",
        validate = true
    )
    lateinit var targetSelection: TargetSelection


    @Option(
        names = ["-k"],
        description = ["Public key to encrypt with."],
        required = true,
        paramLabel = "<key>",
        order = 1
    )
    fun setKeySource(s: IOSource.Input) {
        publicKeySource = s
    }

    @Option(
        names = ["-v"],
        description = ["Configuration document to encrypt."],
        required = true,
        paramLabel = "<config>",
        order = 2
    )
    fun setConfigSource(s: IOSource.Input) {
        configSource = s
    }

    @Option(
        names = ["-f", "--format"],
        description = ["Uses the configuration source format if the it cannot be derived from the name (for example STDIN), or if you want to override the implied format."],
        required = false,
        order = 5,
        paramLabel = "<format>"
    )
    fun setConfigSourceFormat(f: Format) {
        formatOverride = f
    }

    override fun run() {

        val format = formatOverride
            ?: Format.fromName(configSource.uri)
            ?: failCommand("Unable to determine configuration format of $configSource")

        val source: JsonNode = configSource.tryOpen()?.use { format.mapper().readTree(it) }
            ?: failCommand("Could not find source configuration document: $configSource")

        val encryptionKey: KeyWithType = publicKeySource.tryOpen()?.use { KeyWithType.fromString(it.reader().readText()) }
            ?: failCommand("Unable to open encryption public key : $publicKeySource")

        val encryptionTargets: List<String> = targetSelection.selected
        val encrypted: JsonNode = encryptWithSelection(source, encryptionKey, encryptionTargets)

        println(encrypted.toPrettyString())
    }

    private fun encryptWithSelection(source: JsonNode, encryptionKey: KeyWithType, selection: List<String>): JsonNode {

        return source
    }


    class TargetSelection {

        private lateinit var resolveSelection: () -> List<String>

        @Option(
            names = ["-T", "--target"],
            description = ["Expression to select encryption keys in the source document."],
            required = true,
            converter = [CommaSeparatedListConverter::class]
        )
        fun setSelection(selection: List<String>) {
            resolveSelection = { selection }
        }

        @Option(
            names = ["--target-file"],
            description = ["Treat each line as selection of keys in the source document"],
            required = true,
            paramLabel = "<inputSource>"
        )
        fun setTargetPerLine(selection: IOSource.Input) {
            resolveSelection = { selection.open().bufferedReader().readLines() }
        }

        val selected get() = resolveSelection()
    }

}

