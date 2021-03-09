package graymatter.sec.usecase

import com.palantir.config.crypto.KeyWithType
import graymatter.sec.common.crypto.KeyWithType
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.document.DocumentMapper
import graymatter.sec.common.resourceFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DecryptConfigUseCaseTest {

    private lateinit var publicKeyWithType: KeyWithType
    private lateinit var privateKeyWithType: KeyWithType

    @BeforeAll
    fun setUp() {
        publicKeyWithType = KeyWithType(resourceFile("/keys/test"))
        privateKeyWithType = KeyWithType(resourceFile("/keys/test.private"))
    }

    @Test
    fun testEncryptDecryptCycle() {

        @Language("yaml") val unencryptedDoc = """
        ---
        jdbc:
          driver: "org.h2.Driver"
          url: "jdbc:h2:mem:testDb"
          password: doNotNeedToSeeThis
          user: sa
        """.trimIndent()
        val unencryptedFormat = DocumentFormat.YAML

        val encryptedFormat = DocumentFormat.JSON
        val encrypted = ByteArrayOutputStream().run {
            EncryptConfigUseCase(
                openInput = unencryptedDoc::byteInputStream,
                openOutput = { this },
                inputFormat = unencryptedFormat,
                outputFormat = encryptedFormat,
                keyWithType = publicKeyWithType,
                encryptedPaths = listOf("/jdbc/password", "/jdbc/user")
            ).run()
            String(toByteArray())
        }

        println("Encrypted")
        println("--------------------")
        println(encrypted)

        val decrypted = ByteArrayOutputStream().run {
            DecryptConfigUseCase(
                keyWithType = privateKeyWithType,
                source = encrypted::byteInputStream,
                sourceFormat = encryptedFormat,
                destination = {this},
                destinationFormat = unencryptedFormat
            ).run()
            String(toByteArray())
        }

        println("Decrypted")
        println("---------------------------")
        println()
        println(decrypted)

        val expected = DocumentMapper.of(unencryptedFormat).readTree(unencryptedDoc)
        val actual = DocumentMapper.of(unencryptedFormat).readTree(decrypted)

        assertEquals(expected, actual)
    }

}


