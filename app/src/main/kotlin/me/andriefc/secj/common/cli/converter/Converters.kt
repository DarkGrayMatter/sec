@file:Suppress("unused")

package me.andriefc.secj.common.cli.converter

import me.andriefc.secj.common.cli.CommandLogLevel
import me.andriefc.secj.common.io.IOSource
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
