@file:Suppress("TestFunctionName")

package graymatter.sec.testing

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.node.ObjectNode
import graymatter.sec.App
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.document.DocumentMapper
import graymatter.sec.common.resourceAt
import java.net.URI

sealed class SampleConfig {

    abstract val uri: URI
    abstract val format: DocumentFormat
    abstract val content: String

    fun document(): ObjectNode = DocumentMapper.of(format).readTree(content) as ObjectNode

    final override fun toString(): String {
        return DocumentMapper.of(format).let { mapper ->
            val doc = mapper.createObjectNode().run {
                put("storageFormat", when (format) {
                    DocumentFormat.JSON -> "Json"
                    DocumentFormat.YAML -> "Yamel"
                    DocumentFormat.JAVA_PROPERTIES -> "Java Properties"
                })
                put("uri", "$uri")
                set<ObjectNode>("configuration", document())
            }

            val writer = mapper.writer().with(DefaultPrettyPrinter())
            writer.writeValueAsString(doc)
        }
    }
}


private class SampleConfigImpl(
    override val format: DocumentFormat,
    override val uri: URI,
    override val content: String,
) : SampleConfig() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SampleConfigImpl

        if (format != other.format) return false
        if (uri != other.uri) return false
        if (content != other.content) return false

        return true
    }

    override fun hashCode(): Int {
        var result = format.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + content.hashCode()
        return result
    }
}

fun SampleConfig(format: DocumentFormat): SampleConfig {
    val resource = resourceAt<App>("/samples/sample-config.${
        when (format) {
            DocumentFormat.JSON -> "json"
            DocumentFormat.YAML -> "yaml"
            DocumentFormat.JAVA_PROPERTIES -> "properties"
        }
    }")

    return SampleConfigImpl(format, resource.toURI(), resource.readText(Charsets.UTF_8))
}
