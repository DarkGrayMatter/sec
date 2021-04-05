package graymatter.sec.command

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

internal class GenerateKeyTest : CommandTest<GenerateKey>() {

    private val givenKeyName = "test"

    override fun newCommand(): GenerateKey {
        return GenerateKey().apply {
            setDestination(givenWorkingDir)
            setKeyName(givenKeyName)
        }
    }

    @Test
    fun byDefaultDestinationPathShouldBeCreated() {
        val expectedKeyDir = File(givenWorkingDir, "keys")
        cliArgs(
            "--alg", "rsa",
            "--key", givenKeyName,
            "$expectedKeyDir"
        )
        whenRunningCommand()
        assertTrue(
            expectedKeyDir.exists(),
            "Expect generate-key command to create keys directory [$expectedKeyDir]"
        )
    }
}
