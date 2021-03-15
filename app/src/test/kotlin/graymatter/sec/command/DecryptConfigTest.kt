package graymatter.sec.command

import com.fasterxml.jackson.databind.node.ObjectNode
import graymatter.sec.App
import graymatter.sec.common.Properties
import graymatter.sec.common.UUID
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.document.jsonOf
import graymatter.sec.common.document.treeOf
import graymatter.sec.common.document.visitNodePathsOf
import graymatter.sec.common.resourceAt
import graymatter.sec.common.toPropertiesMap
import org.junit.jupiter.api.*
import java.io.File
import java.util.*
import kotlin.test.assertTrue

internal class DecryptConfigTest : AbstractCommandTest<DecryptConfig>() {

    override fun setupCommand(): DecryptConfig = DecryptConfig()

    /**
     * This test exercise the requirement of the command to always pick the best
     * intended output format based the user input
     * (as documented in [issue-18](https://github.com/DarkGrayMatter/sec/issues/18#issue-830946442)).
     */
    @Nested
    @DisplayName("Automatically detect output format when decrypting a document.")
    inner class TestAutoDetectingOutputConfigFormat {

        private lateinit var givenEncryptedResource: String
        private lateinit var givenDecryptedCommandLine: MutableList<String>
        private lateinit var givenDecryptionKeyResource: String
        private lateinit var decryptedFileOut: File

        @BeforeEach
        fun initTest() {
            givenEncryptedResource = res("/samples/sample-config.yaml")
            givenDecryptedCommandLine = mutableListOf()
            givenDecryptionKeyResource = res("/keys/test.private")
            decryptedFileOut = file("decrypted-config-${UUID()}")
        }

        @Test
        @DisplayName("If I explicitly set the --format-out value, choose it.")
        fun userExplicitlySetsOutputFormat() {
            givenDecryptedSampleResourceWithFormat(DocumentFormat.YAML)
            whenAddingToCommandLineExplicitFormatOfProps()
            whenRunningCommand()
            thenAssertOutputIsFileIsOfType(DocumentFormat.YAML)
        }

        @Test
        @DisplayName("If I name the output file with a known/valid extension then choose a format based on the output extension.")
        fun userImpliedFormatByNamingTheFileWithExtension() {
            givenDecryptedSampleResourceWithFormat(DocumentFormat.YAML)
            whenRunningCommand()
            thenAssertOutputIsFileIsOfType(DocumentFormat.JAVA_PROPERTIES)
        }

        @Test
        @DisplayName("As a last resort us the input format.")
        fun userDidNotHintAtAnyOutputFormat() {
            whenUserEnterCommandToDecryptSampleYamlConfig()
            whenRunningCommand()
            thenAssertOutputIsFileIsOfType(DocumentFormat.YAML)
        }

        private fun addToCommandLine(vararg args: String) {
            givenDecryptedCommandLine.addAll(args)
        }

        private fun whenUserEnterCommandToDecryptSampleYamlConfig() {
            givenDecryptedSampleResourceWithFormat(DocumentFormat.YAML)
        }

        private fun whenAddingToCommandLineExplicitFormatOfProps() {
            addToCommandLine("--format-out", "java_properties")
        }

        private fun givenDecryptedSampleResourceWithFormat(format: DocumentFormat) {
            givenEncryptedResource = res("/samples/sample-config.${format.defaultFileExtension}")
        }

        private fun thenAssertOutputIsFileIsOfType(format: DocumentFormat) {
            
            assertAll(
                "Expected $decryptedFileOut is of type ${format.name}",
                { assertTrue(decryptedFileOut.length() > 0) },
            )
        }

        private fun whenRunningCommand() {
            addToCommandLine("--file-out", decryptedFileOut.toString())
            addToCommandLine("--res-in", givenEncryptedResource)
            addToCommandLine("--key-res", givenDecryptionKeyResource)
            givenCommandLineOf(*givenDecryptedCommandLine.toTypedArray())
            this@DecryptConfigTest.whenRunningCommand()
        }

        private fun res(path: String) = assertDoesNotThrow("Expected resource: $path") {
            path.also { resourceAt<App>(it) }
        }
    }

}
