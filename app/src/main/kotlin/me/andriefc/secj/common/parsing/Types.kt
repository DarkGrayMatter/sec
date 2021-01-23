@file:Suppress("unused", "ClassName")

package me.andriefc.secj.common.parsing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import java.io.InputStream



/**
 * An enum to indicate supported formats of documents this tool can process.
 */
enum class StructuredDocumentFormat(vararg validExtensions: String) {

    JSON("json"),
    YAML("yml", "yaml"),
    PROPERTIES("properties"),
    CSV("csv");

    private val extensions = validExtensions.map(String::toLowerCase)

    companion object {

        private val formatsWithSuffixes: List<Pair<StructuredDocumentFormat, String>> =
            values().flatMap { format ->
                format.extensions.map { ext ->
                    val suffix = ".${ext}".toLowerCase()
                    format to suffix
                }
            }

        /**
         * Determine yhe document format
         */
        @JvmStatic
        fun ofFile(file: java.io.File): StructuredDocumentFormat? {
            return fromName(file.name)
        }

        /**
         * Determines the format denoted by the last part of a file name after the DOT ("**`.`**") character.
         */
        @JvmStatic
        fun fromName(name: String): StructuredDocumentFormat? {
            return formatsWithSuffixes.firstOrNull { (_, suffix) -> name.endsWith(suffix, ignoreCase = true) }?.first
        }
    }

}


