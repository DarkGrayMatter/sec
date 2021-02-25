package graymatter.sec.usecase

import com.fasterxml.jackson.databind.node.ObjectNode
import com.palantir.config.crypto.KeyWithType
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.document.ObjectMappers
import graymatter.sec.common.document.readTree
import graymatter.sec.common.document.visitNodePathsOf
import io.github.azagniotov.matcher.AntPathMatcher
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

class EncryptConfigurationUseCase(
    private val openInput: () -> InputStream,
    private val openOutput: () -> OutputStream,
    private val inputFormat: DocumentFormat,
    private val keyWithType: KeyWithType,
    private val encryptablePaths: List<String>,
    private val outputFormat: DocumentFormat,
    private val charset: Charset = Charsets.UTF_8
) : Runnable {

    override fun run() {

        val doc: ObjectNode = openInput().use { it.readTree(inputFormat) }
        val encryptedDoc = encrypt(doc)
        val encryptedDocMapper = ObjectMappers.of(outputFormat)

        openOutput().bufferedWriter(charset).use { output ->
            encryptedDocMapper.writeTree(
                encryptedDocMapper.createGenerator(output),
                encryptedDoc
            )
        }
    }

    private fun encrypt(doc: ObjectNode): ObjectNode {

        if (encryptablePaths.isEmpty()) {
            return doc
        }

        val encryptable = AntPathMatcher.Builder().withTrimTokens().build()::isMatch
        val encrypt = keyWithType.type.algorithm.newEncrypter()::encrypt

        return visitNodePathsOf(doc) {
            encryptablePaths.firstOrNull { rule -> encryptable(rule, path) }?.also {
                val encrypted = encrypt(keyWithType, subject.textValue())
                val encryptedNode = textNode("{$encrypted}")
                set(encryptedNode)
            }
        }
    }
}
