@file:Suppress("ClassName")

package graymatter.sec.usecase

import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.document.DocumentMapper
import graymatter.sec.common.func.Tried
import graymatter.sec.common.func.Try
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.util.concurrent.Callable

class ConvertConfigUseCase(
    sourceProvider: () -> InputStream,
    sourceCharset: Charset,
    private val sourceFormat: DocumentFormat,
    targetProvider: () -> OutputStream,
    targetCharset: Charset,
    private val targetFormat: DocumentFormat,
) : Callable<Tried<ConvertConfigUseCase.Conversion>> {

    sealed class Conversion(val message: String) {
        object completed : Conversion("completed")
        class DidNothing(message: String) : Conversion(message)
    }

    private val reader = { InputStreamReader(sourceProvider(), sourceCharset) }
    private val writer = { OutputStreamWriter(targetProvider(), targetCharset) }

    override fun call(): Tried<Conversion> = Try {
        when (sourceFormat) {
            targetFormat -> Conversion.DidNothing("target and destination has the same format: $sourceFormat")
            else -> convert()
        }
    }

    private fun convert(): Conversion {
        val configDoc = reader().use { DocumentMapper.of(sourceFormat).readTree(it) }
        return when {
            !configDoc.isObject -> Conversion.DidNothing(
                "Expected document structure, but found, ${
                    configDoc.nodeType.title()
                } instead.")
            else -> writer().use { writer ->
                DocumentMapper.of(targetFormat).writer().writeValue(writer, configDoc)
                Conversion.completed
            }
        }
    }
}

private fun Enum<*>.title(): String {
    return buildString {
        append(name.first().toUpperCase())
        append(name.substring(1).toLowerCase().replace('_', ' '))
    }
}
