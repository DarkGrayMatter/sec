@file:Suppress("MemberVisibilityCanBePrivate")

package becode.sec.common.parsing

import becode.sec.common.tr
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper

object StructuredDocumentType {

    val json = ObjectMapper().configure()
    val csv = CsvMapper().configure()
    val properties = JavaPropsMapper().configure()
    val yaml = YAMLMapper().configure()

    fun DocumentedFormat.mapper(): ObjectMapper {
        return when (this) {
            DocumentedFormat.JSON -> json
            DocumentedFormat.YAML -> yaml
            DocumentedFormat.PROPERTIES -> properties
            DocumentedFormat.CSV -> csv
        }
    }

    private fun ObjectMapper.configure(): ObjectMapper = findAndRegisterModules()
}

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

fun String.asTree(mapper: ObjectMapper): JsonNode = mapper.treeFromContent(this)

