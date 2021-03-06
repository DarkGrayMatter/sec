package graymatter.sec.common.validation

/**
 * This a very basic interface for validation messages.
 */
interface ValidationContext {

    interface Validation

    fun requires(validationPassed: Boolean, errorMessage: () -> String): Validation

    fun clear(validation: Validation): Validation?
    fun clear()

    fun passed(validation: Validation): Boolean
    fun passed(): Boolean

    fun failures(): ValidationErrors
}

