package graymatter.sec.command

import graymatter.sec.common.Properties
import graymatter.sec.common.resourceFile
import graymatter.sec.common.toPropertiesMap
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import picocli.CommandLine
import java.io.File
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class EncryptConfigTest : AbstractCommandTest<EncryptConfig>() {

    private lateinit var givenUnencryptedProperties: Properties
    private lateinit var givenEncryptionKeyFile: File
    private lateinit var givenUnencryptedPropertiesFile: File

    override fun setupCommand(): EncryptConfig {
        return EncryptConfig()
    }

    @BeforeEach
    override fun setUp() {
        super.setUp()
        givenUnencryptedPropertiesFile = resourceFile("/samples/sample-config.properties")
        givenUnencryptedProperties = Properties(givenUnencryptedPropertiesFile)
        givenEncryptionKeyFile = resourceFile("/keys/test")
    }

    @Test
    fun encryptedFileShouldOutputToDirWhenSpecified() {
        val expectedEncryptedPropertiesFile = givenCommandToEncryptToFile()
        whenRunningCommand()
        thenAssertEncryptedPropertiesFileProvidedKeys(expectedEncryptedPropertiesFile)
    }


    private fun whenRunningCommand() {
        givenCommand.run()
    }

    private fun givenCommandToEncryptToFile(): File {
        val fileOut = File(givenWorkingDir, "encrypted.properties")
        givenCommandLineOf(
            "--file", "$givenUnencryptedPropertiesFile",
            "--key", "$givenEncryptionKeyFile",
            "--file-out", "$fileOut"
        )
        return fileOut
    }

    private fun thenAssertEncryptedPropertiesFileProvidedKeys(encryptedPropertiesFile: File) {

        fun expectEncryptionFileTo(expectation: String): String {
            return "Expect encrypted file ($encryptedPropertiesFile), to $expectation"
        }

        assertTrue(expectEncryptionFileTo("exists"), encryptedPropertiesFile::exists)
        assertTrue(expectEncryptionFileTo("be not empty")) { encryptedPropertiesFile.length() > 0 }

        val keysToProcess = givenUnencryptedProperties
            .toPropertiesMap().keys.sorted()

        val actualKeysProcessed = Properties(encryptedPropertiesFile)
            .toPropertiesMap().keys.sorted()

        assertEquals(
            keysToProcess, actualKeysProcessed,
            expectEncryptionFileTo(
                "contain all the keys in the given properties file ($givenUnencryptedPropertiesFile)"))
    }

}
