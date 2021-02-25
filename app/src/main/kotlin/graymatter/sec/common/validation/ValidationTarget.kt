package graymatter.sec.common.validation

interface ValidationTarget {

    fun interface ValidationError {
        fun error(message: String)
    }

    fun performValidation(validation: ValidationError)
}
