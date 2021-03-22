package graymatter.sec.command.reuse.validation

import graymatter.sec.command.reuse.group.KeyProvider
import graymatter.sec.common.OrderedId
import graymatter.sec.common.validation.Validator
import org.junit.jupiter.api.*
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("SameParameterValue")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Testing shared validation strategies")
internal class ValidationsKtTest {

    private lateinit var workDir: File
    private lateinit var validator: Validator

    @BeforeEach
    fun prepareTest(@TempDir tempDir: File) {
        validator = Validator()
        workDir = tempDir
    }

    @Nested
    @DisplayName("Key Provider Validation Tests")
    inner class TestKeyProviderValidation {

        private lateinit var keyProvider: KeyProvider
        private var givenKeyNotSetMessage = "key not set"
        private var givenKeyNotLoadingPreamble = "unable to load key"

        @BeforeEach
        fun prepareTest() {
            keyProvider = KeyProvider()
        }

        @Test
        @DisplayName("Key validation should fail if the user has not provide any key")
        fun keyNotAvailableShouldFailValidation() {

            // Given
            // - implied for all tests

            // When
            whenValidatingKeyProvider()

            // Then
            assertFalse(keyProvider.isAvailable)
            assertThatHasValidationErrors(1)
            assertEquals(listOf(givenKeyNotSetMessage), actualMessages())
        }

        @Test
        @DisplayName("Key validation should fail when the user selects a key on the classpath which does not exists.")
        fun keyValidationShouldCaterForMissingKeyOnClassPath() {

            //Given:
            keyProvider.setKeyFileFromClassPath("/non-existing-key-${OrderedId()}")

            //When:
            whenValidatingKeyProvider()

            //Then:
            assertThatHasValidationErrors(1)
            assertTrue(actualMessages().first().startsWith(givenKeyNotLoadingPreamble))
            assertTrue("Expected validation to be triggered by an IOException") {
                validator.failures().first().cause is IOException
            }
        }

        @ParameterizedTest(name = "Bad key : {0}")
        @ValueSource(strings = [
            "this is bad key",
            "RSA-PRIV:not a key",
            "RSA-PRIV:fpKCaZBWhOkLqe3VWBxQ4sxn4g+v5khaFOdIDYOMq3BNWib7m683ksvFjJc/sJb9ADWq6WGhsi6Mvb4UXN7RvKxph49Fo9h6/rDNQNlmvn7m2QMUUyWulfe7IB/tJVhzG28Zpb8GjQQIyOklkUNjyFk8deqJHh4fK7eRgZXaDJPykVvJ9nj2xqCJUuP51dLereWqY5FOXUjCSVjzY7+qDIDTGwZhm8YSuseRg2v7LZVJ2//sblPrxEgzUUHdZ55EFKyPKzdNkxSnHFrCEd1K+CguZqKs7YXYXQKdzZjTyTJYJlk3v4bVJOiJ3d7MvBrHGkSBMdaFPxy4jYUQaQzN46/iOFEEfzrrhrTOHA2XDbzTt3NcuHZmofS+2DgjIRsqkArodCOXbhmBnw82M7ONUnW0d/UNMeF/uIExh6gSv5cjIez1mgLz5DNPC+Dj/HowYY6UIOr4IcyklvEctxGwYB4I4NReYEG7VT8m31EIBdfX1LHaCxSLv6tpotVDNeX225nD6LO8mP+Eo+y8Z9qpO06YyhySqHirs+/8hHMDzkqnrVXkOEfmWfetUYNIznMfx5P0C8//Hm14CkGQ/Es0Io8uo3wG1otg8dXqHRQFXTGwuruQpDMl/M3d0Vfj0JFSpgIiwQJL3K6TR7mo3ru28e+QhrSsUZu2OSXZdwhtd7oOkThF7ZhmZcbKo9kduUKNR8saxuwwWI6YFMy3wq0lfsxQrPoAshmUbWW7b5S17Pv2GFrLCEl908yWylYdLjwtnsv0td2+HhXs3Pf7YwiX5b749pThBsrpOCgi3AH0lwH2akB/JJ8dNtSeZKy4tkWniEIGdse1est1ldB0Vp9sew8BjGv5vWGx9kQG1zarrW0ZlzGsXEqaqf0POjHq1YjSYIrfJgrPHYvrJiXH631htx63qAKxHM9fInF4AGqsMkCl4koiFP4UurYL8BEBzr+U47M2ZVhDrhENUL6KeHSOcXbFcqL+A3cQmT3mPaEs1hwPgHd4O3aDrfY4PbCJsubVHnGr3tki8YCe4JmqgZ+BxvoJkSZ4o5tQoQT7rWd71c5uFZdB+Nf+AxdNQEPaTE1CoAjVTnT1EiXVxZG9E3xS2zSOIgpHXu4m2UMvLUvgjIRordUtFGTU+RNFURH5LtXw+WqJf78TYK6vW9T9D41iFXQuxS3xawe4vLlhbOgvN7CqPj91d563jI5GQl8fNkq8r2vuL9Jx9xeZszT3lqMZw+h8m2YB0yZNhYoBaEO6nQ/tD40VXUzMX9992DRjPm/3m7cc9Aorw5B/eBtr15J1XX5BVzkJIQU6w4nCe7uRCkTbMqKVhUt+2mq2vvyj1D/ZPnFAcoyyXba8n4SdfA/ShbbSr64oejwGmGK57oFE0Dho4OUeS4mB0v+hLUxUsHl2JnS3aOC+eF3ZxtqVMhcd5PGDk7IYl1LosGcciMq2HbnrBaX+sm93O6H0ebc3a+EFJdD8RPgT651bPkc9y+mAchWkpJzj6zsCrl70zXIY3OsCr/IZtNIldWLIe0woSIjb3+x0x/1bXUGW5XGGBcv4PtLVaN8jPg6K5VzXovZbhM1UpXOOhkVtnv07iUznv09P",
            "RSA-PUB:LqIO9c9X3sZYTBBu+mJHSpUPnO6QJg2Rmv2JeGidXsQDvkNbKQTUhPcgwAxNBtDLFp8hol/10rDwHeqfL04Lv5UYyKpDjmtskcnqlXMGoo0=",
            "RSA-PRIV:MIIEvgIBADANBgkqggiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDI0FQV9MReTvpjg+NpmAqUcqoVtER9wmCtmV/1nt0hHEdUVvl4Wj3DDBE6PMb3sDS2N/o9PF7E/2W39bjpU5MQPwPuzHPMA95chU0kuZxbjQgDXsnVpDCShUEFVonlHND6Z69WXGNvyG0+8SHTm433kdF+QHo1Q30dr9icvi8Yx62/IFbUGHA2Jqf0V+u8VuEIJIhDw2YpVsVCT4H7U/Jfqdpewl3w1eoHX0YpQ4/hwnPxCHCCAZZXSOY5rI3j49u7FTRGmGjGEY/OamyiuGi3aBAwzxzTtt50P0f95AoyoREmgywIXhnU7mhhgV3mvLAK/k7VAZE5nk75kBzi9mtVAgMBAAECggEBAL9rTmvreO/I6wbHZpR2OMeOkbDEuHEEQ3HXfNA+Ud1I/nlXus/NfYgaTaWs36ClF2oG7ANJM7EnHsf/c/b+EnZvCwmrXEpB+clJy+JRB8mBIv1LcquyqhZQ7UzwU9vQ3yeChHGW+bZJYFGaOzth0S8HkaF+Rr/VCEOvJpUq+OvtYAC09zJOX8Nz0DwQpwz+f6RObv8Izc1offy/TrKaaictr7lsjgrk0gc1ku0Hzq/9XCDoaNVtU5I4MUYdCMdI3P7HkOVnDsjz2xqCJ0EwPT9EnL1pqqnsa1cptfRs0Fm3/Zlt9SwG/HnJ4g6Wl102Z5Ixld7mpvWHq3OgpCrSleECgYEA/mufh+H1BKyvT0XQWBtsk6jS3AELNb3bds9c2rYIqxTLD40jHD9yo7sWBY9TYiPvwxAivcVXV4KP7sQZETSk9SEJCh5wL1XEdfjjy4KESw3190kfj3XY6cN0Mz6X+gvJvXe46ZqiYunDBI5fdOnM6at4Ui5zhMjFLk/7uHLTBv0CgYEAyg+Asaxl3xDTYeklfSQHicScU8TEZ9jh03yE9HFWfLwf6Ic2FQHvy8qGDFrNjGhvUggPBs5b7jG94+pGX4jtfaCU1L2GnE36hykW2QpOdJSVd1uB1fEy0oDmoXkE4R4wZccCr2RT8xKZGHItdD9xQdYbmd1Wa4+AgaZ8QJ/kYTkCgYAUdGimDxeY4Z1SnUVL4CCRmpAhWgAhuPrtCWzotJvrzUcqH+nDuqIn4cG6frRwfn0nTKPOaBBGm9ugkamGRZpBjv9gDeRtGdMqvPDrirnCIwQ0dm+QJCRlXu53tD+ZvdrhXb/DIEiP9UVVl3C7QJTV8JBC4zMsaMvZllqhkLHArQKBgFUZHFk3wxukCXJ174z5rJcQD8qT1yfpRop8Kb6NMSCNVl7m+xbz3tsUK0puv5y/qwqATFvQcxUpK9EeLI81qte61pOWUmfny3g7DN1RouMkZKKFDnRdBctz8C3XOv/YaAelPfNCLLz1eza5d3067ucMVkmB11CrejKgPDo3XMa5AoGBAN9ffHxoRW9Xm1S7VbylIMbAkQu6ToS29HwmZNeP9LgmuLsmH/X1jHLhkaWd5PBDgTFQdgG0lxo9+pLMp5KGEiKz8HDITfkHRJ5K9rXIRycpxkSH6LvzcL1qQ3i4KoTjb5+57M1IH0bbAP3Ej26/zUmqYBeIgI8krCeoPJwr4lDO",
            "AES:not a eas key"
        ])
        fun validationShouldNotRaiseExceptionInCaseOfBadKey(badKey: String) {

            // Given:
            keyProvider.setKeyFile(File(workDir, "invalid-key-${OrderedId}").apply {
                assertTrue(createNewFile(), "Unable to create file: $this")
                writeText(badKey)
            })

            //When:
            whenValidatingKeyProvider()

            //Then:
            assertThatHasValidationErrors(1)
            actualMessages()
        }

        private fun whenValidatingKeyProvider() {
            assertDoesNotThrow {
                validator.validateKeyProvider(
                    keyProvider,
                    keyNotSetMessage = { givenKeyNotSetMessage },
                    keyNotLoadingMessagePreamble = { givenKeyNotLoadingPreamble }
                )
            }
        }
    }

    private fun assertThatHasValidationErrors(expected: Int) {
        val actual = validator.failures().size
        assertEquals(expected, actual, "Expected to find $expected validation error(s)")
    }

    private fun actualMessages(): List<String> {
        return validator.failures().also { failures ->
            println("Actual validation messages (${failures.size})")
            println("----------------------------------------------------------------------------------")
            failures.forEachIndexed { i, (message, cause) ->
                println(buildString {
                    append(" ").append(i + 1).append(") ")
                    append(message)
                    cause?.also { append(" (Caused by ${cause.javaClass.name})") }
                })
            }
        }.map { it.message }
    }
}
