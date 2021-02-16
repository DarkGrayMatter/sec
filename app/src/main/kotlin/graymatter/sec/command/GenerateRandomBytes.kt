package graymatter.sec.command

import graymatter.sec.common.BinaryEncoding
import graymatter.sec.common.exception.failCommandOn
import picocli.CommandLine.*
import java.security.SecureRandom

@Command(name = "generate-random-bytes", description = ["Generates random bytes"])
class GenerateRandomBytes : Runnable {

    private var numberOfChunks: Int = 1
    private var byteSize: Int = -1
    private lateinit var encoding: BinaryEncoding

    private val secureRandom by lazy(LazyThreadSafetyMode.NONE, ::SecureRandom)

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
        if (numberOfChunks == 1) {
            println(generateEncodedRandomBytes())
        } else generateSequence { generateEncodedRandomBytes() }.take(numberOfChunks).forEach {
            println(it)
        }
    }

    private fun generateEncodedRandomBytes() = secureRandom.run {
        ByteArray(byteSize).run {
            nextBytes(this)
            encoding.encode(this)
        }
    }
}
