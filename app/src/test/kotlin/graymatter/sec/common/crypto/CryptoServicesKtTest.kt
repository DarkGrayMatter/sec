package graymatter.sec.common.crypto

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CryptoServicesKtTest {

    @Test
    fun testTryToExtractEncryptedText() {

        val testData: List<Pair<String, String?>> = listOf(
            "\${enc:}" to null,
            "\${enc:this-is-encoded-content}" to "this-is-encoded-content",
            "{enc:}" to null
        )

        assertAll(testData.map { (possibleEncodedContent, expectedEncodedContent) ->
            {
                val actualEncodedContent = possibleEncodedContent.tryExtractEncryptedContent()
                assertEquals(expectedEncodedContent, actualEncodedContent)
            }
        })
    }


}
