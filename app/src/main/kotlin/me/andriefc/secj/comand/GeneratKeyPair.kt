package me.andriefc.secj.comand

import picocli.CommandLine.Command


@Command(
    name = "generate-key-pair",
    description = ["Generates private-public key pair"]
)
class GeneratKeyPair : Runnable {
    override fun run() {
        TODO("Not yet implemented")
    }
}
