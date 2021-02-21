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
import io.github.azagniotov.matcher.AntPathMatcher
import picocli.CommandLine
import java.io.OutputStream

@CommandLine.Command(name = "encrypt-config", description = ["Encrypt a configuration document given a key"])
class EncryptConfig : Runnable {

    @CommandLine.Mixin
    lateinit var sourceConfig: ConfigSourceRequirements

    @CommandLine.Mixin
    lateinit var encryptionKey: KeyRequirements

    @CommandLine.Mixin
    lateinit var encryptionRules: ConfigProcessingRulesRequirements

    @CommandLine.Mixin
    lateinit var outputConfig: ConfigOutputRequirements

    override fun run() {

        val doc: ObjectNode = sourceConfig.run {
            input.source.open().use {
                it.readTree(requireNotNull(overriddenInputFormat))
            }
        }

        val keyWithType = encryptionKey.keyWithType()
        val encryptedDoc = encrypt(doc, keyWithType, encryptionRules.rules)
        val encryptedDocFormat = requireNotNull(outputConfig.outputFormat ?: sourceConfig.overriddenInputFormat)

        outputConfig.target.output.open().use { output -> write(output, encryptedDoc, encryptedDocFormat) }

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
