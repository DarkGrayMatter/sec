@file:Suppress("unused")

package graymatter.sec.common.cli.converter

import graymatter.sec.common.BinaryEncoding
import graymatter.sec.common.io.IOSource
import picocli.CommandLine.ITypeConverter


object InputSourceConverter : ITypeConverter<IOSource.Input> {
    override fun convert(value: String?): IOSource.Input = IOSource.Input.fromString(requireNotNull(value))
}

object OutputSourceConverter : ITypeConverter<IOSource.Output> {
    override fun convert(value: String?): IOSource.Output = IOSource.Output.fromString(requireNotNull(value))
}

object BinaryEncodingConverter : ITypeConverter<BinaryEncoding> {
    override fun convert(value: String): BinaryEncoding {
        return BinaryEncoding.fromName(value)
    }
}

