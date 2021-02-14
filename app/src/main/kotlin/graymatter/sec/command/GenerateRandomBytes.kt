package graymatter.sec.command

import becode.sec.common.BinaryEncoding
import becode.sec.common.exception.failCommandOn
import picocli.CommandLine.*
import java.security.SecureRandom

@Command(name = "generate-random-bytes", description = ["Generates random bytes"])
class GenerateRandomBytes : Runnable {

    private var numberOfChunks: Int = 1
    private lateinit var generateRandomBytes: ByteArray.() -> Unit
    private var byteSize: Int = -1
    private lateinit var encoding: BinaryEncoding

    @Option(
        names = ["-b", "--base"],
        required = true,
        description = ["Binary encoding use to represents random bytes"],
        showDefaultValue = Help.Visibility.ALWAYS,
        defaultValue = "base64"
    )
    fun setEncoding(encoding: BinaryEncoding) {
        this.encoding = encoding
    }

    @Option(
        names = ["--bytes"],
        required = true,
        description = ["Size of random number generator."]
    )
    fun setByteSize(bytesSize: Int) {
        this.byteSize = bytesSize
    }

    @Option(names = ["-n"], required = false, description = ["How many random values should be generated."])
    fun setNumberToGenerate(n: Int) {
        failCommandOn(
            n <= 0,
            ExitCode.USAGE,
            "Number of chunks or random bytes to generate must be zero or more."
        )
        numberOfChunks = n
    }

    override fun run() {

        val randomGeneratedChunks = generateSequence {
            ByteArray(byteSize).run {
                val rnd = SecureRandom()
                rnd.nextBytes(this)
                encoding.encode(this)
            }
        }

        randomGeneratedChunks.take(numberOfChunks).forEach { println(it) }
    }

}
