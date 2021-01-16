package me.andriefc.secj.comand

import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(
    name = "encrypt-config",
    description = ["Encrypts a configuration document. (Supports yaml, JSon, & java properties)"]
)
class EncryptStructuredDocumentCommand : Runnable {

    private lateinit var publicKeySource: String
    private lateinit var configSource: String

    @Option(names = ["-k"], description = ["Public key to encrypt with."], required = true, paramLabel = "<key>")
    fun setKeySource(s: String) {
        publicKeySource = s
    }

    @Option(
        names = ["-v"],
        description = ["Configuration document to encrypt."],
        required = true,
        paramLabel = "<config>"
    )
    fun setComfigSource(s: String) {
        configSource = s
    }

    override fun run() {
        println("publicKeySource : $publicKeySource")
        println("configurationSource: $configSource")
    }

}


