package graymatter.sec.command

import graymatter.sec.command.reuse.group.OutputTargetProvider
import graymatter.sec.command.reuse.group.SeedProvider
import graymatter.sec.common.cli.SelfValidatingCommand
import graymatter.sec.common.crypto.BinaryEncoding
import graymatter.sec.common.encodeBinary
import graymatter.sec.common.validation.Validator
import picocli.CommandLine.*
import java.io.PrintWriter
import java.security.SecureRandom

@Command(name = "generate-random-bytes")
class GenerateRandomBytes : SelfValidatingCommand() {

    var enabled = true
        private set

    @Option(
        names = ["-b", "--bytes"],
        description = ["Size of random bytes to generate."],
        required = true
    )
    var numberOfBytes: Int? = null

    @Option(
        names = ["-n", "--repeat"],
        defaultValue = "1",
        description = ["How may times to generate the random bytes."]
    )
    var repeatNumberOfTimes: Int = 1

    @Option(
        names = ["--prefix"],
        description = ["Prefix each random encoded bytes with this value."],
        required = false
    )
    var prefix: String? = null

    @Option(
        names = ["-e", "--encoding"],
        description = ["How the bytes should be encoded to string."],
        showDefaultValue = Help.Visibility.ALWAYS,
        defaultValue = "hex"
    )
    var encoding: BinaryEncoding = BinaryEncoding.Hex

    @ArgGroup(order = 0, heading = "Use these options to specify a seed value for pseudo random numbers:%n")
    var seedProvider: SeedProvider? = null

    @ArgGroup(order = 1, heading = "Use these options to control where the random bytes should be written to:%n")
    var outputTarget: OutputTargetProvider = OutputTargetProvider()

    override fun Validator.validateSelf() = Unit

    override fun applyDefaults() {
        outputTarget.takeUnless { it.isAvailable }?.setOutputToStdOut()
    }

    override fun performAction() {

        if (!enabled) {
            return
        }

        val g = makeGenerator()
        val bytes = ByteArray(numberOfBytes!!)

        PrintWriter(outputTarget.openOutputStream()).use { out ->
            repeat(repeatNumberOfTimes) {
                val generatedBytes = g(bytes)
                when (prefix) {
                    null -> out.println(generatedBytes)
                    else -> out.println("$prefix$generatedBytes")
                }
            }
        }

        enabled = false

    }

    fun enableAgain() {
        enabled = false
    }

    private fun makeGenerator(): (ByteArray) -> String {

        val generator = when (val seed = seedProvider?.seed()) {
            null -> SecureRandom()
            else -> SecureRandom(seed)
        }

        return { bytes: ByteArray ->
            generator.nextBytes(bytes)
            bytes.encodeBinary(encoding)
        }
    }

}
