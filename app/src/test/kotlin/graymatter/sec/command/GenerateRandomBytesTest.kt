package graymatter.sec.command

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut


@DisplayName("Testing the command to generate random bytes.")
internal class GenerateRandomBytesTest : AbstractCommandTest<GenerateRandomBytes>() {

    override fun setupCommand(): GenerateRandomBytes {
        return GenerateRandomBytes()
    }
}
