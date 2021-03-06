package graymatter.sec.common.crypto

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CryptoServicesKtTest {

    @Test
    fun testTryToExtractEncryptedText() {

        val nodeTextToExpectedPartList = listOf(

            "\${enc:eyJ0eXBlIjoiUlNBIiwibW9kZSI6Ik9BRVAiLCJjaXBoZXJ0ZXh0IjoiYjR4TWM2di9NeWtpd1NBNzdpa2hZZHlnZHpvc1VMZmlPZFl0eEpBUXFIdTlxaTN1eXYwTm9QQXJ2b0hnY2hTVDR0b2ZQRnljWFREczBhb3RFa3VEei81QjJxUE43RnJsVHhLRmdYbVV5T0dSUGVtQUd4QWVYTUR3R3E4b3E2UEFzMnY2ODZPRktYQzF3N3pxR1c5Q3VjQW5TNlhRYXQ3TlRVZ1lOQzEwNFpUVFIzNHBBbVlQVmpmWE5ka1VOQWNFd2FJRmxWcDVLSVFpcno2d3h2dEZhMzhqdTdOZkdVeXNHRzdMYVhUQXRXVjI3YzJldDg2dDMwQmQ1dmZPQkZNUUkvalJUZ2JKcHVza3pJNWZSUzkzSHVzWU1YOW1nRGczbTRnc0FvK2pZNnNvbHdOTk56cHlQZlhjU2hTdzlTajl5QzJmK0JoK1F5bjBldWhSdlJNSFV3PT0iLCJvYWVwLWFsZyI6IlNIQS0yNTYiLCJtZGYxLWFsZyI6IlNIQS0yNTYifQ==}"
                    to "enc:eyJ0eXBlIjoiUlNBIiwibW9kZSI6Ik9BRVAiLCJjaXBoZXJ0ZXh0IjoiYjR4TWM2di9NeWtpd1NBNzdpa2hZZHlnZHpvc1VMZmlPZFl0eEpBUXFIdTlxaTN1eXYwTm9QQXJ2b0hnY2hTVDR0b2ZQRnljWFREczBhb3RFa3VEei81QjJxUE43RnJsVHhLRmdYbVV5T0dSUGVtQUd4QWVYTUR3R3E4b3E2UEFzMnY2ODZPRktYQzF3N3pxR1c5Q3VjQW5TNlhRYXQ3TlRVZ1lOQzEwNFpUVFIzNHBBbVlQVmpmWE5ka1VOQWNFd2FJRmxWcDVLSVFpcno2d3h2dEZhMzhqdTdOZkdVeXNHRzdMYVhUQXRXVjI3YzJldDg2dDMwQmQ1dmZPQkZNUUkvalJUZ2JKcHVza3pJNWZSUzkzSHVzWU1YOW1nRGczbTRnc0FvK2pZNnNvbHdOTk56cHlQZlhjU2hTdzlTajl5QzJmK0JoK1F5bjBldWhSdlJNSFV3PT0iLCJvYWVwLWFsZyI6IlNIQS0yNTYiLCJtZGYxLWFsZyI6IlNIQS0yNTYifQ==",

            "abc-chde-hsjsjsj"
                    to null,

            "\${enc:decode-me-please"
                    to null
        )

        assertAll(nodeTextToExpectedPartList.mapIndexed { index, (
            nodeText,
            expectedEncodedPart) ->
            {
                val actualEncodedPart = nodeText.tryExtractEncryptedContent()
                assertEquals(expectedEncodedPart, actualEncodedPart, "[${index + 1}] <$nodeText>")
            }
        })
    }


}
