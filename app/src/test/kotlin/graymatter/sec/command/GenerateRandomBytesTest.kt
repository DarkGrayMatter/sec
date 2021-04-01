package graymatter.sec.command

import com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import picocli.CommandLine.MissingParameterException
import kotlin.test.assertEquals

internal class GenerateRandomBytesTest : AbstractCommandTest<GenerateRandomBytes>() {

    private var seedEncoding: String? = null
    private var seed: String? = null
    private var prefixOfRandom: String? = null
    private var repeatRandom: Int? = null
    private var byteSizeOfRandom: Int? = null
    private lateinit var encoding: String
    private lateinit var capturedOutput: List<String>

    override fun setupCommand(): GenerateRandomBytes {
        return GenerateRandomBytes()
    }


    @BeforeEach
    fun setupCommandDefaults() {
        this.byteSizeOfRandom = null
        this.repeatRandom = null
        this.encoding = "hex"
        this.prefixOfRandom = null
        this.seedEncoding = null
        this.seed = null
        this.capturedOutput = emptyList()
    }

    @Test
    @DisplayName("Empty command line should fail with missing parameter exception")
    fun commandWithNoArgsShouldFailWithMissingParamException() {
        assertThrows<MissingParameterException> { whenRunningCommand() }
    }

    @Test
    @DisplayName("A user should be able to request 1 random numbers in one command")
    fun repeatGenerateBytes() {
        repeatRandom = 1
        byteSizeOfRandom = 10
        whenRunningCommand()
        assertThat(capturedOutput).isNotEmpty
        assertEquals(2, capturedOutput.take(10).size)
    }


    override fun whenRunningCommand() {

        cliArgs("-e", encoding)
        repeatRandom?.also { v -> cliArgs("-n", "$v") }
        prefixOfRandom?.also { v -> cliArgs("--prefix", v) }
        byteSizeOfRandom?.also { v -> cliArgs("-b", "$v") }
        seedEncoding?.also { v -> cliArgs("--seed-encoding", v) }
        seed?.also { v -> cliArgs("--seed", v) }

        capturedOutput =
            tapSystemOut { super.whenRunningCommand() }
                .lines().dropLastWhile { line -> line.trim().isEmpty() }

        println("capturedOutputLines (including header): ${capturedOutput.size}")
        capturedOutput.forEach(::println)
        capturedOutput = capturedOutput.drop(3)
    }


}
