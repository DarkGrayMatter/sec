package graymatter.sec.command

import graymatter.sec.App
import graymatter.sec.common.crypto.component1
import graymatter.sec.common.crypto.component2
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.encodeBinary
import graymatter.sec.common.io.IOSource
import graymatter.sec.common.resourceAt
import graymatter.sec.common.file
import graymatter.sec.common.trimToLine
import org.junit.jupiter.api.*
import picocli.CommandLine
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class EncryptConfigTest {

    private lateinit var givenCommandLineArgs: Array<out String>
    private lateinit var cmd: EncryptConfig
    private lateinit var parsed: CommandLine.ParseResult

    private val keyPair = object {
           val private by lazy {
            resourceAt<EncryptConfigTest>("/keys/test").file()
        }
    }

    @BeforeEach
    fun setupTest() {
        cmd = EncryptConfig()
    }

    @Nested
    inner class ConfigSourceInputRequirementTest() {

        @Test
        fun supplySourceInputFromFile() {
            givenCommandLineOf("config.yaml")
            val actual = cmd.input.input.source as IOSource.Input.File
            assertEquals("config.yaml", actual.file.name)
            assertEquals(DocumentFormat.YAML, cmd.input.requestedFormat)
        }

        @Test
        fun sourceInputFromStdInShouldFailIfNoFormatSpecified() {
            givenCommandLineOf("--stdin")
            assertThrows<IllegalArgumentException> { requireNotNull(cmd.input.requestedFormat) }
        }

        @Test
        fun sourceInputFromStdInShouldBe_STDIN() {
            givenCommandLineOf("--stdin")
            assertTrue { cmd.input.input.source.isStdIn }
        }

    }

    @Nested
    inner class KeyRequirementTests {

        @Test
        fun shouldLoadFromKeyFile() {
            givenCommandLineOf("-k", keyPair.private.path)
            assertDoesNotThrow {
                val (key, keyType) = cmd.encryptionKey.keyWithType()
                println("key.type  = ${keyType.name}")
                println("key.bytes = ${key.bytes().encodeBinary()}")
            }
        }

        @Test
        fun shouldLoadFromCommandLine() {
            givenCommandLineOf(
                "--key-text",
                """
                RSA-PUB:MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyNBUFfTEXk76Y4PjaZgKlH
                KqFbREfcJgrZlf9Z7dIRxHVFb5eFo9wwwROjzG97A0tjf6PTxexP9lt/W46VOTED8D7sxzzAPe
                XIVNJLmcW40IA17J1aQwkoVBBVaJ5RzQ+mevVlxjb8htPvEh05uN95HRfkB6NUN9Ha/YnL4vGM
                etvyBW1BhwNian9FfrvFbhCCSIQ8NmKVbFQk+B+1PyX6naXsJd8NXqB19GKUOP4cJz8QhwggGW
                V0jmOayN4+PbuxU0RphoxhGPzmpsorhot2gQMM8c07bedD9H/eQKMqERJoMsCF4Z1O5oYYFd5r
                ywCv5O1QGROZ5O+ZAc4vZrVQIDAQAB
                """.trimToLine()
            )
            assertDoesNotThrow {
                val (key, keyType) = cmd.encryptionKey.keyWithType()
                println("key.type  = ${keyType.name}")
                println("key.bytes = ${key.bytes().encodeBinary()}")
            }
        }

        @Test
        fun shouldLoadFromClassPath() {
            givenCommandLineOf("--key-res", "/keys/test")
            assertDoesNotThrow {
                val (key, keyType) = cmd.encryptionKey.keyWithType()
                println("key.type  = ${keyType.name}")
                println("key.bytes = ${key.bytes().encodeBinary()}")
            }
        }
    }

    @Nested
    inner class EncryptionPathTests {

        @Test
        fun testEncryptionPaths() {
            givenCommandLineOf()
        }
    }


    private fun givenCommandLineOf(vararg args: String) {
        givenCommandLineArgs = args
        parsed = App.createCommandLine(cmd).parseArgs(* givenCommandLineArgs)
    }

}
