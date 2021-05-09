@file:Suppress("ClassName")

package graymatter.sec.usecase

import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.document.DocumentMapper
import graymatter.sec.common.func.Either
import graymatter.sec.common.func.eitherTry
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.*
import java.util.concurrent.Callable

class ConvertConfigUseCase(
    sourceProvider: () -> InputStream,
    private val sourceFormat: DocumentFormat,
    targetProvider: () -> OutputStream,
    private val targetFormat: DocumentFormat,
) : Callable<Either<Exception, ConvertConfigUseCase.CompletionState>> {

    sealed class CompletionState(val message: String) {
        object Completed : CompletionState("completed")
        class DidNothing(message: String) : CompletionState(message)
    }

    private val reader = { InputStreamReader(sourceProvider(), Charsets.UTF_8) }
    private val writer = { OutputStreamWriter(targetProvider(), Charsets.UTF_8) }

    override fun call(): Either<Exception, CompletionState> {
        return eitherTry {
            when (sourceFormat) {
                targetFormat -> CompletionState.DidNothing("target and destination has the same format: $sourceFormat")
                else -> convert()
            }
        }
    }

    private fun convert(): CompletionState {
        val configDoc = reader().use { DocumentMapper.of(sourceFormat).readTree(it) }
        return when {
            !configDoc.isObject -> CompletionState.DidNothing(
                "Expected document structure, but found, ${
                    configDoc.nodeType.title()
                } instead.")
            else -> writer().use { writer ->
                DocumentMapper.of(targetFormat).writer().withDefaultPrettyPrinter().writeValue(writer, configDoc)
                CompletionState.Completed
            }
        }
    }

    private fun Enum<*>.title(): String {
        return buildString {
            append(name.first().uppercaseChar())
            append(name.substring(1).lowercase(Locale.getDefault()).replace('_', ' '))
        }
    }
}

