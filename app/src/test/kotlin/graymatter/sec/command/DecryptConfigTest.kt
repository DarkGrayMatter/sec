package graymatter.sec.command

import graymatter.sec.common.UUID
import graymatter.sec.common.add
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.io.assertFileHasContentOf
import graymatter.sec.common.resourceFile
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

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

        private val encryptedConfigFile: File = resourceFile("/samples/sample-config.properties")
        private val decryptionKeyFile: File = resourceFile("/samples/sample-config.properties")
        private lateinit var commandLineArgs: MutableList<String>
        private lateinit var decryptedContentFileName: String
        private lateinit var fileOut: File

        @BeforeEach
        fun prepareTest() {
            decryptedContentFileName = "decrypted-content-${UUID().toString().replace("-","")}"
            commandLineArgs = mutableListOf()
        }

        @Test
        @DisplayName("If I explicitly set the --format-out value, choose it.")
        fun userExplicitlySetsOutputFormat() {
            commandLineArgs.add("--format-out", "json")
            whenDecrypting()
            thenDecryptedFileContainsJsonConfiguration()
        }

        private fun thenDecryptedFileContainsJsonConfiguration() {
            fileOut.assertFileHasContentOf(DocumentFormat.JSON)
        }
/*
        @Test
        @DisplayName("If I name the output file with a known/valid extension then choose a format based on the output extension.")
        fun userImpliedFormatByNamingTheFileWithExtension() {
        }

        @Test
        @DisplayName("As a last resort us the input format.")
        fun userDidNotHintAtAnyOutputFormat() {
        }*/

        private fun whenDecrypting() {

            println("""
                +--------------------------------------------------->
                 \   DECRYPTING - $encryptedConfigFile
                  \-------------------------------------------------->
                  
            """.trimIndent())

            fileOut = file(decryptedContentFileName)
            givenCommandLineOf(
                *commandLineArgs.apply {
                    add("--file-in", encryptedConfigFile.toString())
                    add("--key", decryptionKeyFile.toString())
                    add("--file-out", fileOut.toString())
                }.toTypedArray()
            )
            whenRunningCommand()

        }
    }



}
