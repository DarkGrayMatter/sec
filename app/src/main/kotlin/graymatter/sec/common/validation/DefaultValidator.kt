package graymatter.sec.common.validation

import graymatter.sec.common.OrderedId
import java.util.*

class DefaultValidator : Validator {

    private val failures = TreeMap<ValidationId, String>()

    override fun requires(validationPassed: Boolean, errorMessage: () -> String): Validator.Validation {
        return ValidationId().apply {
            if (!validationPassed) {
                failures[this] = errorMessage()
            }
        }
    }

    override fun clear(validation: Validator.Validation): Validator.Validation? {
        return failures.remove(validation)?.let { validation }
    }

    override fun clear() {
        failures.clear()
    }

    override fun passed(validation: Validator.Validation): Boolean {
        return failures[validation] == null
    }

    override fun passed(): Boolean = failures.isEmpty()

    private class ValidationId : Validator.Validation, Comparable<ValidationId> {
        private val value = OrderedId()
        override fun toString(): String = value.toString()
        override fun compareTo(other: ValidationId): Int = value.compareTo(other.value)
    }

    override fun failures(): ValidationErrors {
        val listing = failures.keys.map { k -> k to failures[k] as String }.toList()
        return ErrorListing(listing)
    }

    private class ErrorListing(
        private val listing: List<Pair<ValidationId, String>>
    ) : List<String> by (listing.map { it.second }), ValidationErrors {

        override fun get(validation: Validator.Validation): String? {
            val (_, error) = find(validation) ?: return null
            return error
        }

        override fun contains(validation: Validator.Validation): Boolean {
            return find(validation) != null
        }

        private fun find(id: Validator.Validation): Pair<Validator.Validation, String>? {
            return listing.firstOrNull { (c, _) -> c == id }
        }

    }

}
