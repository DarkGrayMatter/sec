package graymatter.sec.usecase

import com.fasterxml.jackson.databind.node.ObjectNode
import com.palantir.config.crypto.EncryptedValue
import com.palantir.config.crypto.KeyWithType
import graymatter.sec.common.crypto.tryExtractEncryptedContent
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.document.DocumentMapper
import graymatter.sec.common.document.readTree
import graymatter.sec.common.document.visitNodePathsOf
import java.io.InputStream
import java.io.OutputStream

class DecryptConfigUseCase(
    private val keyWithType: KeyWithType,
    private val source: () -> InputStream,
    private val sourceFormat: DocumentFormat,
    private val destination: () -> OutputStream,
    private val destinationFormat: DocumentFormat
) {

    fun run() = output(decrypt())

    private fun decrypt(): ObjectNode {

        val doc = source().use { it.readTree<ObjectNode>(sourceFormat) }

        return visitNodePathsOf(doc) {
            if (node.isTextual) {
                val encryptedPart = node.asText().tryExtractEncryptedContent()?.let(EncryptedValue::fromString)
                if (encryptedPart != null) {
                    val decrypted = encryptedPart.decrypt(keyWithType)
                    set(textNode(decrypted))
                }
            }
        }
    }

    private fun output(doc: ObjectNode) {
        destination().bufferedWriter().use { out ->
            DocumentMapper.of(destinationFormat)
                .writerWithDefaultPrettyPrinter()
                .writeValue(out, doc)
        }
    }

}
