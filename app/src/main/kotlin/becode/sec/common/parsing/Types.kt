@file:Suppress("unused", "ClassName")

package becode.sec.common.parsing

import becode.sec.common.Separator

/**
 * An enum to indicate supported formats of documents this tool can process.
 */
enum class DocumentedFormat(vararg validExtensions: String) {

    JSON("json"),
    YAML("yml", "yaml"),
    PROPERTIES("properties"),
    CSV("csv");

    private val extensions = validExtensions.map(String::toLowerCase)

    companion object {

        private val formatsWithSuffixes: List<Pair<DocumentedFormat, String>> =
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
        fun ofFile(file: java.io.File): DocumentedFormat? {
            return fromName(file.name)
        }

        /**
         * Determines the format denoted by the last part of a file name after the DOT ("**`.`**") character.
         */
        @JvmStatic
        fun fromName(name: String): DocumentedFormat? {
            return formatsWithSuffixes.firstOrNull { (_, suffix) -> name.endsWith(suffix, ignoreCase = true) }?.first
        }
    }

}


class Path private constructor(
    val value: String,
    val separator: Separator,
    parts: List<String>
) : List<String> by parts {

    constructor(
        separator: Separator,
        parts: List<String>
    ): this(parts.joinToString("${separator.char}"), separator, parts)

    companion object {

    }

}


