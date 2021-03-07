package graymatter.sec.command

import graymatter.sec.command.reuse.group.OutputTargetArgGroup
import graymatter.sec.command.reuse.mixin.GivenSeed
import graymatter.sec.common.cli.validate
import graymatter.sec.common.crypto.BinaryEncoding
import graymatter.sec.common.exception.failCommandOn
import picocli.CommandLine.*
import java.security.SecureRandom

@Command(name = "generate-random-bytes", description = ["Generates random bytes"])
class GenerateRandomBytes : Runnable {

    private var numberOfChunks: Int = 1
    private var byteSize: Int = -1
    private lateinit var encoding: BinaryEncoding

    @Spec
    lateinit var spec: Model.CommandSpec

    @ArgGroup(validate = true, heading = "If you want specify a seed, use the following options:%n")
    private var givenSeed: GivenSeed? = null

    private val secureRandom by lazy(LazyThreadSafetyMode.NONE) {
        when (val seedAsBytes = givenSeed?.asBytes()) {
            null -> SecureRandom()
            else -> SecureRandom(seedAsBytes)
        }
    }

    @Option(
        names = ["-e", "--enc", "--encoding"],
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

    @Option(names = ["-N"], required = false, description = ["How many random values should be generated."])
    fun setNumberToGenerate(n: Int) {
        numberOfChunks = n
    }

    override fun run() {
        validateCli()
        generateRandomBytes()
    }

    private fun generateRandomBytes() {
        if (numberOfChunks == 1) {
            println(generateEncodedRandomBytes())
        } else generateSequence { generateEncodedRandomBytes() }.take(numberOfChunks).forEach {
            println(it)
        }
    }

    private fun validateCli() {
        validate(spec) {
            requires(numberOfChunks >= 0) {
                "Number of generated values should always be 1 or more."
            }
        }
    }

    private fun generateEncodedRandomBytes() = secureRandom.run {
        ByteArray(byteSize).run {
            nextBytes(this)
            encoding.encode(this)
        }
    }
}
