@file:Suppress("MemberVisibilityCanBePrivate")

package becode.sec.common.parsing

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import becode.sec.common.parsing.JsonPathVisitor.visit
import becode.sec.common.tr
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

/**
 * Object to visit a Json based documents based on path.
 *
 * @see [JsonPathVisitor.NodeVisit]
 * @see [visit]
 */
object JsonPathVisitor {

    interface NodeVisit {
        val path: String
        val node: JsonNode
        fun stop()
    }

    @JvmStatic
    inline fun visit(root: JsonNode, crossinline visitNode: NodeVisit.() -> Unit) {

        object : NodeVisit {

            private val queue: Queue<Pair<String, JsonNode>> = LinkedList()
            private var visiting = false
            private var currentPath: String? = null
            private var currentNode: JsonNode? = null

            init {
                visiting = true
                enqueue(root, "")
                visit()
            }

            override val node: JsonNode
                get() = currentNode.takeIf { visiting } ?: throw IllegalStateException()

            override val path: String
                get() = currentPath.takeIf { visiting } ?: throw IllegalStateException()

            override fun stop() {
                queue.clear()
                visiting = false
            }

            private fun enqueue(node: JsonNode, id: String) {
                require(visiting)
                when {
                    node.isObject -> node.fieldNames().forEach { field -> enqueue(node.get(field), field) }
                    node.isArray -> (0..node.size()).forEach { index -> enqueue(node.get(index), "$index") }
                }
                queue.add(id to node)
            }

            private fun dequeue() {
                val (id, node) = queue.remove()
                currentNode = node
                currentPath = buildString {
                    queue.joinTo(this, ".") { (id, _) -> id }
                    if (isNotEmpty()) append(".")
                    append(id)
                }
            }

            private fun visit() {
                require(visiting)
                try {
                    while (visiting && queue.isNotEmpty()) {
                        dequeue()
                        visitNode()
                    }
                } finally {
                    // Always cleanup!
                    visiting = false
                    queue.clear()
                    currentNode = null
                    currentPath = null
                }
            }
        }
    }
}

fun String.asTree(mapper: ObjectMapper): JsonNode = mapper.treeFromContent(this)
