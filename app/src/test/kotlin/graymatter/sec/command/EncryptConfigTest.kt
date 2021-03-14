package graymatter.sec.command

import graymatter.sec.common.Properties
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.resourceFile
import graymatter.sec.common.toPropertiesMap
import org.junit.jupiter.api.*
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
    @Test
    fun outputFormatResolutionTest() {

        var assertions = 0

        fun assertOutputSelected(
            rule: String,
            expectedFormat: DocumentFormat,
            vararg commandLine: String,
        ): () -> Unit = {
            val assertionIndex = ++assertions
            println("----[$assertionIndex -> ${expectedFormat.defaultFileExtension}]------------------------------------------------------------------------------------------------")
            println(rule)
            println("-----------------------------------------------------------------------------------------------------------------")
            print("\n\n")
            givenCommandLineOf(* commandLine)
            whenRunningCommand()
            assertEquals(givenCommand.outputFormat, expectedFormat, " Failed[$assertionIndex] : $rule")
        }

        assertAll(
            assertOutputSelected(
                rule = "If I explicitly set the `--format-out` value, choose it.",
                expectedFormat = DocumentFormat.JAVA_PROPERTIES,
                "--format-out", "java_properties",
                "--key-res", "/keys/test",
                "--res-in", "/samples/sample-config.yaml"
            ),
            assertOutputSelected(
                rule = "If I name the output file with a known/valid extension then choose a format based on the output extension.",
                expectedFormat = DocumentFormat.JSON,
                "--key-res", "/keys/test",
                "--res-in", "/samples/sample-config.yaml",
                "--file-out", "${File(givenWorkingDir, "config.json")}"
            ),
            assertOutputSelected(
                rule = "As a last resort us the input format",
                expectedFormat = DocumentFormat.YAML,
                "--key-res", "/keys/test",
                "--res-in", "/samples/sample-config.yaml",
                "--stdout"
            ),
        )

    }
}
