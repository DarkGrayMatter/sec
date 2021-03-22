package graymatter.sec.command

import com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut
import graymatter.sec.common.Properties
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.io.assertFileHasContentOf
import graymatter.sec.common.resourceFile
import graymatter.sec.common.toPropertiesMap
import org.junit.jupiter.api.*
import picocli.CommandLine
import picocli.CommandLine.ParameterException
import java.io.File
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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

    @Test
    fun alwaysDefaultToStdOutIfUserHasNotSelectedExplicitOutput() {
        cliArgs("--key", givenEncryptionKeyFile.toString())
        cliArgs("--file-in", givenUnencryptedPropertiesFile.toString())
        val out = tapSystemOut { whenRunningCommand() }.also { println(it) }
        assertNotNull(out)
        assertTrue(out.isNotEmpty())
    }


    private fun givenCommandToEncryptToFile(): File {
        val fileOut = file("encrypted.properties")
        cliArgs(
            "--file-in", "$givenUnencryptedPropertiesFile",
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

    @Test
    fun commandWithNoArgsShouldFailOnValidation() {
        val expected = assertThrows<ParameterException> { whenRunningCommand() }
        println(expected.message)
        assertTrue(expected.message?.contains(
            "For your assistance, please consult the usage below",
            ignoreCase = true) == true)
    }


    /**
     * This test asserts that output format based on [issue-#17](https://github.com/DarkGrayMatter/sec/issues/17#issue-830945985)
     * which attempts to choose reasonable defaults based in the absence of explicit defaults.
     *
     * To quote the user story:
     *
     * As a User I want the tool to detect the output format based on (in order of precedence):
     *
     * 1. If I explicitly set the `--format-out` value, choose it.
     * 2. If I name the output file with a known/valid extension then choose a format based on the output extension.
     * 3. As a last resort us the input format.
     */
    @Nested
    @DisplayName("Smart handling of output format based on user input")
    inner class TestSmartHandlingOfOutputHandling {

        private val yamlConfigFile = resourceFile("/samples/sample-config.yaml")
        private lateinit var fileOutName: String
        private lateinit var fileOut: File

        @BeforeEach
        fun setUp() {
            fileOutName = "encrypted-config"
        }

        @Test
        @DisplayName("If I explicitly set the `--format-out` value, choose it.")
        fun userSelectExplicitFormatOut() {
            givenFileOutFormatAsJson()
            whenEncrypting()
            thenAssertFileOutContainsJson()
        }

        @Test
        @DisplayName("If I name the output file with a known/valid extension then choose a format based on the output extension.")
        fun userSuppliedFileNameWithKnownExtension() {
            givenFileHashJsonExtension()
            whenEncrypting()
            thenAssertFileOutContainsJson()
        }

        @Test
        @DisplayName("As a last resort to the input format.")
        fun userNotSetAnyOutputFormat() {
            whenEncrypting()
            thenAssertFileOutContainsYaml()
        }

        private fun thenAssertFileOutContainsJson() {
            assertFileHasContentOf(DocumentFormat.JSON, fileOut)
        }

        private fun thenAssertFileOutContainsYaml() {
            assertFileHasContentOf(DocumentFormat.YAML, fileOut)
        }

        private fun givenFileOutFormatAsJson() {
            cliArgs("--format-out", "json")
        }

        private fun givenFileHashJsonExtension() {
            fileOutName += ".${DocumentFormat.JSON.defaultFileExtension}"
        }

        private fun whenEncrypting() {
            fileOut = file(fileOutName)
            cliArgs("--key", givenEncryptionKeyFile.toString())
            cliArgs("--file-in", yamlConfigFile.toString())
            cliArgs("--file-out", fileOut.toString())
            whenRunningCommand()
        }
    }
}
