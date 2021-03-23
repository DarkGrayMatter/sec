package graymatter.sec.command

import graymatter.sec.command.reuse.group.OutputTargetProvider
import graymatter.sec.command.reuse.mixin.GivenSeed
import graymatter.sec.common.cli.SelfValidatingCommand
import graymatter.sec.common.crypto.BinaryEncoding
import graymatter.sec.common.encodeBinary
import graymatter.sec.common.trimIndentToSentence
import graymatter.sec.common.validation.Validator
import graymatter.sec.common.validation.requiresThat
import picocli.CommandLine.*
import java.io.PrintStream
import java.security.SecureRandom

@Command(name = "generate-bytes", description = ["Generates random bytes"])
class GenerateRandomBytes : SelfValidatingCommand() {

    @ArgGroup
    val output: OutputTargetProvider = OutputTargetProvider()

    @Option(names = ["--enc"], description = ["Binary text encoding."], defaultValue = "base64")
    lateinit var encoding: BinaryEncoding

    @Option(names = ["--chunks"], description = ["Number of chunks of random bytes."])
    var numberOfChunks: Int = 1

    @Option(names = ["-n", "--bytes"], description = ["How many bytes random bytes to generate."])
    var numberOfBytes: Int = -1

    @Option(names = ["--format"], description = ["Print format (defaults to \"%s\")"])
    var printFormat: String = "%s"

    @Mixin
    var givenSeed: GivenSeed? = null

    override fun Validator.validateSelf() {
        requiresThat(numberOfBytes > 0) {
            """
                Number of bytes must be greater than zero (instead of $numberOfBytes)).
            """.trimIndentToSentence()
        }
        requiresThat(numberOfChunks > 0) {
            """
                Please set the number of chunks to greater than zero (instead of $numberOfChunks). 
            """.trimIndentToSentence()
        }
        validate {
            val expected = "[expected]"
            runCatching { printFormat.format(expected) }.apply {
                onSuccess {
                    if (expected !in it) {
                        failed("Invalid print format supplied: $printFormat")
                    }
                }
                onFailure {
                    failed("Invalid print format supplied: $printFormat", it)
                }
            }
        }
    }

    override fun performAction() {
        synchronized(this) {
            if (!isExecutedOnce) {
                generateByteChunks()
                isExecutedOnce = true
            }
        }

        val generateBytes: () -> String = SecureRandom().let { rnd ->
            {
                val bytes = ByteArray(numberOfBytes)
                rnd.nextBytes(bytes)
                encoding.encode(bytes)
            }
        }
        println(generateBytes())
    }

    private fun generateByteChunks() {

        val rng = when (val seed = givenSeed?.asBytes()) {
            null -> SecureRandom()
            else -> SecureRandom(seed)
        }

        fun nextBytes() = ByteArray(numberOfBytes).apply { rng.nextBytes(this) }

        PrintStream(output.openOutputStream()).use { out ->
            repeat(numberOfBytes) {
                out.println(nextBytes().encodeBinary(encoding))
            }
        }
    }

    override fun applyDefaults() {
        output.setOutputToStdOut()
    }

    companion object {
        //todo Need this flag to work around bug
        private var isExecutedOnce = false
    }
}
