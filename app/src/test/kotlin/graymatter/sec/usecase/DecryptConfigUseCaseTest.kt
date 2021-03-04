package graymatter.sec.usecase

import graymatter.sec.common.Properties
import graymatter.sec.common.crypto.KeyWithType
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.resourceFile
import graymatter.sec.common.toPropertiesMap
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.ByteArrayOutputStream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DecryptConfigUseCaseTest {


    @Test
    fun testAllPropertiesAreDecrypted() {

        val encryptedPropertiesFile = resourceFile("/samples/encryptedConfig.properties")
        val decryptionKeyWithType = KeyWithType(resourceFile("/keys/test.private"))

        val decryptedPropertiesMap = ByteArrayOutputStream().let { bytesOut ->
            DecryptConfigUseCase(
                keyWithType = decryptionKeyWithType,
                source = encryptedPropertiesFile::inputStream,
                sourceFormat = DocumentFormat.JAVA_PROPERTIES,
                destination = { bytesOut },
                destinationFormat = DocumentFormat.JAVA_PROPERTIES
            ).run()
            Properties(bytesOut).toPropertiesMap()
        }

        decryptedPropertiesMap.forEach { (k, v) ->
            println("$k -> $v")
        }
    }
}


