@file:Suppress("unused")

package me.andriefc.secj.comand

import com.palantir.config.crypto.KeyFileUtils.keyPairToFile
import com.palantir.config.crypto.KeyPair
import com.palantir.config.crypto.KeyPairFiles
import com.palantir.config.crypto.algorithm.Algorithm
import picocli.CommandLine.*
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

@Command(
    name = "generate-key-pair",
    description = ["Generates private-public key pair"]
)
class GenerateKeyPairCommand : Runnable {

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
        names = ["--force-path"],
        description = ["Creates path if does not exists."],
        defaultValue = "false"
    )
    fun setForcePath(b: Boolean) {
        forcePath = b
    }

    @Parameters(
        index = "0",
        arity = "1",
        description = ["Where the keys file should be written to"],
        defaultValue = ".",
        paramLabel = "DESTINATION",
    )
    fun setDestination(path: File) {
        this.dest = path
    }


    override fun run() {
        val kp = algorithm.newKeyPair()
        val path = dest(keyName).toAbsolutePath()
        val keys = saveKeyPair(kp, path)
        println(
            """
            |Generated keys: 
            |   Encryption: ${keys.encryptionKeyFile()}
            |   Decryption: ${keys.decryptionKeyFile()}
        """.trimMargin()
        )
    }

    private fun saveKeyPair(kp: KeyPair, publicKeyPath: Path): KeyPairFiles {

        val privateKeyPath = publicKeyPath.resolveSibling("${publicKeyPath.fileName}.private")

        privateKeyPath.also(Files::deleteIfExists)
        publicKeyPath.also(Files::deleteIfExists)

        return keyPairToFile(kp, publicKeyPath)

    }

    private fun dest(path: String): Path = File(dest, path).run {
        if (!exists() && forcePath && !parentFile.mkdirs()) {
            throw IOException("Unable to create path for key files: $this")
        }
        canonicalFile
            .toPath()
            .toAbsolutePath()
    }
}

