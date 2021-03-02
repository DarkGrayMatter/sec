package graymatter.sec.usecase

import com.palantir.config.crypto.KeyWithType
import graymatter.sec.common.document.DocumentFormat
import java.io.InputStream
import java.io.OutputStream

class DecryptConfigUseCase(
    private val keyWithType: KeyWithType,
    private val source: () -> InputStream,
    private val sourceFormat: DocumentFormat,
    private val destination: () -> OutputStream,
    private val destinationFormat: DocumentFormat
) {
    fun run() {
        TODO()
    }
}
