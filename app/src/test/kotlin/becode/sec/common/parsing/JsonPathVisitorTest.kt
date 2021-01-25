package becode.sec.common.parsing

import becode.sec.common.parsing.JsonPathVisitor.visitPaths
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

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

        val expected = mapOf(
            "name" to "andries",
            "id" to "72717271",
            "label" to "bottom"
        )

        val actual: Map<String, String?> = mutableMapOf<String, String>().run {
            visitPaths(doc) {
                if (node.isValueNode) {
                    put(path, node.textValue())
                }
            }
            toMap()
        }

        assertEquals(expected, actual)

    }

    @Test
    fun visitArrayDoc() {
        TODO("Not implemented")
    }
}
