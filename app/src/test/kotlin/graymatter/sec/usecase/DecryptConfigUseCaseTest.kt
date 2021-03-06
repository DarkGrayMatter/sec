package graymatter.sec.usecase

import graymatter.sec.common.Properties
import graymatter.sec.common.crypto.KeyWithType
import graymatter.sec.common.crypto.tryExtractEncryptedContent
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.resourceFile
import graymatter.sec.common.toPropertiesMap
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DecryptConfigUseCaseTest {

    @Test
    fun testAllPropertiesAreDecrypted() {

    }
}


