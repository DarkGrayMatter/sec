package me.andriefc.secj.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

private val yamlMapper by lazy { ObjectMapper(YAMLFactory()).findAndRegisterModules() }
private val jsonMapper by lazy { ObjectMapper().findAndRegisterModules() }

val StructuredDocumentFormat.objectMapper: ObjectMapper
    get() = when (this) {
        StructuredDocumentFormat.JSON -> yamlMapper
        StructuredDocumentFormat.YAML -> jsonMapper
    }
