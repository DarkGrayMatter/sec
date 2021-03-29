package graymatter.sec.usecase

import com.palantir.config.crypto.KeyFileUtils.keyPairToFile
import com.palantir.config.crypto.KeyPairFiles
import com.palantir.config.crypto.algorithm.Algorithm
import graymatter.sec.common.func.Try
import graymatter.sec.common.func.Tried
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.RuntimeException
import java.util.concurrent.Callable

class GenerateKeyUseCase(
    private val keyName: String,
    private val keyAlgorithm: Algorithm,
    private val keyLocation: File,
    private val forceKeyLocation: Boolean,
    private val overwriteExisting: Boolean,
) : Callable<Tried<KeyPairFiles>> {

    class KeyGenerationException(val reason: String, cause: Exception? = null) : RuntimeException(reason, cause)

    private lateinit var preparedLocation: File

    override fun call():Tried<KeyPairFiles> = Try {
        prepareLocation()
        generateKeyPair()
    }

    private fun prepareLocation() {

        fun File.drop() {
            if (exists()) {
                if (!overwriteExisting) {
                    throw KeyGenerationException("Not allowed to overwrite: [$this]")
                }
                delete()
            }
        }

        preparedLocation = keyLocation.run {
            if (exists()) {
                if (!isDirectory) {
                    throw KeyGenerationException("Key location exists already, but not as directory.")
                }
            } else if (forceKeyLocation) {
                if (!mkdirs()) {
                    throw KeyGenerationException(
                        "Unable to create key pair path: $this",
                        IOException("Unable to create path")
                    )
                }
            } else {
                throw KeyGenerationException("Unable to create key", FileNotFoundException("$keyLocation"))
            }
            canonicalFile.absoluteFile.also { parent ->
                File(parent, keyName).drop()
                File(parent, "$keyName.private").drop()
            }
        }
    }

    private fun generateKeyPair(): KeyPairFiles {
        return keyPairToFile(keyAlgorithm.newKeyPair(), File(preparedLocation, keyName).toPath())
    }
}
