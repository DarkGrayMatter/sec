@file:Suppress("unused")

package becode.sec.common.cli.converter

import becode.sec.common.io.IOSource
import picocli.CommandLine.ITypeConverter


class InputSourceConverter : ITypeConverter<IOSource.Input> {
    override fun convert(value: String?): IOSource.Input = IOSource.Input.fromString(requireNotNull(value))
}

class OutputSourceConverter : ITypeConverter<IOSource.Output> {
    override fun convert(value: String?): IOSource.Output = IOSource.Output.fromString(requireNotNull(value))
}

object CommaSeparatedListConverter : ITypeConverter<List<String>> {
    override fun convert(value: String?): List<String> {
        return when (value) {
            null -> emptyList()
            else -> value.splitToSequence(",").toList()
        }
    }
}
