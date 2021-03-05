package graymatter.sec.usecase

import com.fasterxml.jackson.databind.node.ObjectNode
import com.palantir.config.crypto.KeyWithType
import graymatter.sec.common.*
import graymatter.sec.common.crypto.tryExtractEncryptedContent
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.document.readTree
import io.github.azagniotov.matcher.AntPathMatcher
import org.junit.jupiter.api.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class EncryptConfigurationUseCaseTest {

    private lateinit var unencryptedDocFormat: DocumentFormat
    private lateinit var unencryptedDoc: ObjectNode
    private lateinit var unencrypted: Map<String, String?>
    private lateinit var matcher: AntPathMatcher
    private lateinit var publicKey: File
    private lateinit var unencryptedConfigFile: File
    private lateinit var encryptablePathsExpressions: List<String>
    private lateinit var encrypted: Map<String, String?>

    @BeforeEach
    fun setUp() {
        matcher = AntPathMatcher.Builder().build()
        publicKey = resourceFile<EncryptConfigurationUseCase>("/keys/test")
        unencryptedConfigFile = resourceFile<EncryptConfigurationUseCase>("/samples/unencryptedConfig.yaml")
        unencryptedDocFormat = requireNotNull(DocumentFormat.ofFile(unencryptedConfigFile))
        encryptablePathsExpressions = emptyList()
        unencryptedDoc = unencryptedConfigFile.inputStream().use { it.readTree(unencryptedDocFormat) }
        unencrypted = Properties(unencryptedDoc).toPropertiesMap()
    }

    @Test
    fun testNonTextNodesShouldNotCrashEncryption() {
        givenEncryptablePaths("/magicLinkChallenge/**")
        thenEncrypt()
    }

    @Test
    fun testAllExpectedPathsShouldBeEncrypted() {

        givenExpectedEncryptedPaths()
        thenEncrypt()

        val keyToPathPairs = unencrypted.keys.map { key -> key to key.keyToPath() }.toList()

        fun label(index: Int, expression: String) = "[${index + 1}: $expression]"

        val matches = AntPathMatcher.Builder().build()::isMatch

        assertAll(encryptablePathsExpressions.withIndex().map { (index, expression) ->
            {
                val matchedKey =
                    keyToPathPairs.firstOrNull { (_, path) -> matches(expression, path) }?.first

                val processedValue = encrypted[matchedKey]

                println("${label(index, expression)} -> [${matchedKey?.keyToPath()} -> $processedValue]")

                assertNotNull(matchedKey)
                assertNotNull(processedValue)
                assertNotNull(processedValue.tryExtractEncryptedContent())
            }
        })
    }

    private fun thenEncrypt() {
        encrypted = assertDoesNotThrow {
            Properties().run {
                load(ByteArrayOutputStream().apply {
                    EncryptConfigurationUseCase(
                        openInput = unencryptedConfigFile::inputStream,
                        openOutput = { this },
                        inputFormat = unencryptedDocFormat,
                        keyWithType = KeyWithType.fromString(publicKey.readText()),
                        encryptablePaths = encryptablePathsExpressions,
                        outputFormat = DocumentFormat.JAVA_PROPERTIES
                    ).run()
                })
                toPropertiesMap()
            }
        }
    }

    private fun givenEncryptablePaths(vararg paths: String) {
        encryptablePathsExpressions = paths.toList()
    }

    private fun givenExpectedEncryptedPaths() {
        givenEncryptablePaths(
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
            "/identityEncryptionKeys/*/encoding",
        )
    }
}

private fun String.keyToPath(): String = "/${replace('.', '/')}"
