package me.andriefc.secj.comand

import com.palantir.config.crypto.KeyFileUtils
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.File

@Suppress("unused")
@Command(
    name = "encrypt-value",
    description = ["Encrypt value based on the supplied public key."]
)
class EncryptValue : Runnable {

    private lateinit var plainText: String
    private lateinit var publicKeyFile: File

    @Option(
        names = ["-k"],
        required = true,
        paramLabel = "PUBLIC_KEY_FILE",
        description = ["Public key used encrypt a value."]
    )
    fun setKeyPublicKeyFile(file: File) {
        this.publicKeyFile = file
    }

    @Option(
        names = ["-v"],
        required = true,
        description = ["Value to encrypt."]
    )
    fun setClearValue(string: String) {
        this.plainText = string
    }

    override fun run() {
        val kt = KeyFileUtils.keyWithTypeFromPath(publicKeyFile.toPath())
        val encryptor = kt.type.algorithm.newEncrypter()
        val encrypted = encryptor.encrypt(kt, plainText)
        println(encrypted)
    }
}
