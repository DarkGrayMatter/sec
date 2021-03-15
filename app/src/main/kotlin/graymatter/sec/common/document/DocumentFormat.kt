package graymatter.sec.common.document

/**
 * An enum to indicate supported formats of documents this tool can process.
 */
enum class DocumentFormat(vararg validExtensions: String) {

    JSON("json"),
    YAML("yml", "yaml"),
    JAVA_PROPERTIES("properties");

    val fileExtensions = validExtensions.map(String::toLowerCase).toList()
    val defaultFileExtension: String get() = fileExtensions.first()

    companion object {

        fun ofExt(ext: String): DocumentFormat {
            return values().first { ext.toLowerCase() in it.fileExtensions }
        }

        private val FORMATS_WITH_SUFFIXES: List<Pair<DocumentFormat, String>> =
            values().flatMap { format ->
                format.fileExtensions.map { ext ->
                    val suffix = ".${ext}".toLowerCase()
                    format to suffix
                }
            }

        /**
         * Determine yhe document format
         */
        @JvmStatic
        fun ofFile(file: java.io.File): DocumentFormat? {
            return ofUri(file.name)
        }

        /**
         * Determines the format denoted by the last part of a file name after the DOT ("**`.`**") character.
         */
        @JvmStatic
        fun ofUri(name: String): DocumentFormat? {
            return FORMATS_WITH_SUFFIXES.firstOrNull { (_, suffix) -> name.endsWith(suffix, ignoreCase = true) }?.first
        }
    }

}
