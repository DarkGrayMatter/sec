package graymatter.sec.common

import com.fasterxml.jackson.databind.node.ObjectNode
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.document.writeTree
import java.io.*
import java.net.URL
import java.util.*

fun Properties.load(bytes: ByteArray) = ByteArrayInputStream(bytes).use(this::load)
fun Properties.load(text: String) = StringReader(text).use(this::load)
fun Properties.load(bytes: ByteArrayOutputStream) = load(bytes.toByteArray())
fun Properties.load(text: StringWriter) = load(text.toString())
fun Properties.load(text: StringBuilder) = load(text.toString())


fun Properties(tree: ObjectNode): Properties {
    val source = ByteArrayOutputStream().apply { use { it.writeTree(tree, DocumentFormat.JAVA_PROPERTIES) } }
    return Properties().apply { load(source) }
}


fun Properties(url: URL): Properties = url.openStream().use { Properties(it) }
fun Properties(input:InputStream): Properties = Properties().apply { load(input) }
fun Properties(text: String): Properties = Properties().apply { load(text) }
fun Properties(map: Map<String, String?>): Properties = Properties().apply { putAll(map) }
fun Properties(vararg pairs: Pair<String, String?>): Properties = java.util.Properties().apply {
    pairs.forEach { (k, v) -> put(k, v) }
}

fun Properties.toPropertiesMap(): Map<String, String?> {
    return entries.asSequence().map { (k, v) -> k as String to v as String? }.toMap()
}

fun Properties(bytes: ByteArray) = Properties().also { it.load(ByteArrayInputStream(bytes)) }
fun Properties(file: File) = Properties().apply { file.inputStream().use { load(it) } }
fun Properties(source: ByteArrayOutputStream) = Properties(source.toByteArray())
