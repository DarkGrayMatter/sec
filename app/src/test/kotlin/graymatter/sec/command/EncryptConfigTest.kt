package graymatter.sec.command

import graymatter.sec.App
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.io.IOSource
import org.junit.jupiter.api.*
import picocli.CommandLine
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class EncryptConfigTest {

    private lateinit var givenCommandLineArgs: Array<out String>
    private lateinit var cmd: EncryptConfig
    private lateinit var parsed: CommandLine.ParseResult

    @BeforeEach
    fun setupTest() {
        cmd = EncryptConfig()
    }

    @Nested
    inner class ConfigSourceInputRequirementTest() {

        @Test
        fun supplySourceInputFromFile() {
            givenCommandLineOf("config.yaml")
            val actual = cmd.sourceConfig.input.source as  IOSource.Input.File
            assertEquals("config.yaml", actual.file.name)
            assertEquals(DocumentFormat.YAML, cmd.sourceConfig.requestedFormat())
        }

        @Test
        fun sourceInputFromStdInShouldFailIfNoFormatSpecified() {
            givenCommandLineOf("--stdin")
            assertThrows<IllegalArgumentException> { requireNotNull(cmd.sourceConfig.requestedFormat()) }
        }

        @Test
        fun sourceInputFromStdInShouldBe_STDIN() {
            givenCommandLineOf("--stdin")
            assertTrue { cmd.sourceConfig.input.source.isStdIn }
        }

    }

    @Nested
    inner class KeyRequirementTests {

        @Test
        fun shouldBeAbleToLoadKeyFromFile() {
            givenCommandLineOf("-k", "")
        }

    }

    private fun givenCommandLineOf(vararg args: String) {
        givenCommandLineArgs = args
        parsed = App.createCommandLine(cmd).parseArgs(* givenCommandLineArgs)
    }

}
