package graymatter.sec.command

import graymatter.sec.command.reuse.mixin.GivenSeed
import graymatter.sec.common.cli.SelfValidatingCommand
import graymatter.sec.common.crypto.BinaryEncoding
import graymatter.sec.common.trimIndentToSentence
import graymatter.sec.common.validation.Validator
import graymatter.sec.common.validation.requiresThat
import picocli.CommandLine.*
import java.security.SecureRandom

@Command(name = "generate-random-bytes", description = ["Generates random bytes"])
class GenerateRandomBytes : SelfValidatingCommand() {

    private var numberOfChunks: Int = 1
    private var byteSize: Int = -1
    private lateinit var encoding: BinaryEncoding

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

    override fun Validator.validateSelf() {
        requiresThat(numberOfChunks > 0) {
            """
            Unable to generate `$numberOfChunks` a random sets of bytes: 
            Value must greater than one.
            """.trimIndentToSentence()
        }
        requiresThat(byteSize > 0) {
            """
            Unable to generate random byte set with a byte size of `$byteSize`: 
            Value must greater than one.
            """.trimIndentToSentence()
        }
    }

    override fun performAction() {
        generateRandomBytes()
    }

    private fun generateRandomBytes() {
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
