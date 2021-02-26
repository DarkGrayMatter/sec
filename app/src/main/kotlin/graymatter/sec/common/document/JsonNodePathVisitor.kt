package graymatter.sec.common.document

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeCreator

interface JsonNodePathVisitor : VisitingPath<JsonNode>, JsonNodeCreator {
    /**
     * Replaces [item] with [new] if the new item is not the same as current item.
     *
     * @param new The item to replace the current item being visited.
     * @return The replaced item or `null` if the new item is the same as current item.
     */
    fun replace(new: JsonNode): JsonNode?

}
