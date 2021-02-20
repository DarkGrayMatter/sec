package graymatter.sec.common.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper

object ObjectMappers {

    val json = ObjectMapper().configure()
    val csv = CsvMapper().configure()
    val properties = JavaPropsMapper().configure()
    val yaml = YAMLMapper().configure()

    private fun ObjectMapper.configure(): ObjectMapper = findAndRegisterModules()

    fun of(format: DocumentFormat): ObjectMapper {
        return when (format) {
            DocumentFormat.JSON -> json
            DocumentFormat.YAML -> yaml
            DocumentFormat.PROPERTIES -> properties
            DocumentFormat.CSV -> csv
        }
    }
}
