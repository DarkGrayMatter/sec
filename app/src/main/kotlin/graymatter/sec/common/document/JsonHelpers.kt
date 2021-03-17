@file:Suppress("MemberVisibilityCanBePrivate")

package graymatter.sec.common.document

import graymatter.sec.common.trimIndentToSentence
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

fun <T : JsonNode> ObjectMapper.treeFromContent(content: String, expectedContentNodeClass: Class<out T>): T {

    val node = readTree(content)

    return when {
        expectedContentNodeClass.isInstance(node) -> expectedContentNodeClass.cast(node)
        else -> throw IllegalArgumentException(
            """
            Unexpected content type parsed. Expected ${expectedContentNodeClass.simpleName}, but found 
             ${node.javaClass.simpleName} instead. (see [${node.toPrettyString()}])"
            """.trimIndentToSentence()
        )
    }
}

inline fun <reified T : JsonNode> ObjectMapper.treeFromContent(content: String): T {
    return this.treeFromContent(content, T::class.java)
}

inline fun <reified T : JsonNode> InputStream.readTree(format: DocumentFormat, charset: Charset = Charsets.UTF_8): T {
    return DocumentMapper.of(format).readTree(reader(charset)) as T
}

inline fun <reified T : JsonNode> treeOf(format: DocumentFormat, content: String): T {
    return DocumentMapper.of(format).treeFromContent(content) as T
}

inline fun <reified T : JsonNode> jsonOf(content: String): T = treeOf(DocumentFormat.JSON, content)

fun OutputStream.writeTree(tree: JsonNode, format: DocumentFormat) {
    val mapper = DocumentMapper.of(format)
    mapper.writeTree(mapper.createGenerator(this), tree)
    flush()
}
