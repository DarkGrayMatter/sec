package graymatter.sec.common.document

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeCreator
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import kotlin.LazyThreadSafetyMode.PUBLICATION

/**
 * This container keeps track of content, as well as maintaining a relation to the parent [Container]. This
 * arrangement enables the container to build a path to the node on demand via the [Container.path] calculate/lazy
 * property.
 *
 * @param key The key of the container to identify this container on the [parent]
 * @param parent A parent for this container. Maybe `null` if this is root container.
 * @param content The content of this container.
 */
private class Container(val key: Any, val parent: Container?, content: JsonNode) {

    var content: JsonNode = content
        private set

    constructor(root: JsonNode) : this("", null, root)

    val path: String by lazy(PUBLICATION) {
        buildString {
            parent?.path?.also(this::append)
            if (!endsWith(PATH_SEPARATOR)) append(PATH_SEPARATOR)
            append(key)
        }
    }

    val isLeaf: Boolean
        get() = content.isValueNode

    val isRoot: Boolean
        get() = parent == null

    fun replace(new: JsonNode): JsonNode? {
        return when {
            new == content -> null
            parent == null -> null
            else -> when (val owner = parent.content) {
                is ArrayNode -> content.also { owner.set(key as Int, new) }
                is ObjectNode -> content.also { owner.set<JsonNode>(key as String?, new) }
                else -> null
            }
        }
    }

    companion object {
        private const val PATH_SEPARATOR = "/"
    }
}

/**
 * Implements the business logic of visiting via the [run] function. Exposes also control functions
 * such [VisitingPath.stop] which allow the caller to stop processing content nodes.
 *
 * > **Important:** The this visitor implementation is not thread safe.
 */
private class PathVisitor(
    private val visitNodePath: JsonNodePathVisitor.() -> Unit
) : JsonNodePathVisitor,
    JsonNodeCreator by JsonNodeFactory.instance {

    var visiting = true; private set
    private var container: Container? = null

    override val item: JsonNode
        get() = connectedContainer.content

    override val path: String
        get() = connectedContainer.path

    override fun stop() {
        visiting = false
    }

    fun run(root: JsonNode) {
        visiting = true
        try {
            visit(Container(root))
        } finally {
            visiting = false
        }
    }

    override fun replace(new: JsonNode): JsonNode? = connectedContainer.replace(new)

    private val connectedContainer: Container
        get() = container.takeIf { visiting } ?: throw IllegalStateException()

    private fun visit(container: Container) {

        if (!visiting) {
            return
        }

        var leafContainer: Container? = null

        val children = when (container.content) {

            is ArrayNode -> (0 until container.content.size()).asSequence()
                .map { index -> Container(index, container, container.content[index]) }

            is ObjectNode -> container.content.fieldNames().asSequence()
                .map { field -> Container(field, container, container.content[field]) }

            else -> {
                leafContainer = container
                null
            }
        }

        leafContainer?.also(this::visitLeaf)
        children?.also(this::visitChildren)
    }

    private fun visitLeaf(container: Container) {
        this.container = container
        this.visitNodePath()
    }

    private fun visitChildren(children: Sequence<Container>) {
        for (child in children) {
            if (!visiting) break
            when {
                child.isLeaf -> visitLeaf(child)
                else -> visit(child)
            }
        }
    }
}

/**
 * Visits content nodes of from root node.
 *
 * @param root The root node to start visiting from.
 * @param visitNodePath A lambda with which takes a [VisitingPath] as receiver.
 * @return The root node visited.
 */
fun <N : JsonNode> visitNodePathsOf(root: N, visitNodePath: JsonNodePathVisitor.() -> Unit): N {
    PathVisitor(visitNodePath).run(root)
    return root
}
