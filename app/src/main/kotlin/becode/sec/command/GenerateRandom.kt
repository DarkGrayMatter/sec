package becode.sec.command

import becode.sec.common.BinaryEncoding
import picocli.CommandLine.*

@Command(name = "random", description = ["Generates random bytes"])
class GenerateRandom: Runnable {

    private var byteSize: Int = -1
    private lateinit var encoding: BinaryEncoding

    @Option(names = ["-b", "--enc"], required = true, description = ["Text encoding use to represents random bytes"])
    fun setEncoding(encoding: BinaryEncoding) {
        this.encoding = encoding
    }

    @Option(names = ["--bytes"], required = true, description = ["Size of random number generator."])
    fun setByteSize(bytesSize: Int) {
        this.byteSize = bytesSize
    }

    override fun run() {
        println(encoding)
        println(byteSize)
    }


}
