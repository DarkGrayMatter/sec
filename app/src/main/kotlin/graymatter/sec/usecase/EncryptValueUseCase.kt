package graymatter.sec.usecase

import com.palantir.config.crypto.KeyWithType
import java.io.OutputStream

class EncryptValueUseCase(
    private val plainText: String,
    private val secretOut: () -> OutputStream,
    private val key: KeyWithType
) : Runnable {
    override fun run() {
        val encrypted = key.type.algorithm.newEncrypter().encrypt(key, plainText).toString()
        secretOut().writer().use { it.write(encrypted) }
    }
}
