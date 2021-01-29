@file:Suppress("MemberVisibilityCanBePrivate")

package becode.sec.common.parsing

import becode.sec.common.PreCondition.require
import becode.sec.common.parsing.JsonPathVisitor.visit
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
 * @see [JsonPathVisitor.NodeVisit]
 * @see [visit]
 */
object JsonPathVisitor {

    interface NodeVisit {
        val path: String
        var node: JsonNode
        fun stop()
    }

    private val factory: JsonNodeFactory
        get() = JsonNodeFactory.instance

    private sealed class PathLocation(val segment: String) {

        open fun extendPath(path: StringBuilder): Boolean {
            if (path.isNotEmpty()) path.append(PATH_SEPARATOR)
            path.append(segment)
            return true
        }

        object Root : PathLocation(EMPTY_PATH) {
            override fun extendPath(path: StringBuilder): Boolean = false
        }


        data class AtIndex(val index: Int) : PathLocation(index.toString())
        data class AtField(val name: String) : PathLocation(name)

        companion object {
            const val PATH_SEPARATOR: Char = '.'
            const val EMPTY_PATH = ""
        }

    }

    fun visit(root: JsonNode, visitNode: NodeVisit.() -> Unit) {

        object : NodeVisit {

            private var visiting = true
            private var stack = LinkedList<Pair<PathLocation, JsonNode>>()
            private var parent: JsonNode? = null

            init {
                stack.push(PathLocation.Root to root)
                visit()
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
                    path = PathLocation.Root.segment
                }
            }

            private fun visit() {
                while (visiting && stack.isNotEmpty()) {
                    val (location, node) = stack.pop()
                    when {
                        node.isValueNode -> visitValue(node, location)
                        node.isArray -> visitArray(node as ArrayNode)
                        node.isObject -> visitObject(node as ObjectNode)
                    }
                }
                visiting = false
            }

            private fun visitObject(objectNode: ObjectNode) {
                objectNode.fieldNames().forEachRemaining { field ->
                    stack.push(PathLocation.AtField(field) to objectNode.get(field))
                }
            }

            private fun visitArray(arrayNode: ArrayNode) {
                arrayNode.forEachIndexed { index, jsonNode ->
                    stack.push(PathLocation.AtIndex(index) to jsonNode)
                }
            }

            private fun visitValue(valueNode: JsonNode, valueLocation: PathLocation) {

                parent = when {
                    stack.isEmpty() -> null
                    else -> stack.peek().second
                }

                node = valueNode
                path = buildString {
                    for ((location, _) in stack) location.extendPath(this)
                    valueLocation.extendPath(this)
                }

                visitNode()
            }
        }
    }

}

