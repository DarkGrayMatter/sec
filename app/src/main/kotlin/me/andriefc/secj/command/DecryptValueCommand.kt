package me.andriefc.secj.command

import com.palantir.config.crypto.EncryptedValue
import com.palantir.config.crypto.KeyFileUtils
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.File


@Command(
    name = "decrypt-value",
    description = ["Decrypts a value given a private key."]
)
@Suppress("unused")
class DecryptValueCommand : Runnable {

    private lateinit var secretText: String
    private lateinit var secretKey: File

    override fun run() {
        val encrypted = EncryptedValue.fromString(secretText)
        val kt = KeyFileUtils.keyWithTypeFromPath(secretKey.toPath())
        val decrypted = encrypted.decrypt(kt)
        println(decrypted)
    }

    @Option(
        names = ["-k"],
        required = true,
        paramLabel = "PRIVATE_KEY_FILE",
        description = ["Private key required to decrypt the secret text."]
    )
    fun setSecretKey(file: File) {
        secretKey = file
    }

    @Option(
        names = ["-v"],
        required = true,
        description = ["Secret text to decrypt."]
    )
    fun setSecretText(text: String) {
        secretText = text
    }
}
