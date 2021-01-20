@file:Suppress("unused")

package me.andriefc.secj.comand

import com.fasterxml.jackson.databind.JsonNode
import com.palantir.config.crypto.KeyWithType
import me.andriefc.secj.common.io.IOSource
import me.andriefc.secj.common.parsing.Mappers.mapper
import me.andriefc.secj.common.parsing.StructuredDocumentFormat
import picocli.CommandLine.*

@Command(
    name = "encrypt-config",
    description = ["Encrypts a configuration document. (Supports yaml, JSon, & java properties)"]
)
class EncryptConfigCommand : Runnable {

    private var formatOverride: StructuredDocumentFormat? = null
    private lateinit var configSource: IOSource.Input
    private lateinit var publicKeySource: IOSource.Input

    @ArgGroup(
        exclusive = true,
        multiplicity = "1",
        validate = true
    )
    lateinit var targetSelection: TargetSelection


    @Option(names = ["-k"], description = ["Public key to encrypt with."], required = true, paramLabel = "<key>", order = 1)
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
    fun setConfigSourceFormat(f: StructuredDocumentFormat) {
        formatOverride = f
    }


    override fun run() {

        val format = formatOverride
            ?: StructuredDocumentFormat.fromName(configSource.uri)
            ?: throw IllegalArgumentException("Unable to determine configuration format of ${configSource.uri}")

        val source: JsonNode = configSource.open().use { format.mapper().readTree(it) }
        val encryptionKey: KeyWithType = publicKeySource.open().use { KeyWithType.fromString(it.reader().readText()) }
        val encryptionTargets: List<String> = targetSelection.selection()
        val encrypted: JsonNode = encryptWithSelection(source, encryptionKey, encryptionTargets)

        println(encrypted.toPrettyString())
    }

    private fun encryptWithSelection(source: JsonNode, encryptionKey: KeyWithType, selection: List<String>): JsonNode {


        return source
    }


    class TargetSelection {

        private lateinit var retrieveSelection: () -> List<String>

        @Option(
            names = ["-t"],
            description = ["Expression to select encryption keys in the source document."],
            required = true
        )
        fun setSelection(selection: List<String>) {
            retrieveSelection = { selection }
        }

        @Option(
            names = ["--target-per-line"],
            description = ["Treat each line as selection of keys in the source document"],
            required = true,
            paramLabel = "<inputSource>"
        )
        fun setTargetPerLine(selection: IOSource.Input) {
            retrieveSelection = { selection.open().bufferedReader().readLines() }
        }

        fun selection() = retrieveSelection()
    }


}

