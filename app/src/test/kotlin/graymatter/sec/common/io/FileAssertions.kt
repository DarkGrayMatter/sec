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

fun assertFileHasContentOf(
    format: DocumentFormat,
    file: File,
    vararg assertFurtherThat: (props: Properties) -> Unit,
) {

    assertTrue(file.exists(), "File does not exists: $file")
    assertTrue(file.length() > 0, "File is empty: $file")

    fun readAsProperties(): Properties {
        return Properties().apply {
            visitNodePathsOf(treeOf<ObjectNode>(format, file.readText())) {
                if (node.isValueNode || node.isMissingNode) {
                    val value = node.asText(null)
                    val key = path.substring(1).replace('/', '.')
                    setProperty(key, value)
                }
            }
        }
    }

    val props = assertDoesNotThrow({ "Error parsing file as ${format.name.lowercase(Locale.getDefault())}: $file" }) {
        when (format) {
            DocumentFormat.JAVA_PROPERTIES -> Properties(file)
            else -> readAsProperties()
        }
    }

    assertFurtherThat.forEach { it(props) }
}
