package graymatter.sec.common.io

import com.fasterxml.jackson.databind.node.ObjectNode
import graymatter.sec.common.Properties
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.document.treeOf
import graymatter.sec.common.document.visitNodePathsOf
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import java.util.*
import kotlin.test.assertTrue

fun File.assertFileHasContentOf(format: DocumentFormat, vararg assertFurtherThat:(props: Properties) -> Unit) {

    assertTrue(exists(), "File does not exists: $this")
    assertTrue(length() > 0, "File is empty: $this")

    val props = assertDoesNotThrow("Error parsing file as ${format.name.toLowerCase()}: $this") {
        when (format) {
            DocumentFormat.JAVA_PROPERTIES -> Properties(this)
            else -> Properties().apply {
                visitNodePathsOf(treeOf<ObjectNode>(format, readText())) {
                    if (node.isValueNode || node.isMissingNode) {
                        val value = node.asText(null)
                        val key = path.substring(1).replace('/', '.')
                        setProperty(key, value)
                    }
                }
            }
        }
    }

    assertFurtherThat.forEach { it(props) }
}
