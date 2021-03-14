package graymatter.sec.common.validation

/**
 * This a very basic interface for validation messages.
 */
interface ValidationContext {

    interface Validation

    fun requires(validationPassed: Boolean, errorMessage: () -> String): Validation

    fun requires(lastValidation: Validation, validateFurther: ValidationContext.() -> Validation): Validation {
        return when {
            !passed(lastValidation) -> lastValidation
            else -> validateFurther()
        }
    }

    fun Validation.andThen(test: () -> Boolean, furtherErrorMessage: () -> String): Validation {
        return requires(this) { requires(test(), furtherErrorMessage) }
    }

    fun <T> Validation.andThenWith(nextSubject:() -> T, validateFurtherWith: ValidationContext.(subject:T) -> Validation): Validation {
        return when {
            !passed(this) -> this
            else -> validateFurtherWith(nextSubject())
        }
    }

    fun clear(validation: Validation): Validation?
    fun clear()

    fun passed(validation: Validation): Boolean
    fun passed(): Boolean

    fun failures(): ValidationErrors

}

