package graymatter.sec.usecase

import com.palantir.config.crypto.KeyWithType
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.resourceFile
import graymatter.sec.common.trimIndentToLine
import io.github.azagniotov.matcher.AntPathMatcher
import org.junit.jupiter.api.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class EncryptConfigurationUseCaseTest {

    private lateinit var matcher: AntPathMatcher
    private lateinit var publicKey: File
    private lateinit var unencryptedConfigFile: File
    private lateinit var encryptionPaths: List<String>

    @BeforeEach
    fun setUp() {
        matcher = AntPathMatcher.Builder().build()
        publicKey = resourceFile<EncryptConfigurationUseCase>("/keys/test")
        unencryptedConfigFile = resourceFile<EncryptConfigurationUseCase>("/samples/unencryptedConfig.yaml")
        encryptionPaths = listOf(
            "/postgres/user",
            "/postgres/password",
            "/hashId/salt",
            "/legacyJwt/signingKey",
            "/jwt/signingKey",
            "/event/key",
            "/event/keyAlgorithm",
            "/recaptcha/secret",
            "/mongo/username",
            "/mongo/password",
            "/maskConfiguration/*",
            "/identityEncryptionKeys/*/key",
            "/identityEncryptionKeys/*/encoding"
        )
    }

    @Test
    fun testEncrypt() {
        val encrypted: Map<String, String?> = assertDoesNotThrow {
            Properties().run {
                val output = ByteArrayOutputStream()
                EncryptConfigurationUseCase(
                    openInput = unencryptedConfigFile::inputStream,
                    openOutput = { output },
                    inputFormat = requireNotNull(DocumentFormat.ofFile(unencryptedConfigFile)),
                    keyWithType = KeyWithType.fromString(publicKey.readText()),
                    outputFormat = DocumentFormat.JAVA_PROPERTIES,
                    encryptablePaths = encryptionPaths
                ).run()
                load(ByteArrayInputStream(output.toByteArray()))
                println("Encrypted Document")
                println("------------------")
                forEach { (k, v) -> println("$k = $v") }
                toMap()
            }
        }
        assertExpectedPathsAreEncrypted(encrypted)
    }

    private fun assertExpectedPathsAreEncrypted(encrypted: Map<String, String?>) {

        println(
            """
            *********************************************** 
            * Asserting that required paths are encrypted *
            ***********************************************
            """.trimIndent()
        )

        val pathToKeyValuePair = encrypted.keys.map { k -> propertyKeyToPath(k) to (k to encrypted[k]) }.toMap()

        fun findMatched(pathSelection: String): Pair<String, String?>? {
            val matchedPath = pathToKeyValuePair.keys.firstOrNull { path -> matcher.isMatch(pathSelection, path) }
            return matchedPath?.let(pathToKeyValuePair::get)
        }

        fun labelSpec(i: Int, pathSelection: String) = "[$i:$pathSelection]"

        fun assertPathIsEncrypted(i: Int, pathSelection: String) {

            val (matchedKey, matchedValue) =
                findMatched(pathSelection)
                    ?: fail {
                        "${
                            labelSpec(
                                i,
                                pathSelection
                            )
                        } Expected to encrypt something in doc, but non was found."
                    }


            println("${labelSpec(i, pathSelection)} -> $matchedValue")

            val matchedValueIsEncrypted =
                matchedValue != null && matchedValue.startsWith("{enc:") && matchedValue.endsWith("}")



            assertTrue(
                matchedValueIsEncrypted,
                """
                  Matched [$matchedKey] with ${labelSpec(i, pathSelection)}, but expected encrypted value
                    instead of [$matchedValue]
                """.trimIndentToLine()
            )
        }

        val assertions =
            encryptionPaths.withIndex()
                .map { (i, pathSelection) -> { assertPathIsEncrypted(i, pathSelection) } }

        assertAll("Expected all specified paths to be encrypted.", assertions)
    }

    companion object {

        private fun propertyKeyToPath(key: String): String = "/${key.replace('.', '/')}"

        private fun Properties.toMap(): Map<String, String?> =
            entries.map { (k, v) -> k as String to v as String? }.toMap()
    }
}
