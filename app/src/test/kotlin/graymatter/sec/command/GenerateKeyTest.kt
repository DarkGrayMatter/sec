package graymatter.sec.command

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

internal class GenerateKeyTest : AbstractCommandTest<GenerateKey>() {

    private val givenKeyName = "test"

    override fun setupCommand(): GenerateKey {
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
        givenCommand.run()
        assertTrue(
            expectedKeyDir.exists(),
            "Expect generate-key command to create keys directory [$expectedKeyDir]"
        )
    }
}
