package graymatter.sec.common.validation

import graymatter.sec.common.OrderedId
import java.util.*

class DefaultValidationContext : ValidationContext {

    private val failures = TreeMap<ValidationId, String>()

    override fun requires(validationPassed: Boolean, errorMessage: () -> String): ValidationContext.Validation {
        return ValidationId().apply {
            if (!validationPassed) {
                failures[this] = errorMessage()
            }
        }
    }

    override fun clear(validation: ValidationContext.Validation): ValidationContext.Validation? {
        return failures.remove(validation)?.let { validation }
    }

    override fun clear() {
        failures.clear()
    }

    override fun passed(validation: ValidationContext.Validation): Boolean {
        return failures[validation] == null
    }

    override fun passed(): Boolean = failures.isEmpty()

    private class ValidationId : ValidationContext.Validation, Comparable<ValidationId> {
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

        override fun get(validation: ValidationContext.Validation): String? {
            val (_, error) = find(validation) ?: return null
            return error
        }

        override fun contains(validation: ValidationContext.Validation): Boolean {
            return find(validation) != null
        }

        private fun find(id: ValidationContext.Validation): Pair<ValidationContext.Validation, String>? {
            return listing.firstOrNull { (c, _) -> c == id }
        }

    }

}
