@file:Suppress("MemberVisibilityCanBePrivate")

package becode.sec.common.parsing

import becode.sec.common.PreCondition.require
import becode.sec.common.parsing.JsonPathVisitor.visitPaths
import becode.sec.common.tr
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import java.util.*

object ContentMapping {

    val json = ObjectMapper().findAndRegisterModules().configureCommons()
    val csv = CsvMapper().findAndRegisterModules().configureCommons()
    val properties = JavaPropsMapper().findAndRegisterModules().configureCommons()
    val yaml = YAMLMapper().findAndRegisterModules().configureCommons()

    fun StructuredDocumentFormat.mapper(): ObjectMapper {
        return when (this) {
            StructuredDocumentFormat.JSON -> json
            StructuredDocumentFormat.YAML -> yaml
            StructuredDocumentFormat.PROPERTIES -> properties
            StructuredDocumentFormat.CSV -> csv
        }
    }

    private fun ObjectMapper.configureCommons() = apply {
        // todo: set date formatting
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


fun String.asTree(mapper: ObjectMapper): JsonNode = mapper.treeFromContent(this)


/**
 * Object to visit a Json based documents based on path.
 *
 * @see [JsonPathVisitor.NodeVisit]
 * @see [visitPaths]
 */
object JsonPathVisitor {

    interface NodeVisit {
        val path: String
        val node: JsonNode
        fun stop()
    }

    private val factory: JsonNodeFactory
        get() = JsonNodeFactory.instance

    private const val SEPARATOR = "."

    fun visitPaths(root: JsonNode, visitNode: NodeVisit.() -> Unit) {

        object : NodeVisit {

            private var visiting = true
            private var stack = LinkedList<Pair<String, JsonNode>>()

            init {
                if (push(root)) {
                    visitOnce()
                }
                visiting = false
            }


            private fun push(jsonNode: JsonNode): Boolean {
                return when {
                    jsonNode.isArray -> {
                        (0..jsonNode.size())
                            .map(Int::toString)
                            .zip(jsonNode)
                            .forEach(stack::push)
                        true
                    }
                    jsonNode.isObject -> {
                        jsonNode.fields()
                            .asSequence()
                            .forEach { (k, v) -> stack.push(k to v) }
                        true
                    }
                    jsonNode.isValueNode && jsonNode == root -> {
                        val name = jsonNode.asText()
                        val fieldValue = factory.nullNode()
                        stack.push(name to fieldValue)
                        true
                    }
                    else -> false
                }
            }

            private fun visitOnce() {
                try {
                    while (visiting && stack.isNotEmpty()) {
                        val (k, v) = stack.pop()
                        node = v
                        path = buildPathTo(k)
                        visitNode()
                        if (visiting && node.isContainerNode) {
                            node.forEach(this::push)
                        }
                    }
                } finally {
                    stop()
                }
            }

            private fun buildPathTo(k: String): String {
                return buildString {
                    stack.joinTo(this, SEPARATOR)
                    if (isNotEmpty()) append(SEPARATOR)
                    append(k)
                }
            }

            override var path: String = ""
                private set
                get() {
                    requiresVisiting()
                    return field
                }

            override var node: JsonNode = NullNode.instance
                private set
                get() {
                    requiresVisiting()
                    return field
                }

            private fun requiresVisiting() = require(visiting) {
                "Tree is not available to visit anymore."
            }

            override fun stop() {
                if (visiting) {
                    visiting = false
                    stack.clear()
                    node = factory.nullNode()
                    path = ""
                }
            }

        }
    }

}

