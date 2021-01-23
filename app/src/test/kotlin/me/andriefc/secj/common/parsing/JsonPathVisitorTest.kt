package me.andriefc.secj.common.parsing

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class JsonPathVisitorTest {

    @Language("JSON")
    private val doc = """
        {
            "name": "andries",
            "id": "72717271",
            "label": "bottom"
        }"""


    @Language("JSON")
    private val arrayDoc = """
        [
          {"name": "andries","id": "72717271", "label": "bottom"},
          {"name": "Jason", "id": "6271212-12012012", "label": "top"}
        ]"""

    @Test
    fun visitDoc() {
    }

    @Test
    fun visitArrayDoc() {

    }
}
