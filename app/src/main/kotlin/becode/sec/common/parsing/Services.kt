@file:Suppress("MemberVisibilityCanBePrivate")

package becode.sec.common.parsing

import becode.sec.common.tr
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import java.io.InputStream

object Mappers {

    val json = ObjectMapper().configure()
    val csv = CsvMapper().configure()
    val properties = JavaPropsMapper().configure()
    val yaml = YAMLMapper().configure()

    private fun ObjectMapper.configure(): ObjectMapper = findAndRegisterModules()

    operator fun get(format: ConfigurationFormat): ObjectMapper {
        return when(format) {
            ConfigurationFormat.JSON -> json
            ConfigurationFormat.YAML -> yaml
            ConfigurationFormat.PROPERTIES -> properties
            ConfigurationFormat.CSV -> csv
        }
    }
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

inline fun <reified T : JsonNode> ObjectMapper.treeFrom(input: InputStream): T {
    return readTree(input) as T
}

fun String.asTree(mapper: ObjectMapper): JsonNode = mapper.treeFromContent(this)


inline fun <reified T: JsonNode> InputStream.readTree(format: ConfigurationFormat): T {
    return Mappers[format].readTree(this) as T
}
