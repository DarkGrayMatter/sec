package graymatter.sec.command

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.databind.node.ObjectNode
import com.palantir.config.crypto.KeyWithType
import graymatter.sec.common.cli.reuse.mixin.ConfigOutputRequirements
import graymatter.sec.common.cli.reuse.mixin.ConfigProcessingRulesRequirements
import graymatter.sec.common.cli.reuse.mixin.ConfigSourceRequirements
import graymatter.sec.common.cli.reuse.mixin.KeyRequirements
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.document.ObjectMappers
import graymatter.sec.common.document.readTree
import graymatter.sec.common.document.visitNodePathsOf
import graymatter.sec.common.exception.failCommand
import graymatter.sec.common.exception.failedWithHelp
import io.github.azagniotov.matcher.AntPathMatcher
import picocli.CommandLine
import picocli.CommandLine.Model.CommandSpec
import java.io.OutputStream

@CommandLine.Command(name = "encrypt-config", description = ["Encrypt a configuration document given a key"])
class EncryptConfig : Runnable {

    @CommandLine.Spec
    lateinit var spec: CommandSpec

    @CommandLine.Mixin
    lateinit var input: ConfigSourceRequirements

    @CommandLine.Mixin
    lateinit var encryptionKey: KeyRequirements

    @CommandLine.Mixin
    lateinit var encryptionRules: ConfigProcessingRulesRequirements

    @CommandLine.Mixin
    lateinit var output: ConfigOutputRequirements

    override fun run() {

        validate()

        val doc = input.requestedFormat
            ?.let { format -> input.open().use { it.readTree<ObjectNode>(format) } }
            ?: failCommand("Failed to open :${input}")

        val keyWithType = encryptionKey.keyWithType()
        val encryptedDoc = encrypt(doc, keyWithType, encryptionRules.rules)
        val encryptedDocFormat = requireNotNull(output.outputFormat ?: input.overriddenInputFormat)

        output.open().use { output -> write(output, encryptedDoc, encryptedDocFormat) }
    }

    private fun validate() {

        val errorList = mutableListOf<String>()

        if (!this::input.isInitialized) errorList += "No configuration source input provided."
        if (!this::encryptionKey.isInitialized) errorList += "No encryption key provided."
        if (!this::encryptionRules.isInitialized) errorList += "No encryption path rules provided."
        if (!this::output.isInitialized) errorList += "No output/destination provided."

        if (errorList.isNotEmpty()) {
            spec.failedWithHelp(
                errorList.joinToString(
                    separator = "\n",
                    prefix = "Multiple command line errors. Please consult help command:"
                )
            )
        }
    }


    private fun encrypt(
        config: ObjectNode,
        keyWithType: KeyWithType,
        encryptablePaths: List<String>
    ): ObjectNode {

        if (encryptablePaths.isEmpty()) {
            return config
        }

        val encryptable = AntPathMatcher.Builder().withTrimTokens().build()::isMatch
        val encrypt = keyWithType.type.algorithm.newEncrypter()::encrypt

        visitNodePathsOf(config) {
            encryptablePaths.firstOrNull { rule -> encryptable(rule, path) }?.also {
                val encrypted = encrypt(keyWithType, subject.textValue())
                val encryptedNode = textNode("\${enc:$encrypted}")
                set(encryptedNode)
            }
        }

        return config
    }

    private fun write(
        output: OutputStream,
        doc: ObjectNode,
        docFormat: DocumentFormat
    ) {
        try {
            val mapper = ObjectMappers.of(docFormat)
            val generator = mapper.createGenerator(output, JsonEncoding.UTF8)
            mapper.writeTree(generator, doc)
        } finally {
            output.flush()
        }
    }

}

