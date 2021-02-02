package becode.sec.command

import becode.sec.common.BinaryEncoding
import picocli.CommandLine.*
import java.io.PrintWriter

@Command(name = "random", description = ["Generates random numbers"])
class GenerateRandom {

    private var byteSize: Int = -1
    private lateinit var encoding: BinaryEncoding

    @Mixin
    private lateinit var formatTextBlock: TextBlockFormatting


    @Option(names = ["-e", "--enc"], required = true, description = ["How encrypt the binary value as text."])
    fun setEncoding(encoding: BinaryEncoding) {
        this.encoding = encoding
    }

    @Option(names = ["-b", "--bytes"], required = true, description = ["Size of random number generator."])
    fun setByteSize(bytesSize: Int) {
        this.byteSize = bytesSize
    }


    fun run() {
        TODO()
    }


    class TextBlockFormatting {

        private var numColumns: Int = 1
        private var columnSpacing: Int = 1

        @Option(names = ["--columns"])
        fun setColumns(n: Int) {
            this.numColumns = n
        }


        operator fun invoke(textBlock: String, output: PrintWriter): String {
            TODO()
        }
    }
}
