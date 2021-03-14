package graymatter.sec.command

import graymatter.sec.common.Properties
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.resourceFile
import graymatter.sec.common.toPropertiesMap
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
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
    @ParameterizedTest
    @MethodSource("outputFormatResolutionTestParameters")
    fun outputFormatResolutionTest(
        useCaseRule: String,
        expectedFormat: DocumentFormat,
        givenCommandLine: Array<String>,
    ) {
        givenCommandLineOf(*givenCommandLine)
        val selectedOutputFormat = givenCommand.selectedDefaultOutputFormat
        assertEquals(expectedFormat, selectedOutputFormat, "Fail on rule: $useCaseRule")
    }


    @MethodSource
    fun outputFormatResolutionTestParameters(): List<List<Any>> {
        val inputSelection = arrayOf("--key-res", "/keys/test", "--file-out")
        return listOf(
            listOf(
                "If I explicitly set the `--format-out` value, choose it.",
                DocumentFormat.JAVA_PROPERTIES,
                inputSelection + arrayOf("--format-out", "java_properties", "--stdout")
            ),
            listOf(
                "If I name the output file with a known/valid extension then choose a format based on the output extension.",
                DocumentFormat.JSON,
                inputSelection + arrayOf("file-out", "${File(givenWorkingDir, "encrypted-file-out.json")}")
            ),
            listOf(
                "As a last resort us the input format",
                DocumentFormat.YAML,
                inputSelection + arrayOf("--stdout")
            ),
        )
    }
}
