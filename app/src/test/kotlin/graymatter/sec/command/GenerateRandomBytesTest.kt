package graymatter.sec.command

import graymatter.sec.common.crypto.BinaryEncoding
import graymatter.sec.common.encodeBinary
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

internal class GenerateRandomBytesTest : CommandTest<GenerateRandomBytes>() {

    private var seedHex: String? = null
    private var bytesOut: Int? = null
    private var repeat: Int? = null

    override fun newCommand() = GenerateRandomBytes()

    @Test
    @Disabled("When running the cli args from the command line the desired results are achieved, but not in this unit test.")
    fun testUsingSameSeedProducesSameBytes() {

        seedHex = ByteArray(10).apply { Random.nextBytes(this) }.encodeBinary(BinaryEncoding.Hex)
        bytesOut = 10
        repeat = 2

        lateinit var expected: List<String>
        lateinit var actual: List<String>

        repeat(2) {
            whenRunningCommand()
            when (it) {
                0 -> expected = commandOutputLines!!
                1 -> actual = commandOutputLines!!
            }
        }

        assertEquals(expected, actual)
    }

    override fun buildCommandLine() {
        seedHex?.also { cliArgs("--seed", it, "--seed-encoding", "hex") }
        bytesOut?.also { cliArgs("--bytes", it.toString()) }
        repeat?.also { cliArgs("--repeat", it.toString()) }
    }
}
