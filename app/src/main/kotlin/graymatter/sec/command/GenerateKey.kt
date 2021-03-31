@file:Suppress("unused")

package graymatter.sec.command

import com.palantir.config.crypto.KeyPairFiles
import com.palantir.config.crypto.algorithm.Algorithm
import graymatter.sec.common.cli.SelfValidatingCommand
import graymatter.sec.common.exception.failCommand
import graymatter.sec.common.func.Either
import graymatter.sec.common.validation.Validator
import graymatter.sec.usecase.GenerateKeyUseCase
import graymatter.sec.usecase.GenerateKeyUseCase.KeyGenerationException
import picocli.CommandLine.*
import java.io.File
import kotlin.math.max

@Command(
    name = "generate-key",
    description = ["Generates AES key, or RSA private-public key pair"]
)
class GenerateKey : SelfValidatingCommand() {

    private var failOnExistingKeyFiles: Boolean = false
    private lateinit var dest: File
    private var forcePath = true
    private lateinit var algorithm: Algorithm
    private lateinit var keyName: String

    @Option(
        names = ["--alg", "-a"],
        description = ["Which algorithm to use to generate the key pair. The following are available: RSA, AES"],
        required = true,
    )
    fun setAlgorithm(a: Algorithm) {
        this.algorithm = a
    }

    @Option(
        names = ["--key", "-k"],
        description = ["Name of the key file."],
        required = true
    )
    fun setKeyName(s: String) {
        keyName = s
    }

    @Option(
        names = ["--do-not-force-path"],
        description = ["Creates path if does not exists."],
        defaultValue = "false"
    )
    fun setForcePath(b: Boolean) {
        forcePath = !b
    }

    @Option(
        names = ["--do-not-replace"],
        description = ["Generation of keys will fail if the same key(or keys), are found at the same destination"],
        showDefaultValue = Help.Visibility.ALWAYS,
        defaultValue = "false"
    )
    fun setFailOnExistingKeys(failOnExistingKeyFiles: Boolean) {
        this.failOnExistingKeyFiles = failOnExistingKeyFiles
    }

    @Parameters(
        index = "0",
        arity = "1",
        description = ["Where the keys file should be written to"],
        defaultValue = ".",
        paramLabel = "DIRECTORY",
        showDefaultValue = Help.Visibility.ALWAYS
    )
    fun setDestination(path: File) {
        this.dest = path
    }

    override fun performAction() {
        when (val r = GenerateKeyUseCase(
            keyName = this.keyName,
            keyAlgorithm = this.algorithm,
            forceKeyLocation = this.forcePath,
            overwriteExisting = !failOnExistingKeyFiles,
            keyLocation = this.dest,
        ).call()) {
            is Either.Left -> reportKeyGenerationFailed(r.value)
            is Either.Right -> reportKeyFilesGenerated(r.value)
        }
    }

    override fun Validator.validateSelf() = Unit

    private fun reportKeyGenerationFailed(keyGenerationException: KeyGenerationException) {
        failCommand(ExitCode.SOFTWARE, buildString {
            appendLine("Error generating key [$keyName]")
            appendLine(keyGenerationException.reason)
            keyGenerationException.cause?.also { cause ->
                val causeBy = cause.message ?: "${cause.javaClass.simpleName} (No message given)"
                appendLine("Cause by: $causeBy")
            }
        })
    }

    private fun reportKeyFilesGenerated(files: KeyPairFiles) {

        val infoLines = when {
            files.pathsEqual() -> listOf("Shared Encryption & Decryption Key : ${files.decryptionKeyFile()}")
            else -> listOf(
                "Encryption (Public) key file: ${files.encryptionKeyFile()}",
                "Decryption (Private) key file: ${files.decryptionKeyFile()}"
            )
        }

        val keyTypeLabel = when (algorithm) {
            Algorithm.AES -> "shared key"
            Algorithm.RSA -> "private and public keys"
        }

        val heading = "Generated $keyTypeLabel"
        val headingLineWidth = max(heading.length, infoLines.maxByOrNull(String::length)!!.length) + 6
        val headingLine = headingLineWidth.let { buildString { repeat(it) {append('=')} } }

        println(headingLine)
        println("  $heading")
        println(headingLine)
        infoLines.withIndex().forEach { (i,line) ->
            println(" ${i + 1}. $line")
        }

    }

}


