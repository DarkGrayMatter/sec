package me.andriefc.secj

import me.andriefc.secj.comand.DecryptValue
import me.andriefc.secj.comand.EncryptValue
import me.andriefc.secj.comand.GeneratKeyPair
import picocli.CommandLine
import picocli.CommandLine.Command
import kotlin.system.exitProcess


@Command(
    name = "secj",
    description = ["Security companion to the excellent Palantar library."],
    mixinStandardHelpOptions = true,
    subcommands = [
        GeneratKeyPair::class,
        EncryptValue::class,
        DecryptValue::class
    ]
)
class App

fun main(args: Array<String>) {
    exitProcess(
        CommandLine(App())
            .setAtFileCommentChar('@')
            .setExpandAtFiles(true)
            .setCaseInsensitiveEnumValuesAllowed(true)
            .execute(* args)
    )
}
