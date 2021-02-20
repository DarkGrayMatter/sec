@file:Suppress("MemberVisibilityCanBePrivate")

package graymatter.sec.common.document

import graymatter.sec.common.tr
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream

fun <T : JsonNode> ObjectMapper.treeFromContent(content: String, expectedContentNodeClass: Class<out T>): T {

    val node = readTree(content)

    return when {
        expectedContentNodeClass.isInstance(node) -> expectedContentNodeClass.cast(node)
        else -> throw IllegalArgumentException(
            """
            Unexpected content type parsed. Expected ${expectedContentNodeClass.simpleName}, but found 
            ${node.javaClass.simpleName} instead. (see [${node.toPrettyString()}])"
            """.tr()
        )
    }

}

inline fun <reified T : JsonNode> ObjectMapper.treeFromContent(content: String): T {
    return this.treeFromContent(content, T::class.java)
}

inline fun <reified T : JsonNode> ObjectMapper.treeFrom(input: InputStream): T {
    return readTree(input) as T
}

fun String.asTree(mapper: ObjectMapper): JsonNode = mapper.treeFromContent(this)


inline fun <reified T : JsonNode> InputStream.readTree(format: DocumentFormat): T {
    return ObjectMappers.of(format).readTree(this) as T
}

inline fun <reified T : JsonNode> treeOf(format: DocumentFormat, content: String): T {
    return ObjectMappers.of(format).treeFromContent(content) as T
}

inline fun <reified T : JsonNode> jsonOf(content: String): T = treeOf(DocumentFormat.JSON, content)
