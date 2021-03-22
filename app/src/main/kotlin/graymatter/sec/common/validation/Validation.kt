package graymatter.sec.common.validation

import graymatter.sec.common.OrderedId
import graymatter.sec.common.validation.Validation.Failure
import graymatter.sec.common.validation.Validator.ValidationLabel

interface Validator {

    /**
     * Allow validation messages to be group together.
     */
    interface ValidationLabel

    fun newValidationLabel(): ValidationLabel

    fun validate(performValidation: ValidationContext.() -> Unit): ValidationLabel
    fun validate(validationLabel: ValidationLabel, performValidation: ValidationContext.() -> Unit)

    fun hasFailures(): Boolean
    fun hasFailures(validationLabel: ValidationLabel): Boolean
    fun failures(): List<Failure>
    fun failures(validationLabel: ValidationLabel): List<Failure>
    fun passed(validationLabel: ValidationLabel): Boolean = !hasFailures(validationLabel)

    fun requiresThat(
        vararg passingValidationLabels: ValidationLabel,
        performValidation: ValidationContext.() -> Unit,
    ): ValidationLabel {
        return when (val firstFailure = passingValidationLabels.firstOrNull { passed(it) }) {
            null -> validate { performValidation() }
            else -> firstFailure
        }
    }
}

inline fun Validator.requiresThat(
    passed: Boolean,
    crossinline errorMessage: () -> String,
): ValidationLabel {
    return validate { if (!passed) failed(errorMessage()) }
}


sealed class Validation : Comparable<Validation> {

    class Passed() : Validation()

    class Failure(val message: String, val cause: Throwable?) : Validation() {
        constructor(error: String) : this(error, null)

        override fun toString(): String = message
        operator fun component1(): String = message
        operator fun component2(): Throwable? = cause
    }

    private val _id = OrderedId()
    val passed: Boolean get() = this is Passed

    override fun equals(other: Any?): Boolean {
        return when (other) {
            null -> false
            !is Validation -> false
            else -> other._id == _id
        }
    }

    override fun compareTo(other: Validation): Int {
        return _id.compareTo(other._id)
    }

    override fun hashCode(): Int {
        return _id.hashCode()
    }

}

interface ValidationContext {
    fun failed(error: String)
    fun failed(error: String, cause: Throwable)
    fun passedGroup()
    fun passed()
}

fun Validator(): Validator = ValidatorImpl()

private class ValidatorImpl : Validator {

    private val collectedFailures = linkedMapOf<ValidationLabel, MutableList<Failure>>()

    private class ValidationLabelImpl : ValidationLabel, Comparable<ValidationLabelImpl> {
        private val id = OrderedId()
        override fun toString(): String = id.toString()
        override fun compareTo(other: ValidationLabelImpl): Int = id.compareTo(other.id)
    }

    override fun newValidationLabel(): ValidationLabel {
        return ValidationLabelImpl()
    }

    override fun validate(
        performValidation: ValidationContext.() -> Unit,
    ): ValidationLabel {
        return newValidationLabel().also { validate(it, performValidation) }
    }

    override fun validate(
        validationLabel: ValidationLabel,
        performValidation: ValidationContext.() -> Unit,
    ) {
        object : ValidationContext {

            private val failures = mutableListOf<Failure>()

            init {
                performValidationOnce()
            }

            override fun failed(error: String) {
                failures += Failure(error)
            }

            override fun failed(error: String, cause: Throwable) {
                failures += Failure(error, cause)
            }

            override fun passedGroup() {
                failures.clear()
                collectedFailures[validationLabel]?.clear()
            }

            override fun passed() {
                failures.clear()
            }

            private fun performValidationOnce() {
                performValidation()
                val errors = failures.takeUnless { it.isEmpty() }?.toList() ?: return
                collectedFailures.computeIfAbsent(validationLabel) { mutableListOf() } += errors
            }
        }
    }

    override fun hasFailures(): Boolean {
        return collectedFailures.isNotEmpty()
    }

    override fun hasFailures(validationLabel: ValidationLabel): Boolean {
        return validationLabel in collectedFailures
    }

    override fun failures(): List<Failure> {
        return collectedFailures.flatMap { (_, c) -> c.toList() }
    }

    override fun failures(validationLabel: ValidationLabel): List<Failure> {
        return collectedFailures[validationLabel]?.toList() ?: emptyList()
    }
}
