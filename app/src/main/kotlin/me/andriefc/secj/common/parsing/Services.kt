package me.andriefc.secj.common.parsing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper


object Mappers {

    val json = ObjectMapper().findAndRegisterModules().configureCommons()
    val csv = CsvMapper().findAndRegisterModules().configureCommons()
    val properties = JavaPropsMapper().findAndRegisterModules().configureCommons()
    val yaml = YAMLMapper().findAndRegisterModules().configureCommons()

    fun StructuredDocumentFormat.mapper(): ObjectMapper {
        return when (this) {
            StructuredDocumentFormat.JSON -> json
            StructuredDocumentFormat.YAML -> yaml
            StructuredDocumentFormat.PROPERTIES -> properties
        }
    }


    private fun ObjectMapper.configureCommons() = apply {
        // todo: set date formatting
    }


}
