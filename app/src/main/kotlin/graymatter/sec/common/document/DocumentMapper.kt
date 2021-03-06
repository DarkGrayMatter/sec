package graymatter.sec.common.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper

@Suppress("MemberVisibilityCanBePrivate")
object DocumentMapper {

    val json = ObjectMapper().configure()
    val properties = JavaPropsMapper().configure()
    val yaml = YAMLMapper().configure()

    private fun ObjectMapper.configure(): ObjectMapper = findAndRegisterModules()

    fun of(format: DocumentFormat): ObjectMapper {
        return when (format) {
            DocumentFormat.JSON -> json
            DocumentFormat.YAML -> yaml
            DocumentFormat.JAVA_PROPERTIES -> properties
        }
    }
}
