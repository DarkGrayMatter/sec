package graymatter.sec.common.validation

fun interface ValidationErrorHandler {
    fun fail(errorBundle: List<String>): Nothing
}
