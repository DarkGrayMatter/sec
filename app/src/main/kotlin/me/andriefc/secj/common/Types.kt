package me.andriefc.secj.common

import java.io.File

/**
 * An enum to indicate supported formats of documents this tool can process.
 */
enum class StructuredDocumentFormat(vararg validExtensions: String) {

    JSON("json"),
    YAML("yml", "yaml"),
    PROPERTIES("properties");

    private val fileExtensions = validExtensions.map(String::toLowerCase).toSet()

    companion object {

        /**
         * Determine yhe document format
         */
        @JvmStatic
        fun ofFile(file: File): StructuredDocumentFormat? {
            val ext = file.extension.toLowerCase()
            return values().firstOrNull { f -> ext in f.fileExtensions }
        }

    }
}


