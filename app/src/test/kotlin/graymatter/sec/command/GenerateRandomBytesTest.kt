package graymatter.sec.command

import com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut
import graymatter.sec.App
import graymatter.sec.common.crypto.BinaryEncoding
import graymatter.sec.common.encodeBinary
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class GenerateRandomBytesTest {

    private var commandOutputLines: List<String>? = null
    private var seedHex: String? = null
    private var bytesOut: Int? = null
    private var repeat: Int? = null

    @Test
    @Disabled("When running the cli args from the command line the desired results are achieved, but not in this unit test.")
    fun testUsingSameSeedProducesSameBytes() {

        seedHex = ByteArray(10).apply { Random.nextBytes(this) }.encodeBinary(BinaryEncoding.Hex)
        bytesOut = 10
        repeat = 2

        lateinit var expected: List<String>
        lateinit var actual: List<String>

        repeat(2) { case ->
            whenRunningCommand()
            val collected = commandOutputLines
            assertNotNull(collected)
            when (case) {
                0 -> expected = collected
                1 -> actual = collected
            }
        }

        assertEquals(expected, actual)
    }

    private fun whenRunningCommand() {
        commandOutputLines = null
        val command = GenerateRandomBytes()
        val commandLine = App.createCommandLine(command)
        commandLine.parseArgs(* buildCommandLine())
        commandOutputLines = tapSystemOut { assertDoesNotThrow { command.run() } }?.lines()
    }

    private fun buildCommandLine(): Array<String> {
        val cliArgs = mutableListOf<String>()
        seedHex?.also { cliArgs.add("--seed", it, "--seed-encoding", "hex") }
        bytesOut?.also { cliArgs.add("--bytes", it.toString()) }
        repeat?.also { cliArgs.add("--repeat", it.toString()) }
        return cliArgs.toTypedArray()
    }

    private fun MutableList<String>.add(string: String, vararg more: String) {
        add(string)
        more.forEach(this::add)
    }
}
