@file:Suppress("MemberVisibilityCanBePrivate")

package becode.sec.common.parsing

import becode.sec.common.PreCondition.require
import becode.sec.common.parsing.JsonPathVisitor.visitPaths
import becode.sec.common.tr
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import java.util.*

object StructuredDocumentType {

    val json = ObjectMapper().configure()
    val csv = CsvMapper().configure()
    val properties = JavaPropsMapper().configure()
    val yaml = YAMLMapper().configure()

    fun Format.mapper(): ObjectMapper {
        return when (this) {
            Format.JSON -> json
            Format.YAML -> yaml
            Format.PROPERTIES -> properties
            Format.CSV -> csv
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


/**
 * Object to visit a Json based documents based on path.
 *
 * @see [JsonPathVisitor.VisitingPath]
 * @see [visitPaths]
 */
object JsonPathVisitor {

    interface VisitingPath {
        val path: String
        var node: JsonNode
        fun stop()
    }

    private val factory: JsonNodeFactory
        get() = JsonNodeFactory.instance

    private const val SEPARATOR = "."
    private const val EMPTY_PATH = ""

    private sealed class Id(val pathSegment: String) {
        data class ByIndex(val value: Int) : Id(value.toString())
        data class ByField(val field: String) : Id(field)
        object Root : Id(EMPTY_PATH)
    }

    fun visitPaths(root: JsonNode, visitNode: VisitingPath.() -> Unit) {

        object : VisitingPath {

            private var visiting = true
            private var stack = LinkedList<Pair<Id, JsonNode>>()
            private var parent: JsonNode? = null

            init {
                stack.push(Id.Root to root)
                visitOnce()
                visiting = false
            }

            override var path: String = ""
                private set
                get() {
                    requiresVisiting()
                    return field
                }

            override var node: JsonNode = NullNode.instance
                get() {
                    requiresVisiting()
                    return field
                }
                set(value) {
                    requiresVisiting()
                    field = value
                }

            private fun requiresVisiting() = require(visiting) {
                "Tree is not available to visit anymore."
            }

            override fun stop() {
                if (visiting) {
                    visiting = false
                    stack.clear()
                    node = factory.nullNode()
                    path = EMPTY_PATH
                }
            }

            private fun visitOnce() {
                while (visiting && stack.isNotEmpty()) {
                    val (p, n) = stack.pop()
                    when {
                        n.isValueNode -> visitPath(p, n)
                        n.isArray -> visitArray(n as ArrayNode)
                        n.isObject -> visitObject(n as ObjectNode)
                    }
                }
            }

            private fun visitObject(objectNode: ObjectNode) {
                objectNode.fieldNames().forEachRemaining { field ->
                    stack.push(Id.ByField(field) to objectNode.get(field))
                }
            }

            private fun visitArray(arrayNode: ArrayNode) {
                arrayNode.forEachIndexed { index, jsonNode ->
                    stack.push(Id.ByIndex(index) to jsonNode)
                }
            }

            private fun visitPath(id: Id, n: JsonNode) {
                parent = stack.peek()?.second
                path = buildString /*Build only on actual visit */ {
                    stack.joinTo(this, SEPARATOR) { (id, _) -> id.pathSegment }
                    if (id.pathSegment.isNotEmpty()) {
                        if (isNotEmpty()) append(SEPARATOR)
                        append(id.pathSegment)
                    }
                }
                node = n
                visitNode()
            }
        }
    }

}

