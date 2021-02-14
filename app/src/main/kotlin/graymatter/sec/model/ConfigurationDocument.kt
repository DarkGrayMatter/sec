package graymatter.sec.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

class ConfigurationDocument(
    private var content: ObjectNode
) {

    interface PathEditor {
        var node: JsonNode
        val path: String
        fun drop()
    }

    fun update(editPath: PathEditor.() -> Unit) {

    }

}


