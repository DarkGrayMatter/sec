package graymatter.sec.usecase

import java.security.SecureRandom


class GenerateRandomBytesUseCase(
    seed: ByteArray?,
) {

    private val generator: (Int) -> ByteArray = when (seed) {
        null -> SecureRandom().run { { n: Int -> ByteArray(n).apply { nextBytes(this) } } }
        else -> SecureRandom(seed).run { { n: Int -> ByteArray(n).apply { nextBytes(this) } } }
    }

    fun generate(n: Int) = generator.invoke(n)


}
