package graymatter.sec.common.validation

/**
 * This a very basic interface for validation messages.
 *
 * Messages may be
 * keyed,. or passed in as it. Keyed messages may removed either by key - a
 * specific message, or all of them via the [clear] functions.
 *
 */
interface Validator {

    interface Validation

    fun requires(validationPassed: Boolean, errorMessage: () -> String): Validation
    fun clear(validation: Validation): Validation?
    fun clear()

    fun passed(validation: Validation): Boolean
    fun passed(): Boolean

    fun failures(): ValidationErrors
}

