package graymatter.sec.usecase

import com.palantir.config.crypto.algorithm.Algorithm
import graymatter.sec.common.OrderedId
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class GenerateKeyUseCaseTest {

    @Test
    fun testHappyPath(@TempDir tempDir: File) {
        assertDoesNotThrow {
            GenerateKeyUseCase(
                keyName = "test_${OrderedId()}",
                keyLocation = tempDir,
                keyAlgorithm = Algorithm.AES,
                forceKeyLocation = false,
                overwriteExisting = true
            ).call().onSuccess {

                fun assertPathExistsAndPointsToNonEmptyFile(path: Path) {
                    val file = path.toFile()
                    assertTrue { file.isFile }
                    assertTrue { file.exists() }
                    assertTrue { file.length() > 0 }
                }

                assertAll(
                    { assertPathExistsAndPointsToNonEmptyFile(it.decryptionKeyFile()) },
                    { assertPathExistsAndPointsToNonEmptyFile(it.encryptionKeyFile()) }
                )
            }
        }
    }
}
