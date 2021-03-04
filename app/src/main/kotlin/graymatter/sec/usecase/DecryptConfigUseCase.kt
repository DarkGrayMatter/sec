package graymatter.sec.usecase

import com.fasterxml.jackson.databind.node.ObjectNode
import com.palantir.config.crypto.EncryptedValue
import com.palantir.config.crypto.KeyWithType
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

    fun run() {
        val doc = decrypt()
        output(doc)
    }

    private fun decrypt(): ObjectNode {

        val doc = source().use { it.readTree<ObjectNode>(sourceFormat) }

        return visitNodePathsOf(doc) {

            val encrypted = node.asText(null)
                ?.takeIf(EncryptedValue::isEncryptedValue)
                ?.let(EncryptedValue::fromString)

            val decrypted = encrypted
                ?.decrypt(keyWithType)

           if (decrypted != null) {
               set(textNode(decrypted))
           }
        }
    }

    private fun output(doc: ObjectNode) {
        destination().bufferedWriter().use {
            DocumentMapper.of(destinationFormat)
                .writerWithDefaultPrettyPrinter()
                .writeValue(it, doc)
        }
    }

}
