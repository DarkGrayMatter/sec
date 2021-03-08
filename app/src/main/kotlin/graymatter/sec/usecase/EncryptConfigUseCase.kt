package graymatter.sec.usecase

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.palantir.config.crypto.KeyWithType
import graymatter.sec.common.crypto.wrapAsEncryptedContent
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.document.DocumentMapper
import graymatter.sec.common.document.readTree
import graymatter.sec.common.document.visitNodePathsOf
import io.github.azagniotov.matcher.AntPathMatcher
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

class EncryptConfigUseCase @JvmOverloads constructor(
    private val openInput: () -> InputStream,
    private val openOutput: () -> OutputStream,
    private val inputFormat: DocumentFormat,
    private val keyWithType: KeyWithType,
    private val encryptedPaths: List<String>,
    private val outputFormat: DocumentFormat,
    private val charset: Charset = Charsets.UTF_8,
) : Runnable {

    override fun run() {

        val doc: ObjectNode = openInput().use { it.readTree(inputFormat) }
        val encryptedDoc = encrypt(doc)
        val encryptedDocMapper = DocumentMapper.of(outputFormat)

        encryptedDocMapper.writerWithDefaultPrettyPrinter().apply {
            openOutput().bufferedWriter(charset).use { output ->
                writeValue(output, encryptedDoc)
            }
        }
    }

    private fun isEncryptable(node: JsonNode): Boolean = !node.isNull

    private fun encrypt(doc: ObjectNode): ObjectNode {

        if (encryptedPaths.isEmpty()) {
            return doc
        }

        val encrypt = keyWithType.type.algorithm.newEncrypter()::encrypt
        val matches = AntPathMatcher.Builder().build()::isMatch

        return visitNodePathsOf(doc) {
            if (isEncryptable(node)) {
                encryptedPaths.firstOrNull { expression -> matches(expression, path) }?.also {
                    val encrypted = encrypt(keyWithType, node.asText()).toString()
                    val encryptedNode = textNode(encrypted.wrapAsEncryptedContent())
                    set(encryptedNode)
                }
            }
        }
    }
}
