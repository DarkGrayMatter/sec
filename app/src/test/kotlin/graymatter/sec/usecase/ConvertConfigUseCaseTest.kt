package graymatter.sec.usecase

import com.fasterxml.jackson.databind.node.ObjectNode
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.document.treeOf
import graymatter.sec.common.func.right
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.stream.Stream
import kotlin.streams.asStream
import kotlin.test.assertEquals


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ConvertConfigUseCaseTest {

    @Language("JSON")
    private val configAsJson = """
        {
          "database": {
            "username": "sa",
            "password": "password",
            "jdbcUrl": "jdbc:h2:file://~/.h2/data"
          },
          "keys": [
            { "id": "thor", "value": "key1value" },
            { "id": "spiderman", "value":  "key2value"}
          ]
        }
    """.trimIndent()

    @Language("Properties")
    private val configAsJavaProps = """
        database.username=sa
        database.password=password
        database.jdbcUrl=jdbc:h2:file://~/.h2/data
        keys.1.id=thor
        keys.1.value=key1value
        keys.2.id=spiderman
        keys.2.value=key2value
    """.trimIndent()

    @Language("yaml")
    private val configAsYaml = """
        database:
          username: sa
          password: password
          jdbcUrl: "jdbc:h2:file://~/.h2/data"
        keys:
          - id: thor
            value: key1value
          - id: spiderman
            value: key2value
    """.trimIndent()

    @ParameterizedTest
    @MethodSource("testConversionRequestStream")
    fun testConversion(sourceFormat: DocumentFormat, expectedFormat: DocumentFormat) {

        fun sampleConfig(format: DocumentFormat): String {
            return when (format) {
                DocumentFormat.JSON -> configAsJson
                DocumentFormat.YAML -> configAsYaml
                DocumentFormat.JAVA_PROPERTIES -> configAsJavaProps
            }
        }

        val source = sampleConfig(sourceFormat)
        val expected = treeOf<ObjectNode>(expectedFormat, sampleConfig(expectedFormat))

        val (completionState, actual) = assertDoesNotThrow {
            val bytesOut = ByteArrayOutputStream()
            val completionState = ConvertConfigUseCase(
                sourceProvider = { ByteArrayInputStream(source.toByteArray(Charsets.UTF_8)) },
                sourceFormat = sourceFormat,
                targetProvider = { bytesOut },
                targetFormat = expectedFormat
            ).call().right
            completionState to treeOf<ObjectNode>(expectedFormat, bytesOut.toString(Charsets.UTF_8))
        }

        assertEquals(ConvertConfigUseCase.CompletionState.Completed, completionState)
        assertEquals(actual, expected)
    }


    fun testConversionRequestStream(): Stream<Arguments> {
        return sequence<Arguments> {
            for (inputFormat in DocumentFormat.values()) {
                for (outputFormat in DocumentFormat.values()) {
                    if (inputFormat != outputFormat) {
                        yield(Arguments.of(inputFormat, outputFormat))
                    }
                }
            }
        }.asStream()
    }

}

