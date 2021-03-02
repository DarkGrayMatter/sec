package graymatter.sec.common.validation

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DefaultValidatorTest {

    private lateinit var validator: DefaultValidator

    @BeforeEach
    fun setUp() {
        validator = DefaultValidator()
    }

    @Test
    @DisplayName("validator-should-produce-error-in-same-order-as-supplied")
    fun validatorShouldProduceErrorInSameOrderAsSupplied() {

        // GIVEN:
        val expectedFailureSequence = listOf(
            "error-1",
            "error-2",
            "error-3"
        )

        // WHEN:
        val actualFailureSequence = validator.run {
            expectedFailureSequence.forEach { it.givenAsFailure() }
            failures()
        }

        // THEN
        assertEquals(
            expectedFailureSequence, actualFailureSequence,
            "then-expected-errors-should-match-given"
        )

    }

    @Test
    @DisplayName("validator-should-be-able-to-clear-previous-validation-error")
    fun validatorShouldBeAbleToClearPreviousValidationError() {

        // GIVEN:
        val (givenValidationToClear, _)
                = "expected-not-have-an-error".givenAsFailure()

        val (_, givenValidationKeepMessage)
                = "given-validation-error-to-raise".givenAsFailure()

        // WHEN-EXPECTING:
        assertTrue("when-expecting-validation-has-been-cleared") {
            validator.clear(givenValidationToClear)
            validator.passed(givenValidationToClear)
        }

        // THEN:
        assertEquals(
            listOf(givenValidationKeepMessage), validator.failures(),
            "then-expected-only-given-validation-error-to-raise"
        )

    }


    private fun String.givenAsFailure(): Pair<Validator.Validation, String> {
        return (validator.requires(validationPassed = false) { this } to this).also { (validation, _) ->
            assertFalse("expecting-validation-failed-for: [${validation}:$this]") { validator.passed(validation) }
            println("GIVEN-FAILURE-<$validation: $this>")
        }
    }

}
