package becode.sec.common.parsing

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import becode.sec.common.parsing.JsonPathVisitor.visit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class JsonPathVisitorTest {

    @Language("JSON")
    private val doc = """
        {
            "name": "andries",
            "id": "72717271",
            "label": "bottom"
        }""".asTree(ContentMapping.json)


    @Language("JSON")
    private val arrayDoc = """
        [
          {"name": "andries","id": "72717271", "label": "bottom"},
          {"name": "Jason", "id": "6271212-12012012", "label": "top"}
        ]""".asTree(ContentMapping.json)

    @Test
    fun visitDoc() {
        visit(doc) {
            if (node.isValueNode) {
                println("$path : ${node.textValue()}")
            }
        }
    }

    @Test
    fun visitArrayDoc() {
    }
}
