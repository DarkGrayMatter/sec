package graymatter.sec.usecase

import com.fasterxml.jackson.databind.node.ObjectNode
import graymatter.sec.App
import graymatter.sec.common.document.DocumentFormat
import graymatter.sec.common.document.DocumentMapper
import graymatter.sec.common.resourceAt
import graymatter.sec.testing.SampleConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.properties.ReadOnlyProperty


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ConvertConfigUseCaseTest {

    private lateinit var usecase: ConvertConfigUseCase
    private val sampleJsonConfig = SampleConfig(DocumentFormat.JSON)
    private val samplePropertiesConfig = SampleConfig(DocumentFormat.JAVA_PROPERTIES)
    private val sampleYamlConfig = SampleConfig(DocumentFormat.YAML)

    @Test
    fun testOk() {
        println(sampleYamlConfig)
    }


}

