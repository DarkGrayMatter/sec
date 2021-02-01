package becode.sec.common.parsing

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class JsonValueVisitImplTest {

    @Language("JSON")
    private val doc = """
        {
            "name": "andries",
            "id": "72717271",
            "label": "bottom"
        }""".asTree(StructuredDocumentType.json)


    @Language("JSON")
    private val arrayDoc = """
        [
          {"name": "andries","id": "72717271", "label": "bottom"},
          {"name": "Jason", "id": "6271212-12012012", "label": "top"}
        ]""".asTree(StructuredDocumentType.json)

    @Test
    fun visitDoc() {

        val expected = mapOf(
            "name" to "andries",
            "id" to "72717271",
            "label" to "bottom"
        )

    }

}
